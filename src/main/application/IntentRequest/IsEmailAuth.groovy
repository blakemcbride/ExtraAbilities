package IntentRequest

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

/**
 * Author Blake McBride
 * Date: 12/14/19
 */
class IsEmailAuth {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Record rec = db.fetchOne("select * from account where aid=?", servlet.aid)
        String email = rec.getString("email")
        short nverif = rec.getShort("email_nverif")
        if (rec.getString("email_verified") != "Y")
            if (email == null  ||  email.isEmpty())
                Response.respondAndReset servlet, "No email address has been entered.  You can do this by saying 'my email is'."
            else if (nverif >= 3)
                Response.respondAndReset servlet, "You have exceeded the number of verification attempts. You must re-enter your email address. You can do this by saying 'my email is'."
            else
                Response.respondAndReset servlet, "Your email address has not yet been verified. You can do this by saying 'my email verification code is'."
        else
            Response.respondAndReset servlet, "Yes, email address " + email + " has been verified."
    }

}
