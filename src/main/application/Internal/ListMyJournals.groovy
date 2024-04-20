package Internal

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

import java.util.List

/**
 * Author: Blake McBride
 * Date: 12/21/19
 */
class ListMyJournals {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        List<Record> recs
        String resp = ""
        boolean hadSome = false

        if (servlet.pid != null  &&  !servlet.pid.isEmpty()) {
            recs = db.fetchAll "select * from journal_name where aid=? and pid=? order by name", servlet.aid, servlet.pid
            if (!recs.isEmpty()) {
                hadSome = true
                resp += "You have the following private lists:<br>"
                for (Record rec : recs)
                    resp += " " + rec.getString("name") + "<br>"
                resp += "<br>"
            }
        }

        recs = db.fetchAll "select * from journal_name where aid=? and pid is null order by name", servlet.aid
        if (!recs.isEmpty()) {
            hadSome = true
            resp += "You have the following public journals:<br>"
            for (Record rec : recs)
                resp += " " + rec.getString("name") + "<br>"
        }

        if (!hadSome) {
            Response.respondAndReset servlet, "You have no journals."
            return
        }

        Response.respondAndReset servlet, resp
    }

}
