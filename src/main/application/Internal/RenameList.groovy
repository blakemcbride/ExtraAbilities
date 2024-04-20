package Internal

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

import java.util.List

/**
 * Author: Blake McBride
 * Date: 11/17/19
 */
class RenameList {

    private static final boolean noPrivateLists = false
    private static final String mainIntent = "Rename"

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        String fromName = servlet.getAttribute 'fromName'
        String listType = servlet.getAttribute 'listType'
        String toName = servlet.getAttribute 'toName'
        String confirm = servlet.getAttribute "confirm"
        List<Record> recs
        Record rec

        if (noPrivateLists) {
            servlet.rememberAttribute "listType", (listType = "public")
        }

        recs = db.fetchAll "select listid from list_name where aid=?", servlet.aid
        if (recs.isEmpty()) {
            Response.respondAndReset servlet, "You have no lists to rename."
            return
        }

        if (fromName == null || fromName.isEmpty()) {
            Response.buildDialog(servlet, mainIntent, "fromName", "What is the name of the list you wish to rename?")
            return
        }
        if (fromName == "back") {
            Response.respondAndReset servlet, "Exiting rename."
            return
        }
        if (fromName == "help") {
            Response.buildDialog(servlet, mainIntent, "fromName", "This will be the old name of the list.  What is the name of the list you wish to rename?")
            return
        }
        fromName = fromName.replaceAll("'", "")

        if (servlet.pid != null) {  // known user
            recs = db.fetchAll("select * from list_name where aid=? and name=?", servlet.aid, fromName)
            if (recs.isEmpty()) {
                Response.respondAndReset servlet, "You have no list named " + fromName + "."
                return
            }
            if (recs.size() == 2) {
                if (listType == null || (listType != "public" && listType != "private")) {
                    Response.buildDialog(servlet, mainIntent, "listType", "Do you wish to rename the public or private list named " + fromName + "?")
                    return
                }
            } else if (listType == null || listType.isEmpty()) {
                listType = recs.get(0).getString("pid") == null ? "public" : "private"
                servlet.rememberAttribute "listType", listType
            } else if (listType == "public" && recs.get(0).getString("pid") != null) {
                Response.respondAndReset servlet, "There is no public list named " + fromName + "."
                return
            } else if (listType == "private" && recs.get(0).getString("pid") == null) {
                Response.respondAndReset servlet, "There is no private list named " + fromName + "."
                return
            }
        } else {  // anonymous person
            if (listType != null && listType == "private") {
                Response.respondAndReset servlet, "I do not know who you are, therefore, I cannot rename a private list."
                return
            }
            recs = db.fetchAll "select * from list_name where aid=? and name=?", servlet.aid, fromName
            if (recs.isEmpty()) {
                Response.buildDialog(servlet, mainIntent, "fromName", "There is no list named " + fromName + ". What is the name of the list you wish to rename?")
                return
            } else if (recs.size() > 1 && listType == null) {
                Response.respondAndReset servlet, "There are multiple lists named " + fromName + " but I do not know who you are."
                return
            }
            rec = recs.get(0)
            if (rec.getString("pid") != null) {
                Response.respondAndReset servlet, noPrivateLists ? "There is no list named " + fromName + "." : "There is no public list named " + fromName + "."
                return
            }
            listType = "public"
            servlet.rememberAttribute "listType", listType
        }


        if (toName == null || toName.isEmpty()) {
            Response.buildDialog(servlet, mainIntent, "toName", "What is the new name of the list?")
            return
        }
        if (toName == "back" || toName == "end") {
            Response.respondAndReset servlet, "Rename aborted."
            return
        }
        if (toName == "help") {
            Response.buildDialog(servlet, mainIntent, "toName", "This will be the new name of the list.  What is the new name of the list?")
            return
        }
        toName = toName.replaceAll("'", "")
        if (listType == "public") {
            rec = db.fetchOne("select * from list_name where aid=? and pid is null and name=?", servlet.aid, toName)
            if (rec != null) {
                Response.buildDialog(servlet, mainIntent, "toName", "Public list " + toName + " already exists.  What is the new name of the list?")
                return
            }
        } else {
            rec = db.fetchOne("select * from list_name where aid=? and pid=? and name=?", servlet.aid, servlet.pid, toName)
            if (rec != null) {
                Response.buildDialog(servlet, mainIntent, "toName", "Private list " + toName + " already exists.  What is the new name of the list?")
                return
            }
        }

        if (confirm == null || confirm != "yes" && confirm != "no") {
            Response.buildDialog(servlet, mainIntent, "confirm", "Are you sure you wish to rename " + listType + " list '" + fromName + "' to '" + toName + "'?")
            return
        }
        if (confirm != "yes") {
            Response.respondAndReset servlet, listType + " list '" + fromName + "' has not been renamed."
            return
        }

        if (listType == "public") {
            db.execute "update list_name set name=? where aid=? and pid is null and name=?", toName, servlet.aid, fromName
            Response.respondAndReset servlet, "Public list '" + fromName + "' has been renamed to '" + toName + "'."
        } else {
            db.execute "update list_name set name=? where aid=? and pid=? and name=?", toName, servlet.aid, servlet.pid, fromName
            Response.respondAndReset servlet, "Private list '" + fromName + "' has been renamed to '" + toName + "'."
        }
    }

}
