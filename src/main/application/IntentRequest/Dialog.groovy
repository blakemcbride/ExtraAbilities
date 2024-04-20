package IntentRequest

import org.json.JSONObject
import org.kissweb.Matcher
import org.kissweb.Response
import org.kissweb.WordToNumber
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

import java.util.List

/**
 * Author: Blake McBride
 * Date: 12/8/19
 */
class Dialog {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        String request = servlet.getSlot 'request'
        String intent, arg1, arg2

        (intent, arg1, arg2) = parse(request)

        switch (intent) {
            case 'edit list':
                Response.forward(servlet, "EditListContents", "listName", "Which list do you wish to edit?")
                break
            case 'list lists':
                servlet.runGroovy(injson, outjson, "Internal.ListMyLists")
                //listLists(injson, outjson, db, servlet)
                break
            case 'fake person':
                boolean error = false
                try {
                    servlet.setFakePersonId((int)WordToNumber.parseWordToLong(arg1))
                } catch (Exception e) {
                    error = true
                }
                if (error)
                    Response.respondAndReset(servlet, "I could not parse your argument.")
                else
                    Response.respondAndReset(servlet, "Fake person " + arg1 + " has been set.")
                break;
            default:
                Response.requestNotUnderstood("Dialog", request)
                Response.buildDialog(servlet, Dialog.class.getSimpleName(), "request", "I do not understand that.  How may I assist you?")
                break
        }


    }

    private static List parse(String str) {
        Matcher m = new Matcher()

        if (m.match(str, /^(what lists do you have|what lists are there|what lists do I have|list my lists)$/))
            return ["list lists", null, null]
        if (m.match(str, /^edit list$/))
            return ["edit list", null, null]
        if (m.match(str, /^set fake person to (.+)$/))
            return ["fake person", m.matchingSegment(0), null]
        return [null, null, null]
    }

    static void listLists(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        List<Record> recs
        int npers=0, ncommon=0
        String resp = ""

        if (servlet.pid == null  ||  servlet.pid.isEmpty())
            recs = db.fetchAll "select * from list_name where aid=? and pid is null order by name", servlet.aid
        else
            recs = db.fetchAll "select * from list_name where aid=? and (pid=? or pid is null) order by name", servlet.aid, servlet.pid
        if (recs.isEmpty()) {
            Response.buildDialog(servlet, Dialog.class.getSimpleName(), "request", "You have no lists.")
            return
        }
        for (Record rec : recs) {
            String pid = rec.getString "pid"
            if (pid == null  ||  pid.isEmpty())
                ncommon++
            else
                npers++
        }
        if (npers > 0) {
            resp = "You have the following private lists:<br>"
            boolean addComma = false
            for (Record rec : recs) {
                String pid = rec.getString "pid"
                if (pid != null  &&  !pid.isEmpty())
                    resp += " " + rec.getString("name") + "<br>"
            }
            resp += "<br>"
        }

        if (ncommon == 0)
            resp += " You have no public lists."
        else {
            resp += " You have the following public lists:<br>"
            for (Record rec : recs) {
                String pid = rec.getString "pid"
                if (pid == null  ||  pid.isEmpty())
                    resp += " " + rec.getString("name") + "<br>"
            }
            resp += "<br>"
        }
        Response.buildDialog(servlet, Dialog.class.getSimpleName(), "request", resp)
    }
}
