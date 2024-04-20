package Internal

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 12/22/19
 */
class EditJournalContents {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Response.respondAndReset(servlet, "This facility is not yet available.  Check back in a few days.")
    }

}
