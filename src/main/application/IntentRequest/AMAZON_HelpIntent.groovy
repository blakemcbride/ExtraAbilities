package IntentRequest

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 11/23/19
 */
class AMAZON_HelpIntent {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Response.respondAndReset servlet, "You can say things like 'What lists do I have. Create list.  Edit list, and so on.  You can also ask the question 'How do I'."
    }

}
