package IntentRequest

import com.arahant.SendEmailAWS
import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.MainServlet
import org.kissweb.rest.ProcessServlet

import java.security.SecureRandom

/**
 * Author: Blake McBride
 * Date: 12/13/19
 */
class SetEmail {


    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        String email = servlet.getSlot 'email'

        String email2 = email.replaceAll " at ", "@"
        email2 = email2.replaceAll " dot ", "."
        email2 = email2.replaceAll " ", ""
        email2 = email2.toLowerCase()

        int nat = 0
        int ndot = 0
        int max = email2.length()
        for (int i=0 ; i < max ; i++) {
            char c = email2.charAt(i)
            if (c == (char) '@')
                nat++
            else if (nat > 0  &&  c == (char) '.')
                ndot++
        }
        if (nat != 1  ||  ndot < 1) {
            Response.respondAndReset servlet, "Email address " + email + " is invalid.", "Email address '" + email2 + "' is invalid."
            return
        }

        String code = makeVerifCode()

        if (sendEmail(servlet, email2, code)) {
            Record rec = db.fetchOne("select * from account where aid=?", servlet.aid)
            rec.set("email", email2)
            rec.set("email_verified", "N")
            rec.set("email_verification_code", code)
            rec.set("email_nverif", 0)
            rec.update()

            Response.respondAndReset servlet, "An email with a verification code has been sent to '" + email + "'. Use that code to verify your email address.",
                    "An email with a verification code has been sent to '" + email2 + "'. Use that code to verify your email address."
        } else {
            Response.respondAndReset servlet, "There was an error sending your authentication email.  Please try again later."
        }
    }

    private static String makeVerifCode() {
        final String digits = "0123456789"
        String res = ""
        SecureRandom rnd = new SecureRandom()
        for (int i=0 ; i < 6 ; i++)
            res += digits[rnd.nextInt(10)]
        return res
    }

    private static boolean sendEmail(ProcessServlet servlet, String addr, String code) {
        SendEmailAWS em = null
        try {
            String msg = """\
Greetings from the Alexa Extra Abilities skill.<br><br>
KEEP THIS EMAIL<br><br>
We deeply appreciate your use of the Extra Abilities skill.  You will want to keep this email because the User Code supplied below
may be used for data recovery purposes in the future.<br><br>
To authorize your email address, say the following to Alexa:
<h2><b>Alexa, open Extra Abilities</b><h2>
<h2><b>My Email Verification Code is ${code}</b></h2><br><br>
Your Extra Abilities User Code is ${servlet.aid}<br>
Save this code in order to re-connect your data in case someone disables this ability.<br><br>
Thank you.<br><br>
Your Extra Abilities Team
"""
            em = new SendEmailAWS(MainServlet.getSmtpHost(), MainServlet.getSmtpUsername(), MainServlet.getSmtpPassword())
            em.setHTMLMessage(msg)
            em.sendEmail "DO_NOT_REPLY@arahant.com", "Alexa Extra Abilities Skill", addr, "", "Email Authorization Code"
        } catch (Exception e) {
            ProcessServlet.logMessage e, "Error sending email authentication"
            return false
        } finally {
            if (em != null)
                em.close()
        }
        return true
    }
}
