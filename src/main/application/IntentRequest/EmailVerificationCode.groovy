package IntentRequest

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 12/13/19
 */
class EmailVerificationCode {

    private static final short MAX_EMAIL_VERIF = 3


    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        Record rec = db.fetchOne("select * from account where aid=?", servlet.aid)
        if (rec.getString("email_verified") == "Y") {
            Response.respondAndReset servlet, "Your email address has already been verified.  If you with to change your email address, say 'set my email'."
            return
        }
        String email = rec.getString("email")
        if (email == null  ||  email.isEmpty()) {
            Response.respondAndReset servlet, "You have not set up any email address to be verified.  You can set your email address by saying 'set my email'."
            return
        }
        short nverif = rec.getShort("email_nverif")
        if (nverif >= MAX_EMAIL_VERIF) {
            Response.respondAndReset servlet, "You have exceeded the max number of email verification attempts.  You will have to reset your email address.  You can do this by saying 'set my email'."
            return
        }

        String vc = servlet.getSlot 'verifCode'
        vc = vc.replaceAll(" ", "")
        vc = vc.replaceAll("-", "")
        vc.replaceAll("oh", "0")
        vc.replaceAll("o", "0")

        if (vc == rec.getString("email_verification_code")) {
            rec.set("email_verified", "Y")
            rec.update()
            Response.respondAndReset servlet, "Your email address has been verified."
        } else {
            rec.set("email_nverif", ++nverif)
            rec.update()
            if (nverif >= MAX_EMAIL_VERIF) {
                Response.respondAndReset servlet, "Your verification code is invalid and you have exceeded the max number of email verification attempts.  You will have to reset your email address.  You can do this by saying 'set my email'."
            } else {
                Response.respondAndReset servlet, "Your verification code is invalid.  You may try again by saying 'my email verification code is'."
            }
        }
    }
}
