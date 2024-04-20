package org.kissweb.rest;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.kissweb.DateUtils;
import org.kissweb.JsonPath;
import org.kissweb.database.Connection;
import org.kissweb.database.Record;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Author: Blake McBride
 * Date: 11/24/19
 */
public class ProcessServlet implements Runnable {

    private static final Logger logger = Logger.getLogger(ProcessServlet.class);
    private static final Map<String,String> fakePersonMap = new HashMap<>();  //  sessionId, personId
    private static final String fakePerson1 = "4f431efe-6cd6-49b6-87f8-8057102daeac";
    private static final String fakePerson2 = "3d3b8841-a0e4-4803-881e-508d94fde774";
    private static final String fakePerson3 = "9289cd96-0ff1-4113-ad5f-106d3a08634d";
    private ServletRequest request;
    private ServletResponse response;
    private String instr;
    private JSONObject outjson;
    static final int MaxHold = 600;         // number of seconds to cache microservices before unloading them
    static final int CheckCacheDelay = 60;  // how often to check to unload microservices in seconds
    public Connection DB;
    private String sessionId;  //  the current interaction session
    private String userId;     // AWS account (many individuals / a household / a family)
    public String aid;         // account id, my internal representation of userId
    public String personId;    // Unique person within a single AWS account
    public String pid;         // my internal representation of personId
    public String apiAccessToken;
    public String deviceId;
    public boolean personChanged = false;
    private boolean responded = false;
    private Map<String,String> slotMap;
    private Map<String,String> sessionAttributes;
    enum ExecutionReturn {
        Success,
        NotFound,
        Error
    }

    ProcessServlet(QueueManager.Packet packet) {
        request = packet.asyncContext.getRequest();
        response = packet.asyncContext.getResponse();
    }

    ProcessServlet(HttpServletRequest request, HttpServletResponse response, String instr) {
        this.request = request;
        this.response = response;
        this.instr = instr;
    }

    @Override
    public void run() {
        try {
            run2();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run2() throws IOException {
        JSONObject injson;
        ExecutionReturn res;

 //       String instr = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        if (MainServlet.isDebug()) {
            logMessage("\n* * * * text received  * * * *\n");
            logMessage(instr);
            logMessage("\n* * * * end of text  * * * *\n");
        }
        injson = new JSONObject(instr);

        if (!valididateRequest(injson, MainServlet.getAmazonApplicationId())) {
            invalidReturn(response);
            logger.error("Invalid/unauthorized request");
            return;
        }

        if (MainServlet.isDebug()) {
            logMessage("sessionId = " + sessionId);
            logMessage("userId = " + userId);
            logMessage("personId = " + personId);
        }
        outjson = new JSONObject();
        outjson.put("version", "1.0");

        try {
            newDatabaseConnection();
        } catch (Throwable e) {
            if (e instanceof Exception)
                errorReturn(response, "Unable to connect to the database", (Exception) e);
            else
                errorReturn(response, "Unable to connect to the database", null);
            return;
        }

        try {
            injson.put("newUser", getUserInfo());
        } catch (SQLException e) {
            errorReturn(response, "Error getting user info", e);
            throw new IOException(e);
        }

        String requestType = JsonPath.getString(injson, "request.type");
        if (requestType == null ||  requestType.isEmpty()) {
            errorReturn(response, "missing request type", null);
            return;  // error
        }

        switch (requestType) {
            case "LaunchRequest":
            case "SessionEndedRequest":
                res = (new GroovyService()).tryGroovy(this, response, requestType, "main", injson, outjson);
                if (res == ExecutionReturn.Success) {
                    try {
                        updateLastSeen();
                    } catch (SQLException e) {
                        throw new IOException(e);
                    }
                    successReturn(response);
                } else
                    errorReturn(response, requestType + " produced a failed response", null);
                break;
            case "IntentRequest":
                String intentName = JsonPath.getString(injson, "request.intent.name");
                if (intentName == null || intentName.isEmpty()) {
                    errorReturn(response, "missing intent name", null);
                    return;  // error
                }
                intentName = intentName.replace('.', '_');
                runGroovy(injson, outjson, requestType + "." + intentName);
                break;
        }
    }

    private byte[] loadPEM(String resource) throws IOException {
        URL url = new URL(resource);
        InputStream in = url.openStream();
        String pem = new String(readAllBytes(in), StandardCharsets.ISO_8859_1);
        Pattern parse = Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*");
        String encoded = parse.matcher(pem).replaceFirst("$1");
        return Base64.getMimeDecoder().decode(encoded);
    }

    private byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream baos= new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        for (int read=0; read != -1; read = in.read(buf))
            baos.write(buf, 0, read);
        return baos.toByteArray();
    }

    private String getPersonId(JSONObject injson) {
        String personId = JsonPath.getString(injson, "context.System.person.personId");
        if (personId != null  &&  !personId.isEmpty())
            return personId;

        if (sessionId == null ||  !fakePersonMap.containsKey(sessionId))
            return null;
        return fakePersonMap.get(sessionId);
    }

    public void setFakePersonId(int index) {
        switch (index) {
            case -1:  // erase all fake persons
                fakePersonMap.clear();
                personId = null;
                break;
            case 0:  // erase fake person for this session
                personId = null;
                fakePersonMap.remove(sessionId);
                break;
            case 1:
                fakePersonMap.put(sessionId, personId = fakePerson1);
                break;
            case 2:
                fakePersonMap.put(sessionId, personId = fakePerson2);
                break;
            case 3:
                fakePersonMap.put(sessionId, personId = fakePerson3);
                break;
        }
    }

    private static String getSHA1(String input) {
        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32)
                hashtext = "0" + hashtext;

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void runGroovy(JSONObject injson, JSONObject outjson, String module) throws IOException {
        ExecutionReturn res = (new GroovyService()).tryGroovy(this, response, module, "main", injson, outjson);
        if (!responded) {
            responded = true;
            if (res == ExecutionReturn.Success)
                successReturn(response);
            else
                errorReturn(response, module + " produced a failed response", null);
        }
    }

    private void updateLastSeen() throws SQLException {
        Record account = DB.fetchOne("select * from account where user_id=?", userId);
        account.setDateTime("last_seen", new Date());
        account.update();
    }

    private boolean valididateRequest(JSONObject injson, String id) {
        String applicationId = JsonPath.getString(injson, "context.System.application.applicationId");
        if (applicationId == null)
            return false;
        if (!applicationId.equals(id))
            return false;

        // validate request time
        String timestamp = JsonPath.getString(injson, "request.timestamp");
        if (timestamp == null  ||  timestamp.isEmpty()) {
            logMessage("missing timestamp");
            return false;
        }
        ZonedDateTime zdt = ZonedDateTime.parse(timestamp);
        Instant now = Instant.now();
        ZoneId zoneId = ZoneId.of("Z");
        ZonedDateTime dateAndTimeInLA = ZonedDateTime.ofInstant(now, zoneId);
        long seconds = ChronoUnit.SECONDS.between(zdt, dateAndTimeInLA);
        if (seconds < 0L)
            seconds = -seconds;
        if (seconds > 150L) {
            logMessage("out of time range");
            return false;
        }

        sessionId = JsonPath.getString(injson, "session.sessionId");
        userId = JsonPath.getString(injson, "session.user.userId");
        personId = getPersonId(injson);
        deviceId = JsonPath.getString(injson, "context.System.device.deviceId");
        apiAccessToken = JsonPath.getString(injson, "context.System.apiAccessToken");

        JSONObject slots = JsonPath.getObject(injson, "request.intent.slots");
        slotMap = new HashMap<>();
        if (slots != null)
            for (String key : slots.keySet())
                rememberSlot(key, JsonPath.getString(slots, key + ".value"));

        JSONObject atts = JsonPath.getObject(injson, "session.attributes");
        sessionAttributes = new HashMap<>();
        if (atts != null)
            for (String key : atts.keySet())
                rememberAttribute(key, atts.getString(key));

        // cache the personId in case Alexa can't recognize their voice.  Assume same person from prior intent.
        if (personId == null)
            personId = getAttribute("personId");
        else {
            String oldPersonId = getAttribute("personId");
            if (oldPersonId == null  ||  !oldPersonId.equals(personId)) {
                rememberAttribute("personId", personId);
                personChanged = oldPersonId != null;
            }
        }

        return true;
    }

    /**
     *
     * @return true if the user is new, false if existing user
     * @throws SQLException
     */
    private boolean getUserInfo() throws SQLException {
        Record account = DB.fetchOne("select * from account where user_id=?", userId);
        if (account == null) {
            account = DB.newRecord("account");
            account.set("user_id", userId);
            aid = UUID.randomUUID().toString();
            account.set("aid", aid);
            Date dt = new Date();
            account.setDateTime("when_created", dt);
            account.setDateTime("last_seen", dt);
            account.addRecord();
            if (personId != null) {
                Record person = DB.newRecord("person");
                person.set("person_id", personId);
                pid = UUID.randomUUID().toString();
                person.set("pid", pid);
                person.set("aid", aid);
                person.addRecord();
            } else
                pid = null;
            return true;  //  new user
        } else {
            boolean newUser = false;
            aid = account.getString("aid");
            if (personId != null) {
                Record person = DB.fetchOne("select * from person where person_id=?", personId);
                if (person == null) {
                    person = DB.newRecord("person");
                    person.set("person_id", personId);
                    pid = UUID.randomUUID().toString();
                    person.set("pid", pid);
                    person.set("aid", aid);
                    person.addRecord();
                    newUser = true;
                } else
                    pid = person.getString("pid");
            } else
                pid = null;
            return newUser;
        }
    }

    private void newDatabaseConnection() throws SQLException {
        if (!MainServlet.hasDatabase())
            return;
        if (MainServlet.isDebug())
            System.err.println("Previous open database connections = " + MainServlet.getCpds().getNumBusyConnections());
        DB = new Connection(MainServlet.getCpds().getConnection());
        if (MainServlet.isDebug())
            System.err.println("New database connection obtained");
    }

    private void successReturn(ServletResponse response) throws IOException {
        try {
            if (DB != null)
                DB.commit();
        } catch (SQLException e) {

        }
        if (MainServlet.isDebug()) {
            logMessage("\n* * * success - returning the following * * *\n");
            logMessage(outjson.toString());
        }
        response.setContentType("application/json");
        ((HttpServletResponse) response).setStatus(200);
        PrintWriter out = response.getWriter();
        out.print(outjson);
        out.flush();
        out.close();
         closeSession();
    }

    void errorReturn(ServletResponse response, String msg, Exception e) {
        if (DB != null) {
            try {
                DB.rollback();
            } catch (SQLException e1) {
            }
        }
        closeSession();
        if (MainServlet.isDebug()) {
            logMessage("\n* * * error - returning the following * * *\n");
            if (msg != null)
                logMessage(msg);
            if (e != null  &&  e.getMessage() != null)
                logMessage(e.getMessage());
        }
        response.setContentType("application/json");
        ((HttpServletResponse) response).setStatus(200);
        JSONObject outjson = new JSONObject();
        PrintWriter out;
        log_error(msg, e);
        try {
            out = response.getWriter();
            out.print(outjson);
            out.flush();
            out.close();
        } catch (IOException e1) {
            // ignore
        }
    }

    private void invalidReturn(ServletResponse response) {
        if (DB != null) {
            try {
                DB.rollback();
            } catch (SQLException e1) {
            }
        }
        closeSession();
        if (MainServlet.isDebug()) {
            logMessage("\n* * * invalid request * * *\n");
        }
        response.setContentType("application/json");
        ((HttpServletResponse) response).setStatus(400);
        JSONObject outjson = new JSONObject();
        PrintWriter out;
        log_error("invalid request", null);
        try {
            out = response.getWriter();
            out.print(outjson);
            out.flush();
            out.close();
        } catch (IOException e1) {
            // ignore
        }
    }

    private void log_error(final String str, final Throwable e) {
        String time = DateUtils.todayDate() + " ";
        logger.error(time + str, e);
        if (e != null) {
            e.printStackTrace();
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error(sw.toString());
        }
    }

    private void closeSession() {
        java.sql.Connection sconn = null;
        try {
            if (DB != null) {
                sconn = DB.getSQLConnection();
                DB.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DB = null;
        }
        try {
            if (sconn != null)
                sconn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getOutjson() {
        return outjson;
    }

    public static void logMessage(String str) {
        try {
            str += "\n";
            Files.write(Paths.get("logfile.txt"), str.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            // ignore
        }
    }

    public static void logMessage(Exception e, String str) {
        try {
            str += "\n";
            if (e != null) {
                String msg = e.getMessage();
                if (msg != null) {
                    str += msg + "\n";
                }
                Throwable cause = e.getCause();
                if (cause != null) {
                    msg = cause.getMessage();
                    if (msg != null) {
                        str += msg + "\n";
                    }
                }
            }
            Files.write(Paths.get("logfile.txt"), str.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e2) {
            // ignore
        }
    }

    public void forgetSlot(String fld) {
        slotMap.put(fld, null);
        //       slotMap.remove(fld);
    }

    public void forgetAllSlots() {
        slotMap.clear();
    }

    public void rememberSlot(String fld, String val) {
        slotMap.put(fld, val);
    }

    public String getSlot(String fld) {
        return slotMap.get(fld);
    }

    public Map<String,String> getSlotMap() {
        return slotMap;
    }


    public void forgetAttribute(String fld) {
        //sessionAttributes.put(fld, null);
        sessionAttributes.remove(fld);
    }

    public void forgetAllAttributes() {
        String personId = sessionAttributes.get("personId");
        String timeZone = sessionAttributes.get("timeTone");
        sessionAttributes.clear();
        if (personId != null)
            sessionAttributes.put("personId", personId);
        if (timeZone != null)
            sessionAttributes.put("timeZone", timeZone);
    }

    /**
     * Remember an attribute.
     *
     * @param fld the attribute name
     * @param val the attribute value
     * @return val
     */
    public String rememberAttribute(String fld, String val) {
        sessionAttributes.put(fld, val);
        return val;
    }

    public String getAttribute(String fld) {
        return sessionAttributes.get(fld);
    }

    public Map<String,String> getAttributeMap() {
        return sessionAttributes;
    }

}
