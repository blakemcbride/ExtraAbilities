package IntentRequest

import org.json.JSONObject
import org.kissweb.Matcher
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 12/30/19
 */
class Create {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        /*  Global slots
                 rest              what the person said
            Global attributes
                 type              the class that gets the message
                 restType          the attribute rest goes in

        */
        String rest = servlet.getSlot("rest")
        String type = servlet.getAttribute("type")

        if (type == null) {  // first time in - need to parse
            Matcher m = new Matcher()
            
            // List
            
            if (m.match(rest, /^(a |my |the )?list$/)) {
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.CreateList"))
            } else if (m.match(rest, /^(a |my |the )?list (named|called) (.+)$/)) {
                servlet.rememberAttribute("listName", m.matchingSegment(2))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.CreateList"))
            } else if (m.match(rest, /^(a |my |the )?list (.+)$/)) {
                servlet.rememberAttribute("listName", m.matchingSegment(1))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.CreateList"))
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
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.CreateList"))
            } else 
            
                // Journal
            
            if (m.match(rest, /^(a |my |the )?journal$/)) {
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.CreateJournal"))
            } else if (m.match(rest, /^(a |my |the )?journal (named|called) (.+)$/)) {
                servlet.rememberAttribute("journalName", m.matchingSegment(2))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.CreateJournal"))
            } else if (m.match(rest, /^(a |my |the )?journal (.+)$/)) {
                servlet.rememberAttribute("journalName", m.matchingSegment(1))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.CreateJournal"))
            } else if (m.match(rest, /^(a |my |the )?(public|private|personal|family) journal$/)) {
                switch (m.matchingSegment(1)) {
                    case "public":
                    case "family":
                    case "group":
                        servlet.rememberAttribute("journalType", "public")
                        break
                    case "private":
                    case "personal":
                        servlet.rememberAttribute("journalType", "private")
                        break
                }
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.CreateJournal"))
            } else {
                Response.requestNotUnderstood("create", rest)
                Response.respondAndReset(servlet, "I'm sorry, I did not understand that.")
            }
        } else {
            servlet.rememberAttribute(servlet.getAttribute("restType"), rest)
            servlet.runGroovy(injson, outjson, type)
        }
    }

}
