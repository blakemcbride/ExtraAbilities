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
class Who {

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

            if (m.match(rest, /^do you know me as$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.WhatIsMyName"))
            else if (m.match(rest, /^do you think I am$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.WhatIsMyName"))
            else if (m.match(rest, /^am I$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.WhatIsMyName"))
            else if (m.match(rest, /^(wrote|created) extra abilities$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.WhoIsTheAuthor"))
            else if (m.match(rest, /^(wrote|created) this (skill|software)$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.WhoIsTheAuthor"))
            else if (m.match(rest, /^is the author of this (skill|software)$/))
                servlet.runGroovy(injson, outjson, servlet.rememberAttribute("type", "Internal.WhoIsTheAuthor"))
            else {
                Response.requestNotUnderstood("who", rest)
                Response.respondAndReset(servlet, "I'm sorry, I did not understand that.")
            }
        } else {
            servlet.rememberAttribute(servlet.getAttribute("restType"), rest)
            servlet.runGroovy(injson, outjson, type)
        }
    }
}
