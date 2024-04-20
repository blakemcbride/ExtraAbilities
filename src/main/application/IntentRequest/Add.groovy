package IntentRequest

import org.json.JSONObject
import org.kissweb.Matcher
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.rest.ProcessServlet


/**
 * Author: Blake McBride
 * Date: 12/15/19
 */
class Add {

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
            } else if (m.match(rest, /^to list$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else if (m.match(rest, /^to (my |the )?list (.*)$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                servlet.rememberAttribute("listName", m.matchingSegment(1))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else if (m.match(rest, /^to (the )?(top|bottom|beginning|end) of list$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                switch (m.matchingSegment(1)) {
                    case "top":
                    case "beginning":
                        servlet.rememberAttribute("newLocation", "top")
                        break
                    case "bottom":
                    case "end":
                        servlet.rememberAttribute("newLocation", "bottom")
                        break
                }
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else if (m.match(rest, /^to (the )?(top|bottom|beginning|end) of list (.*)$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                switch (m.matchingSegment(1)) {
                    case "top":
                    case "beginning":
                        servlet.rememberAttribute("newLocation", "top")
                        break
                    case "bottom":
                    case "end":
                        servlet.rememberAttribute("newLocation", "bottom")
                        break
                }
                servlet.rememberAttribute("listName", m.matchingSegment(2))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else if (m.match(rest, /^to (my |the )?(.*) list$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                servlet.rememberAttribute("listName", m.matchingSegment(1))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else if (m.match(rest, /^(.*) to list$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                servlet.rememberAttribute("item", m.matchingSegment(0))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else if (m.match(rest, /^(.*) to (bottom|top|beginning|end) of list$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                servlet.rememberAttribute("item", m.matchingSegment(0))
                switch (m.matchingSegment(1)) {
                    case "top":
                    case "beginning":
                        servlet.rememberAttribute("newLocation", "top")
                        break
                    case "bottom":
                    case "end":
                        servlet.rememberAttribute("newLocation", "bottom")
                        break
                }
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else if (m.match(rest, /^(.*) to list (.*)$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                servlet.rememberAttribute("item", m.matchingSegment(0))
                servlet.rememberAttribute("listName", m.matchingSegment(1))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else if (m.match(rest, /^(.*) to (bottom|top|beginning|end) of list (.*)$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                servlet.rememberAttribute("item", m.matchingSegment(0))
                switch (m.matchingSegment(1)) {
                    case "top":
                    case "beginning":
                        servlet.rememberAttribute("newLocation", "top")
                        break
                    case "bottom":
                    case "end":
                        servlet.rememberAttribute("newLocation", "bottom")
                        break
                }
                servlet.rememberAttribute("listName", m.matchingSegment(2))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else if (m.match(rest, /^to (public|private|family|group|personal) list$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                switch (m.matchingSegment(0)) {
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
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else if (m.match(rest, /^to (public|private|family|group|personal) list (.*)$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                switch (m.matchingSegment(0)) {
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
                servlet.rememberAttribute("listName", m.matchingSegment(1))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else if (m.match(rest, /^(.*) to (public|private|family|group|personal) list$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                servlet.rememberAttribute("item", m.matchingSegment(0))
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
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else if (m.match(rest, /^(.*) to (public|private|family|group|personal) list (.*)$/)) {
                servlet.rememberAttribute("action", "add")
                servlet.rememberAttribute("fromExternal", "true")
                servlet.rememberAttribute("item", m.matchingSegment(0))
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
                servlet.rememberAttribute("listName", m.matchingSegment(2))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.EditListContents"))
            } else

            // Journal

            if (m.match(rest, /^(a |my |the )?(journal|diary|log)$/)) {
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.CreateJournal"))
            } else if (m.match(rest, /^(a |my |the )?(journal|diary|log) (named|called) (.+)$/)) {
                servlet.rememberAttribute("journalName", m.matchingSegment(3))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.CreateJournal"))
            } else if (m.match(rest, /^(a |my |the )?(journal|diary|log) (.+)$/)) {
                servlet.rememberAttribute("journalName", m.matchingSegment(2))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.CreateJournal"))
            } else if (m.match(rest, /^(a |my |the )?(public|private|personal|family) (journal|diary|log)/)) {
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
            } else if (m.match(rest, /^to (my |the )?(public |private |personal |family )?(journal|diary|log)( .+)?$/)) {
            } else {
                Response.requestNotUnderstood("add", rest)
                Response.respondAndReset(servlet, "I'm sorry, I did not understand that.")
            }
        } else {
            servlet.rememberAttribute(servlet.getAttribute("restType"), rest)
            servlet.runGroovy(injson, outjson, type)
        }
    }

}
