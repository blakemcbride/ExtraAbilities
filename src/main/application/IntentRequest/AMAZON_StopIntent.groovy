package IntentRequest

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 12/7/19
 */
class AMAZON_StopIntent {
    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        servlet.forgetAllAttributes()
        servlet.forgetAllSlots()
        Response.respondAndExit outjson, "Exiting Extra Abilities."
    }
}
