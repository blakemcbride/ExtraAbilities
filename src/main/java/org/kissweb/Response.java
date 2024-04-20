package org.kissweb;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kissweb.database.Record;
import org.kissweb.rest.ProcessServlet;

import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Author: Blake McBride
 * Date: 11/13/19
 *
 * This class converts responses into the appropriate JSON.
 *
 * Within response texts, "<br>" is treated differently for SSML (what is spoken) and text (what is displayed) as follows:
 *
 * SSML:  ,
 * Text:  \n
 */
public class Response {

    private static final String LOG_FILE_NAME = "../logs/FailedRequests.log";
    private static final String PROMPT = "How may I assist you?";
    private static final String SLOT = "rest";  //  the single slot used to get a user's answer

    public static boolean respondAndReset(ProcessServlet servlet, String sayStr, String displayStr) {

        servlet.forgetAllAttributes();
        servlet.forgetAllSlots();

        String resp = build(servlet, false, sayStr, displayStr);

        JSONObject outjson = servlet.getOutjson();

//        Map<String,String> slotMap = servlet.getSlotMap();
//        if (slotMap == null)
        outjson.put("response", new JSONObject(resp));
//        else
//            outjson.put("response", JsonTemplate.fill(resp, slotMap));

        servlet.forgetAllAttributes();  // but remembers personId
        Map<String,String> attributeMap = servlet.getAttributeMap();
        outjson.put("sessionAttributes", attributeMap);

        return true;  // EXIT
    }

    public static boolean respondAndReset(ProcessServlet servlet, String str1) {

        servlet.forgetAllAttributes();
        servlet.forgetAllSlots();

        String resp = build(servlet, false, str1, str1);

        JSONObject outjson = servlet.getOutjson();

//        Map<String,String> slotMap = servlet.getSlotMap();
//        if (slotMap == null)
        outjson.put("response", new JSONObject(resp));
//        else
//            outjson.put("response", JsonTemplate.fill(resp, slotMap));

        servlet.forgetAllAttributes();  // but remembers personId
        Map<String,String> attributeMap = servlet.getAttributeMap();
        outjson.put("sessionAttributes", attributeMap);

        return true;  // EXIT
    }

    /** This method can only be used to exit Extra Abilities!
     *
     * @param outjson
     * @param promptStr
     * @return
     */
    public static boolean respondAndExit(JSONObject outjson, String promptStr) {
        String resp = build(null, true, promptStr, promptStr);
        outjson.put("response", new JSONObject(resp));
        return true;  // EXIT
    }

    private static String processSSML(String str) {
        str = str.replaceAll("<br>", ". ");
        str = str.replaceAll("\\. +\\. *", ". ");
        str = str.replaceAll("\\. *\\.", ".");
        str = str.replaceAll("\\.,", ".");
        str = str.replaceAll("(\\d+)\\.", "$1,");
        return str;
    }

    private static String processText(String str) {
        return str.replaceAll("<br>", "\n");
    }

    private static String personChanged(ProcessServlet servlet) {
        if (servlet == null  ||  !servlet.personChanged)
            return "";
        try {
            Record rec = servlet.DB.fetchOne("select name from person where pid=?", servlet.pid);
            if (rec == null)
                return "";
            String name = rec.getString("name");
            if (name == null  ||  name.isEmpty())
                return "";
            return "Oh, hello " + name + ". ";
        } catch (SQLException e) {
            return "";
        }
    }

    private static String build(ProcessServlet servlet, boolean endSession, String sayStr, String displayStr) {
        String pc = personChanged(servlet);
        if (displayStr == null)
            displayStr = sayStr;
        sayStr = pc + sayStr;
        displayStr = pc + displayStr;
        StringBuilder sb = new StringBuilder("{");
        sb.append("outputSpeech: { type: \"SSML\", ssml: \"<speak>" + processSSML(sayStr + (endSession ? "" : " " + PROMPT)) + "</speak>\"}");
        sb.append(",card: { type: \"Simple\", content: \"" + processText(displayStr) + "\"}");
        sb.append(",reprompt: { outputSpeech: { type: \"SSML\", ssml: \"<speak>" + processSSML(sayStr) + "</speak>\"}}");
        sb.append(",shouldEndSession: " + endSession);
        sb.append(",type: \"_DEFAULT_RESPONSE\"");
        sb.append("}");
        return sb.toString();
    }

    private static JSONObject buildSlots(ProcessServlet servlet) {
        JSONObject jobj = new JSONObject();
        for (String key : servlet.getSlotMap().keySet()) {
            String val = servlet.getSlot(key);
            JSONObject tobj = new JSONObject();
            tobj.put("name", key);
            if (val == null  ||  val.isEmpty())
                tobj.put("confirmationStatus", "NONE");
            else {
                tobj.put("confirmationStatus", "CONFIRMED");
                tobj.put("value", val);
            }
            jobj.put(key, tobj);
        }
        return jobj;
    }

    private static JSONArray buildDirectives(String intentName, ProcessServlet servlet) {
        ProcessServlet.logMessage("buildDirectives: intentName = " + intentName);
        servlet.forgetSlot(SLOT);
        JSONArray directives = new JSONArray();
        JSONObject directive = new JSONObject();
        directive.put("type", "Dialog.ElicitSlot");
        directive.put("slotToElicit", SLOT);
        JSONObject updatedIntent = new JSONObject();
        updatedIntent.put("name", intentName);
        updatedIntent.put("confirmationStatus", "NONE");
        updatedIntent.put("slots", buildSlots(servlet));
        directive.put("updatedIntent", updatedIntent);
        directives.put(directive);
        ProcessServlet.logMessage("buildDirectives: directive = " + directives.toString(4));
        return directives;
    }

    public static boolean buildDialog(ProcessServlet servlet, String intentName, String attributeToElicit, String query) {
        ProcessServlet.logMessage("buildDialog: intentName = " + intentName);
        query = personChanged(servlet) + query;
        servlet.forgetSlot(SLOT);
        servlet.forgetAttribute(attributeToElicit);
        servlet.rememberAttribute("restType", attributeToElicit);
        JSONObject outjson = servlet.getOutjson();
        JSONObject response = new JSONObject();
        response.put("outputSpeech", new JSONObject("{type: \"SSML\", ssml: \"<speak>" + processSSML(query) + "</speak>\" }"));
        response.put("card", new JSONObject("{ type: \"Simple\", content: \"" + processText(query) + "\"}"));
        response.put("shouldEndSession", false);
        response.put("directives", buildDirectives(intentName, servlet));
        outjson.put("response", response);
        outjson.put("sessionAttributes", servlet.getAttributeMap());
        return true;  // EXIT
    }

    public static boolean buildDialog(ProcessServlet servlet, String intentName, String internalClass, String attributeToElicit, String query) {
        servlet.rememberAttribute("type", "Internal." + internalClass);
        return buildDialog(servlet, intentName, attributeToElicit, query);
    }

    public static boolean forward(ProcessServlet servlet, String intentName, String attributeToElicit, String query) {
        query = personChanged(servlet) + query;
        JSONObject outjson = servlet.getOutjson();
        servlet.forgetSlot("request");
        servlet.forgetSlot(SLOT);
        servlet.forgetAttribute(attributeToElicit);
        servlet.rememberAttribute("restType", attributeToElicit);
        JSONObject response = new JSONObject();
        response.put("outputSpeech", new JSONObject("{type: \"SSML\", ssml: \"<speak>" + processSSML(query) + "</speak>\" }"));
        response.put("shouldEndSession", false);
        response.put("directives", buildDirectives(intentName, servlet));
        outjson.put("response", response);
        outjson.put("sessionAttributes", servlet.getAttributeMap());
        return true;  // EXIT
    }

    public static synchronized void requestNotUnderstood(String where, String request) {
        try {
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            String strDate = dateFormat.format(date);

            File file = new File(LOG_FILE_NAME);
            FileWriter fw = new FileWriter(file, true);
            fw.write(strDate + "  " + where + " - " + request + "\n");
            fw.close();
        } catch (Exception e) {

        }
    }

    public static void main(String [] argv) {
        String s = "1. one<br>2. two<br>3. three<br><br> <br>How can I help you?";
        String ssml = processSSML(s);
        String txt = processText(s);
        int i = 0;
    }

}
