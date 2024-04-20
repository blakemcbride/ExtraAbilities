package IntentRequest

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 11/12/19
 */
class ForgetMyName {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        boolean dontKnow
        if (servlet.pid == null)
            dontKnow = true
        else {
            Record rec = db.fetchOne("select * from person where pid=?", servlet.pid)
            if (rec == null)
                dontKnow = true
            else {
                String name = rec.getString("name")
                dontKnow = name == null  ||  name.isEmpty()
                if (!dontKnow) {
                    rec.set("name", "")
                    rec.update()
                }
            }
        }

        if (dontKnow)
            Response.respondAndReset servlet, "I do not know your name."
        else
            Response.respondAndReset servlet, "Your name has been forgotten"
    }

}
