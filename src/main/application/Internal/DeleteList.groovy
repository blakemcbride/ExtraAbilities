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
class DeleteList {

    private static final String mainIntent = "Delete"

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        String listName = servlet.getAttribute 'listName'
        String listType = servlet.getAttribute 'listType'
        String confirm  = servlet.getAttribute "confirm"
        List<Record> recs
        Record rec

        recs = db.fetchAll "select listid from list_name where aid=?", servlet.aid
        if (recs.isEmpty()) {
            Response.respondAndReset servlet, "You have no lists to delete."
            return
        }

        if (listName == null  ||  listName.isEmpty()) {
            Response.buildDialog(servlet, mainIntent, "listName", "What is the name of the list you wish to delete?")
            return
        }
        if (listName == "back") {
            Response.respondAndReset servlet, "Exiting list delete function."
            return
        }
        if (listName == "help"  ||  listName == "list") {
            String resp = listLists(db, servlet)
            Response.buildDialog(servlet, mainIntent, "listName", resp + "  What is the name of the list you wish to delete?")
            return
        }
        listName = listName.replaceAll("'", "")   //  remove inconsistent apostrophes

        if (servlet.pid != null) {
            recs = db.fetchAll("select * from list_name where aid=? and (pid=? or pid is null) and name=?", servlet.aid, servlet.pid, listName)
            if (recs.isEmpty()) {
                Response.respondAndReset servlet, "You have no list named '" + listName + "'."
                return
            }
            if (recs.size() == 2  &&  (listType == null  ||  (listType != "public" && listType != "private"))) {
                Response.buildDialog(servlet, mainIntent, "listType", "Do you wish to delete the public or private list named '" + listName + "'?")
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
                Response.respondAndReset servlet, "I do not know who you are, therefore, I cannot delete a private list."
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
                Response.respondAndReset servlet, "There is no public list named '" + listName + "'."
                return
            }
            listType = "public"
            servlet.rememberAttribute "listType", listType
        }

        if (confirm == null  ||  confirm != "yes"  &&  confirm != "no") {
            Response.buildDialog(servlet, mainIntent, "confirm", "Are you sure you wish to delete " + listType +  " list '" + listName + "'?")
            return
        }
        if (confirm != "yes") {
            Response.respondAndReset servlet, listType + " list '" + listName + "' has not been deleted."
            return
        }

/*
        String confirmation = JsonPath.getString injson, "request.intent.confirmationStatus"
        if (confirmation == "DENIED") {
            Response.respond outjson, "List " + listName + " has NOT been deleted."
        } else {

 */
            /*
            if (listType == "public")
                rec = db.fetchOne "select listid from list_name where aid=? and pid is null and name=?", servlet.aid, listName
            else
                rec = db.fetchOne "select listid from list_name where aid=? and pid=? and name=?", servlet.aid, servlet.pid, listName
             */
            String listid = rec.getString "listid"
            db.execute "delete from list_detail where listid=?", listid
            db.execute "delete from list_name where listid=?", listid
            Response.respondAndReset servlet, listType.capitalize() + " list '" + listName + "' has been deleted."
//        }
    }

    private static String listLists(Connection db, ProcessServlet servlet) {
        List<Record> recs
        int npers=0, ncommon=0
        String resp = ""

        if (servlet.pid == null  ||  servlet.pid.isEmpty())
            recs = db.fetchAll "select * from list_name where aid=? and pid is null order by name", servlet.aid
        else
            recs = db.fetchAll "select * from list_name where aid=? and (pid=? or pid is null) order by name", servlet.aid, servlet.pid
        if (recs.isEmpty())
            return "You have no lists."
        for (Record rec : recs) {
            String pid = rec.getString "pid"
            if (pid == null  ||  pid.isEmpty())
                ncommon++
            else
                npers++
        }
        if (npers > 0) {
            resp = "You have the following personal lists:<br>"
            for (Record rec : recs) {
                String pid = rec.getString "pid"
                if (pid != null  &&  !pid.isEmpty())
                    resp += " " + rec.getString("name") + "<br>"
            }
            resp += "<br>"
        }

        if (ncommon == 0)
            resp += " You have no public lists."
        else {
            resp += " You have the following public lists:<br>"
            for (Record rec : recs) {
                String pid = rec.getString "pid"
                if (pid == null  ||  pid.isEmpty())
                    resp += " " + rec.getString("name") + "<br>"
            }
            resp += "<br>"
        }
        return resp
    }
}
