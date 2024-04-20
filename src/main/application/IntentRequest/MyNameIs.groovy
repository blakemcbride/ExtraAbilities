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
class MyNameIs {


    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        String name = servlet.getSlot 'name'

        if (servlet.pid == null) {
            Response.respondAndReset servlet, "I cannot remember your name until you train your voice with Alexa.  You can do this by saying 'exit', and then saying 'learn my voice'.  After that, you can then come back into Extra Abilities and tell me your name."
        } else {
            Record rec = db.fetchOne("select * from person where pid=?", servlet.pid)
            if (rec == null) {
                rec = db.newRecord("person")
                rec.set "person_id", servlet.personId
                rec.set "pid", servlet.pid
                rec.set "aid", servlet.aid
                rec.set "name", name
            } else {
                rec.set "name", name
                rec.update()
            }
            Response.respondAndReset servlet, "Okay, ${name}. I will remember that."
        }
    }

}
