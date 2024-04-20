package Internal

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 11/12/19
 */
class WhatIsMyName {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        if (servlet.pid == null) {
            Response.respondAndReset servlet, "I cannot remember your name until you train your voice.  You can do this by saying exit, and then saying learn my voice.  After that, you can then come back into Extra Abilities and tell me your name."
        } else {
            boolean dontKnow
            String name = null
            Record rec = db.fetchOne("select * from person where pid=?", servlet.pid)
            if (rec == null) {
                dontKnow = true
            } else {
                name = rec.getString("name")
                dontKnow = name == null  ||  name.isEmpty()
            }
            if (dontKnow)
                Response.respondAndReset servlet, "I do not know your name.  You can tell me by saying 'my name is'."
            else
                Response.respondAndReset(servlet, "I know you as '${name}'.")
        }

     }

}
