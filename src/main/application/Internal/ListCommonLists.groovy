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
class ListCommonLists {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        List<Record> recs

        recs = db.fetchAll "select * from list_name where aid=? and pid is null order by name", servlet.aid
        if (recs.isEmpty()) {
            Response.respondAndReset servlet, "There are no public lists."
            return
        }

        String resp = "You have the following public lists:<br>"
        for (Record rec : recs)
            resp += " " + rec.getString("name") + "<br>"
        resp += "<br>"
        Response.respondAndReset servlet, resp
    }

}
