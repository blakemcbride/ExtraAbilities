package Internal

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 12/7/19
 */
class ListContentsOfList {

    private static final boolean noPrivateLists = false

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        String listName = servlet.getAttribute 'listName'
        String listType = servlet.getAttribute 'listType'
        List<Record> recs, lrecs
        Record rec

        if (noPrivateLists) {
            servlet.rememberAttribute "listType", (listType="public")
        }

        recs = db.fetchAll "select listid from list_name where aid=?", servlet.aid
        if (recs.isEmpty()) {
            Response.respondAndReset servlet, "You have no lists to list."
            return
        }

        if (listName == null  ||  listName.isEmpty()) {
            Response.buildDialog(servlet, "List", "listName", "What is the name of the list you wish to list?")
            return
        }
        if (listName == "back"  ||  listName == "end") {
            Response.respondAndReset servlet, "Exiting list function."
            return
        }
        listName = listName.replaceAll("'", "")

        if (servlet.pid != null) {
            recs = db.fetchAll("select * from list_name where aid=? and (pid=? or pid is null) and name=?", servlet.aid, servlet.pid, listName)
            if (recs.isEmpty()) {
                Response.respondAndReset servlet, "You have no list named '" + listName + "'."
                return
            }
            if (recs.size() == 2  &&  (listType == null  ||  (listType != "public" && listType != "private"))) {
                Response.buildDialog(servlet, "List", "listType", "Do you wish to list the public or private list named '" + listName + "'?")
                return
            }
            if (listType == null  ||  listType.isEmpty()) {
                listType = recs.get(0).getString("pid") == null ? "public" : "private"
                servlet.rememberAttribute "listType", listType
            }
            if (recs.size() == 1)
                rec = recs.get(0)
            else
                rec = recs.get(recs.get(0).getString("pid") == null  &&  listType == "public" ? 0 : 1)
        } else {
            if (listType != null  &&  listType == "private") {
                Response.respondAndReset servlet, "I do not know who you are, therefore, I cannot list a private list."
                return
            }
            recs = db.fetchAll "select * from list_name where aid=? and name=?", servlet.aid, listName
            if (recs.isEmpty()) {
                Response.respondAndReset servlet, "There are no lists named '" + listName + "'."
                return
            } else if (recs.size() > 1  &&  listType == null) {
                Response.respondAndReset servlet, "There are multiple lists named '" + listName + "' but I do not know who you are."
                return
            }
            rec = recs.get(0)
            if (rec.getString("pid") != null) {
                Response.respondAndReset servlet, noPrivateLists ? "There is no list named '" + listName + "'." : "There is no public list named '" + listName + "'."
                return
            }
            listType = "public"
            servlet.rememberAttribute "listType", listType
        }


        lrecs = db.fetchAll("select * from list_detail where listid=? order by seq", rec.getString("listid"))
        String resp = ""
        if (lrecs.isEmpty())
            Response.respondAndReset(servlet, "List " + listName + " is empty.")
        else {
            resp = "List " + listName + "<br>"
            lrecs.each {
                resp += it.getShort("seq") + ". " + it.getString("item") + "<br>"
            }
            resp += "<br>"
            Response.respondAndReset servlet, resp
        }

    }

}
