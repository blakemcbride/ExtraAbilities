package IntentRequest

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 11/23/19
 */
class OpenExtraAbilities {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Response.respondAndReset servlet, "You are already in Extra Abilities."
    }

}
