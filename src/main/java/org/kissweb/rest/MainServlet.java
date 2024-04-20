package org.kissweb.rest;

import com.amazon.ask.servlet.verifiers.AlexaHttpRequest;
import com.amazon.ask.servlet.verifiers.SkillRequestSignatureVerifier;
import com.amazon.ask.servlet.verifiers.SkillServletVerifier;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.kissweb.database.Connection;

import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyVetoException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static org.kissweb.rest.ProcessServlet.logMessage;


/**
 * Author: Blake McBride
 * Date: 5/4/18
 */
@WebServlet(urlPatterns="/rest")
public class MainServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(MainServlet.class);
    private static Connection.ConnectionType connectionType;
    private static String host;                      // set by KissInit.groovy
    private static String database;                  // set by KissInit.groovy
    private static String user;                      // set by KissInit.groovy
    private static String password;                  // set by KissInit.groovy
    private static String applicationPath;
    private static boolean underIDE = false;
    private static ComboPooledDataSource cpds;
    private static boolean debug = false;          // set by KissInit.groovy
    private static boolean hasDatabase;            // determined by KissInit.groovy
    private static String amazonApplicationId;
    private static boolean systemInitialized = false;
    private static int maxWorkerThreads;

    private static String smtpHost;
    private static String smtpUsername;
    private static String smtpPassword;

    private static int maxParallelThreads = 1;
    private static int currentParallelThreads;
    private final static Object lock = new Object();

    private QueueManager queueManager;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!systemInitialized) {
            try {
                setApplicationPath(request);
                initializeSystem(response);
                if (!systemInitialized)
                    return;
            } catch (Exception e) {
                return;
            }
        }


        /////  Begin my header checks

        String signatureCertChainUrl = request.getHeader("signaturecertchainurl");
        String signature = request.getHeader("signature");
        if (signatureCertChainUrl == null  ||  signatureCertChainUrl.isEmpty()  ||  signature == null  ||  signature.isEmpty()) {
            invalidReturn(response);
            logMessage("signaturecertchainurl or signature is missing");
            return;
        }

        String signatureCertChainUrlNorm;
        try {
            signatureCertChainUrlNorm = (new URI(signatureCertChainUrl)).normalize().toString();
        } catch (URISyntaxException e) {
            invalidReturn(response);
            logMessage("Error normalizing host URL");
            return;
        }

        if (!signatureCertChainUrlNorm.startsWith("https://s3.amazonaws.com/echo.api") && !signatureCertChainUrlNorm.startsWith("https://s3.amazonaws.com:443/echo.api")) {
            invalidReturn(response);
            logMessage("Invalid prefix");
            return;
        }

        //////  End my header checks

        /////////   Begin Alexa ASK header verification
        final InputStream is = request.getInputStream();
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        final byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        final byte[] requestBodyByteArray = buffer.toByteArray();

        final AlexaHttpRequest ahr = new com.amazon.ask.servlet.verifiers.ServletRequest(request, requestBodyByteArray);
        final SkillServletVerifier verif = new SkillRequestSignatureVerifier();
        try {
            verif.verify(ahr);
        } catch (Exception e) {
            invalidReturn(response);
            logMessage("failed ASK verifier");
            return;
        }
        ///////////////  End Alexa ASK header verification

        // keep a log of max threads so I know when there is a problem
        synchronized (lock) {
            if (++currentParallelThreads > maxParallelThreads) {
                maxParallelThreads = currentParallelThreads;
                Level lvl = logger.getLevel();
                logger.setLevel(Level.ALL);
                logger.info("Max threads = " + maxParallelThreads);
                logger.setLevel(lvl);
            }
        }

        // async code - disabled for now
        /*
        if (queueManager == null)
            queueManager = new QueueManager(maxWorkerThreads);
        queueManager.add(request, response);
         */

        //  temporary sync code
        ProcessServlet ps = new ProcessServlet(request, response, new String(requestBodyByteArray));
        ps.run();


        synchronized (lock) {
            --currentParallelThreads;
        }
    }

    private void initializeSystem(ServletResponse response) throws ClassNotFoundException, PropertyVetoException, SQLException {
        ProcessServlet.ExecutionReturn res = (new GroovyService()).internalGroovy(null, response, null, "KissInit", "init");
        if (res == ProcessServlet.ExecutionReturn.Success) {
            hasDatabase = database != null  &&  !database.isEmpty();
            if (hasDatabase)
                makeDatabaseConnection();
            else
                System.out.println("* * * No database configured; bypassing login requirements");
            systemInitialized = true;
        }
    }

    private void makeDatabaseConnection() throws PropertyVetoException, SQLException, ClassNotFoundException {
        if (!hasDatabase) {
            System.out.println("* * * No database configured; bypassing login requirements");
            return;
        }
        if (cpds == null) {
            System.out.println("* * * Attempting to connect to database " + host + ":" + database + ":" + user);
            String cstr = Connection.makeConnectionString(connectionType, host, database, user, password);
            Connection con;
            try {
                con = new Connection(connectionType, cstr);
            } catch (Exception e) {
                System.out.println("* * * Database connection failed (see application/KissInit.groovy)");
                System.out.println("* * * " + e.getMessage());
                throw e;
            }
            con.close();
            System.out.println("* * * Database connection succeeded");

            cpds = new ComboPooledDataSource();

            cpds.setJdbcUrl(cstr);

            cpds.setDriverClass(Connection.getDriverName(connectionType));

            cpds.setMaxStatements(180);
        }
    }

    private void invalidReturn(ServletResponse response) {
        if (MainServlet.isDebug()) {
            logMessage("\n* * * invalid request * * *\n");
        }
        response.setContentType("application/json");
        ((HttpServletResponse) response).setStatus(400);
        JSONObject outjson = new JSONObject();
        PrintWriter out;
        try {
            out = response.getWriter();
            out.print(outjson);
            out.flush();
            out.close();
        } catch (IOException e1) {
            // ignore
        }
    }


    private static void setApplicationPath(HttpServletRequest request) {
        String cpath = request.getServletContext().getRealPath("/");
        System.out.println("* * * Context path = " + cpath);
        applicationPath = System.getenv("KISS_DEBUG_ROOT");
        if (applicationPath == null || applicationPath.isEmpty()) {
            if ((new File(cpath + "../../src/main/application/" + "KissInit.groovy")).exists()) {
                applicationPath = cpath + "../../src/main/application/";
                underIDE = true;
            } else if ((new File(cpath + "../../../../src/main/application/" + "KissInit.groovy")).exists()) {
                applicationPath = cpath + "../../../../src/main/application/";
                underIDE = true;
            } else {
                applicationPath = cpath + (cpath.endsWith("/") ? "" : "/") + "WEB-INF/application/";
                underIDE = false;
            }
        } else {
            underIDE = true;
            applicationPath = applicationPath.replaceAll("\\\\", "/");
            applicationPath = applicationPath + (applicationPath.endsWith("/") ? "" : "/") + "src/main/application/";
        }
        try {
            applicationPath = (new File(applicationPath)).getCanonicalPath() + "/";
        } catch (IOException e) {
            // ignore
        }
        System.out.println(underIDE ? "* * * Is running with source" : "* * * Is not running with source");
        System.out.println("* * * Application path set to " + applicationPath);
    }

    public static String getApplicationPath() {
        return applicationPath;
    }

    public static Connection.ConnectionType getConnectionType() {
        return connectionType;
    }

    public static void setConnectionType(Connection.ConnectionType connectionType) {
        MainServlet.connectionType = connectionType;
    }

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        MainServlet.host = host;
    }

    public static String getDatabase() {
        return database;
    }

    public static void setDatabase(String database) {
        MainServlet.database = database;
    }

    public static String getUser() {
        return user;
    }

    public static void setUser(String user) {
        MainServlet.user = user;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        MainServlet.password = password;
    }

    public static void setApplicationPath(String applicationPath) {
        MainServlet.applicationPath = applicationPath;
    }

    public static boolean isUnderIDE() {
        return underIDE;
    }

    public static void setUnderIDE(boolean underIDE) {
        MainServlet.underIDE = underIDE;
    }

    static ComboPooledDataSource getCpds() {
        return cpds;
    }

    public static void setCpds(ComboPooledDataSource cpds) {
        MainServlet.cpds = cpds;
    }

    static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        MainServlet.debug = debug;
    }

    static boolean hasDatabase() {
        return hasDatabase;
    }

    public static void setHasDatabase(boolean hasDatabase) {
        MainServlet.hasDatabase = hasDatabase;
    }

    static String getAmazonApplicationId() {
        return amazonApplicationId;
    }

    public static void setAmazonApplicationId(String amazonApplicationId) {
        MainServlet.amazonApplicationId = amazonApplicationId;
    }

    public static void setMaxWorkerThreads(int maxWorkerThreads) {
        MainServlet.maxWorkerThreads = maxWorkerThreads;
    }

    public static String getSmtpHost() {
        return smtpHost;
    }

    public static void setSmtpHost(String smtpHost) {
        MainServlet.smtpHost = smtpHost;
    }

    public static String getSmtpUsername() {
        return smtpUsername;
    }

    public static void setSmtpUsername(String smtpUsername) {
        MainServlet.smtpUsername = smtpUsername;
    }

    public static String getSmtpPassword() {
        return smtpPassword;
    }

    public static void setSmtpPassword(String smtpPassword) {
        MainServlet.smtpPassword = smtpPassword;
    }
}
