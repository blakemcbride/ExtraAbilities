package IntentRequest

import org.json.JSONObject
import org.kissweb.Matcher
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 1/7/20
 */
class List {

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

            if (m.match(rest, /^my lists$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListMyLists"))
            else if (m.match(rest, /^(my )?(public|common) lists$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListCommonLists"))
            else if (m.match(rest, /^(my )?(personal|private) lists$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListPrivateLists"))
            else if (m.match(rest, /^contents of list$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListContentsOfList"))
            else if (m.match(rest, /^contents of list (.*)$/)) {
                servlet.rememberAttribute("listName", m.matchingSegment(0))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListContentsOfList"))
            } else if (m.match(rest, /^contents of (my |the )?(.*) list$/)) {
                    servlet.rememberAttribute("listName", m.matchingSegment(1))
                    servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListContentsOfList"))
            } else if (m.match(rest, /^(my )?journals$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListMyJournals"))
            else {
                Response.requestNotUnderstood("list", rest)
                Response.respondAndReset(servlet, "I'm sorry, I did not understand that.")
            }
        } else {
            servlet.rememberAttribute(servlet.getAttribute("restType"), rest)
            servlet.runGroovy(injson, outjson, type)
        }
    }
}
