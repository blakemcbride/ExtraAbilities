package Internal

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 11/17/19
 */
class CreateList {

    private static final String mainIntent = "Create"

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String listName = servlet.getAttribute 'listName'
        String listType = servlet.getAttribute "listType"

        if (listType == null  ||  listType.isEmpty()) {
            Response.buildDialog(servlet, mainIntent, "listType", "Is this a public or private list?")
            return
        }
        if (listType == "back") {
            Response.respondAndReset servlet, "Exiting create list function."
            return
        }
        if (listType == 'help') {
            String help = "A public list is a list applicable to all members of your family.  A private list is one only applicable to you. "
            Response.buildDialog(servlet, mainIntent, "listType", help + "Is this a public or private list?")
            return
        }
        if (listType.startsWith("public"))
            servlet.rememberAttribute("listType", listType="public");
        if (listType.startsWith("private"))
            servlet.rememberAttribute("listType", listType="private");
        if (listType != "public"  &&  listType != "private") {
            Response.buildDialog(servlet, mainIntent, "listType", listType + " is invalid. Is this a public or private list?")
            return
        }
        if (listType == "private"  &&  servlet.pid == null) {
            Response.respondAndReset servlet, "I cannot add a private list because I do not know who you are.  Please tell me who you are first by saying 'My name is, followed by your name.'"
            return
        }

        if (listName == null  ||  listName.isEmpty()) {
            Response.buildDialog(servlet, mainIntent, "listName", "What is the name of the " + listType + " list you wish to add?")
            return
        }
        if (listName == "back") {
            Response.respondAndReset servlet, "Exiting create list function."
            return
        }

        listName = listName.replaceAll("'", "")   //  remove inconsistent apostrophes

        if (listType == "public") {
            Record rec = db.fetchOne "select * from list_name where aid=? and pid is null and name=?", servlet.aid, listName
            if (rec != null) {
                Response.respondAndReset servlet, "Public list '" + listName + "' already exists."
                return;
            }
            rec = db.newRecord "list_name"
            rec.set "listid", UUID.randomUUID().toString()
            rec.set "aid", servlet.aid
            rec.set "name", listName
            rec.addRecord()
            Response.respondAndReset servlet, "Public list '" + listName + "' has been added."

            // go to auto-add mode
            servlet.forgetAllSlots()
            servlet.forgetAllAttributes()
            servlet.rememberAttribute "listName", listName
            servlet.rememberAttribute "publicPrivate", "public"
            servlet.rememberAttribute "action", "add"
            servlet.rememberAttribute "fromExternal", "true"
            Response.buildDialog(servlet, "Open", "EditListContents", "item", "What item would you like to add?")
            return
        }

        // private list

        if (servlet.pid == null) {
            Response.respondAndReset servlet, "I cannot add a private list because I do not know who you are.  Please tell me who you are first by saying 'My name is ...'"
            return
        }
        Record rec = db.fetchOne "select * from list_name where aid=? and pid=? and name=?", servlet.aid, servlet.pid, listName
        if (rec != null) {
            Response.respondAndReset servlet, "Private list '" + listName + "' already exists."
            return;
        }
        rec = db.newRecord "list_name"
        rec.set "listid", UUID.randomUUID().toString()
        rec.set "aid", servlet.aid
        rec.set "pid", servlet.pid
        rec.set "name", listName
        rec.addRecord()
        Response.respondAndReset servlet, "Private list '" + listName + "' has been added."
    }

}
