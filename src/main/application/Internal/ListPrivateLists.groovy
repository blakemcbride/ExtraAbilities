package Internal

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

import java.util.List

/**
 * Author: Blake McBride
 * Date: 11/17/19
 */
class ListPrivateLists {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        String resp

        if (servlet.pid == null || servlet.pid.isEmpty()) {
            Response.respondAndReset servlet, "I do not know who you are so I cannot track personal lists for you. You can train Alexa on your voice and tell me who you are for the future."
            return;
        }

        List<Record> recs = db.fetchAll "select * from list_name where aid=? and pid=? order by name", servlet.aid, servlet.pid
        if (recs.isEmpty()) {
            Response.respondAndReset servlet, "You have no private lists."
            return
        }

        resp = "You have the following private lists:<br>"
        for (Record rec : recs) {
            String pid = rec.getString "pid"
            if (pid != null && !pid.isEmpty())
                resp += " " + rec.getString("name") + "<br>"
        }
        resp += "<br>"
        Response.respondAndReset servlet, resp
    }

}
