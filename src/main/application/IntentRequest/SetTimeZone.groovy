package IntentRequest

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 12/15/19
 */
class SetTimeZone {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        String timezone = servlet.getSlot 'timezone'

        if (timezone == 'back'  ||  timezone == 'end') {
            Response.respondAndReset servlet, "Time zone set aborted."
            return
        }
        if (timezone == 'help' ||  timezone.startsWith("list")  ||  timezone.startsWith("what")) {
            String resp = "Acceptable time zones are as follows:<br>"
            resp += "  Eastern<br>"
            resp += "  Central<br>"
            resp += "  Mountain<br>"
            resp += "  Pacific<br>"
            resp += "  Alaska<br>"
            resp += "  Hawaii<br>"
            resp += "  Samoa.<br>"
            resp += "<br>What time zone do you wish to use?"
            Response.buildDialog(servlet, "SetTimezone", "timezone", resp)
            return
        }
        timezone = timezone.capitalize()
        switch (timezone) {
            case "Eastern":
            case "Central":
            case "Mountain":
            case "Pacific":
            case "Alaska":
            case "Hawaii":
            case "Samoa":
                break
            default:
                Response.buildDialog(servlet, "SetTimezone", "timezone", "'" + timezone + "' is an unacceptable time zone.  You can say 'help to get a list.  What time zone should I use?")
                return
        }

        Record rec = db.fetchOne("select * from account where aid=?", servlet.aid)
        rec.set("timezone", "US/" + timezone)
        rec.update()
        Response.respondAndReset servlet, "Your time zone has been set to " + timezone + "."
    }

}
