package Internal

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 12/15/19
 */
class GetTimeZone {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Record rec = db.fetchOne("select * from account where aid=?", servlet.aid)
        String timezone = rec.getString("timezone")

        if (timezone == null  ||  timezone.isEmpty())
            Response.respondAndReset servlet, "I have no timezone for you.  You can tell me by saying 'my timezone is'."
        else
            Response.respondAndReset servlet, "Your timezone is set to " + timezone + "."
    }

}
