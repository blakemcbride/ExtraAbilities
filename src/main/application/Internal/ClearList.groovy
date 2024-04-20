package Internal

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

import java.util.List

/**
 * Author: Blake McBride
 * Date: 12/18/19
 */
class ClearList {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String name = servlet.getAttribute("name")            //  the name of the item to be cleared
        String confirm = servlet.getAttribute("confirm")

        if (name == null  ||  name.isEmpty()) {
            Response.buildDialog(servlet, "Clear", "name", "What is the name of the list you wish to clear?")
            return
        }
        if (name == 'help'  ||  name == 'list') {
            String resp = listLists(db, servlet)
            Response.buildDialog(servlet, "Clear", "name", resp + " What is the name of the list to clear?")
            return
        }
        Record rec = db.fetchOne("select * from list_name where aid=? and pid is null and name=?", servlet.aid, name)
        if (rec == null) {
            Response.respondAndReset(servlet, "List '" + name + "' does not exist.")
            return
        }

        if (confirm == null  ||  confirm != "yes"  &&  confirm != "no") {
            Response.buildDialog(servlet, "Clear", "confirm", "Are you sure you wish to clear list '" + name + "'?")
            return
        }
        if (confirm == "yes") {
            db.execute("delete from list_detail where listid=?", rec.getString("listid"))
            Response.respondAndReset(servlet, "List '" + name + "' has been cleared.")
        } else
            Response.respondAndReset(servlet, "List '" + name + "' has not been cleared.")
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
