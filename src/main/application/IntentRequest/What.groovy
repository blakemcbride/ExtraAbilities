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
class What {

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

            if (m.match(rest, /^is my name$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.WhatIsMyName"))
            else if (m.match(rest, /^is my email( address)?$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.WhatEmail"))
            else if (m.match(rest, /^is email (address )?do you have( for me)?$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.WhatEmail"))
            else if (m.match(rest, /^is my time zone( set to)?$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.GetTimeZone"))
            else if (m.match(rest, /^time zone am (I|a) (in|set to|do I have set)$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.GetTimeZone"))
            else if (m.match(rest, /^(lists|list) do (I|you|a) have$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListMyLists"))
            else if (m.match(rest, /^(lists|list) are there$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListMyLists"))
            else if (m.match(rest, /^(public|common) (lists|list) do (I|you|a) have$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListCommonLists"))
            else if (m.match(rest, /^(public|common) (lists|list) ( are there)?$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListCommonLists"))
            else if (m.match(rest, /^(personal|private) (lists|list) do (I|you|a) have$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListPrivateLists"))
            else if (m.match(rest, /^(public|common) (lists|list) do (I|you|a) have$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListCommonLists"))
            else if (m.match(rest, /^journals are there$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListMyJournals"))
            else if (m.match(rest, /^journals do (I|you|a) have$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListMyJournals"))
            else if (m.match(rest, /^is (in|on) (the|my) list$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListContentsOfList"))
            else if (m.match(rest, /^is (in|on) list (.+)$/)) {
                servlet.rememberAttribute("listName", m.matchingSegment(1))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListContentsOfList"))
            } else if (m.match(rest, /^is (in|on) (.+) list$/)) {
                    servlet.rememberAttribute("listName", m.matchingSegment(1))
                    servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.ListContentsOfList"))
            } else {
                Response.requestNotUnderstood("what", rest)
                Response.respondAndReset(servlet, "I'm sorry, I did not understand that.")
            }
        } else {
            servlet.rememberAttribute(servlet.getAttribute("restType"), rest)
            servlet.runGroovy(injson, outjson, type)
        }
    }

}
