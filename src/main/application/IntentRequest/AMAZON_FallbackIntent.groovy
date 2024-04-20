package IntentRequest

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 11/11/19
 */
class AMAZON_FallbackIntent {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Response.respondAndReset servlet, "I am sorry, I do not understand that.  You may want to say 'Help, or Exit' to exit Extra Abilities."
    }

}
