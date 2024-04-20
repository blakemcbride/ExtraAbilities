package Internal

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 12/14/19
 */
class WhatEmail {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        Record rec = db.fetchOne("select * from account where aid=?", servlet.aid)
        String email = rec.getString("email")

        if (email == null  ||  email.isEmpty())
            Response.respondAndReset servlet, "I have no email address for you.  You can tell me by saying 'my email is'."
        else if (rec.getString("email_verified") != "Y")
            if (rec.getShort("email_nverif") >= 3)
                Response.respondAndReset servlet, "Your unverified email address is " + email + ".  You have exceeded the allowable number of email verification attempts so you will have to set your password again.  You can do this by saying 'my email is'."
            else
                Response.respondAndReset servlet, "Your unverified email address is " + email + ".  It still needs to be verified.  You should have been sent an email with the instructions."
        else
            Response.respondAndReset servlet, "Your verified email address is " + email + "."
    }

}
