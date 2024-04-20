package IntentRequest

import org.json.JSONObject
import org.kissweb.Matcher
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 12/31/19
 */
class SendEmail {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        /*  Global slots
                 rest              what the person said
            Global attributes
                 type              the class that gets the message
                 restType          the attribute rest goes in

        */
        String rest = servlet.getSlot("rest")
        String type = servlet.getAttribute("type")

        if (type == null) {  // first time in
            Matcher m = new Matcher()

            // List

            if (m.match(rest, /^(a |my |the )?list$/)) {
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EmailList"))
            } else if (m.match(rest, /^(a |my |the )?list (named |called )?(.+)$/)) {
                servlet.rememberAttribute("listName", m.matchingSegment(2))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EmailList"))
            } else if (m.match(rest, /^(a |my |the )?(public|private|personal|family) list$/)) {
                switch (m.matchingSegment(1)) {
                    case "public":
                    case "family":
                    case "group":
                        servlet.rememberAttribute("listType", "public")
                        break
                    case "private":
                    case "personal":
                        servlet.rememberAttribute("listType", "private")
                        break
                }
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EmailList"))
            } else {
                Response.requestNotUnderstood("SendEmail", rest)
                Response.respondAndReset(servlet, "I'm sorry, I did not understand that.")
            }
        } else {
            servlet.rememberAttribute(servlet.getAttribute("restType"), rest)
            servlet.runGroovy(injson, outjson, type)
        }
    }

}
