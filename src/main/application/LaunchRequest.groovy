import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet
import org.kissweb.rest.RestCall

/**
 * Author: Blake McBride
 * Date: 11/11/19
 */
class LaunchRequest {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        boolean newUser = injson.getBoolean("newUser")
        String resp
        if (newUser) {
            resp = "Welcome to Extra Abilities. "
            resp += "Looks like this is your first visit.  I'll explain a couple of things you can do and keep in mind. "
            resp += "With this skill you can keep lists.  You can also do advanced things like email your list to yourself, re-order lists, and other advanced features. "
            resp += "At any prompt you can say 'back' to go back to to the previous function or end the function. "
            resp += "You can also often say 'help' to get help."
        } else {

            resp = "Welcome back to Extra Abilities.  It's good to speak with you again."

            if (servlet.pid != null) {
                Record rec = db.fetchOne("select name from person where pid=?", servlet.pid)
                if (rec != null)  {
                    String name = rec.getString("name")
                    if (name != null  &&  !name.isEmpty())
                        resp = "Welcome back to Extra Abilities " + name + ". It's good to speak with you again."
                }
            }
        }
        Response.respondAndReset servlet, resp

        // get the timezone of their device and save it in their attributes
        String tz = RestCall.getService("https://api.amazonalexa.com/v2/devices/" + servlet.deviceId + "/settings/System.timeZone",
                "application/json", "Bearer " + servlet.apiAccessToken, null)
        servlet.rememberAttribute("timeZone", tz)
    }

}
