package IntentRequest

import org.json.JSONObject
import org.kissweb.Matcher
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 1/8/20
 */
class Whats {

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

            if (m.match(rest, /^(in|on) (the|my) list$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListContentsOfList"))
            else if (m.match(rest, /^(in|on) list (.+)$/)) {
                servlet.rememberAttribute("listName", m.matchingSegment(1))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListContentsOfList"))
            } else if (m.match(rest, /^(in|on) (.+) list$/)) {
                servlet.rememberAttribute("listName", m.matchingSegment(1))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListContentsOfList"))
            } else {
                Response.requestNotUnderstood("what's", rest)
                Response.respondAndReset(servlet, "I'm sorry, I did not understand that.")
            }
        } else {
            servlet.rememberAttribute(servlet.getAttribute("restType"), rest)
            servlet.runGroovy(injson, outjson, type)
        }
    }

}
