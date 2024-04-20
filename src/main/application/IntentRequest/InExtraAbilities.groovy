package IntentRequest

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 11/11/19
 */
class InExtraAbilities {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Response.respondAndReset servlet, "Yes, you are still in Extra Abilities."
    }

}
