package Internal

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

import java.util.List

/**
 * Author: Blake McBride
 * Date: 12/21/19
 */
class DeleteJournal {

    private static final String mainIntent = "Delete"

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        String journalName = servlet.getAttribute 'journalName'
        String confirm  = servlet.getAttribute "confirm"
        List<Record> recs
        Record rec

        recs = db.fetchAll "select jid from journal_name where aid=?", servlet.aid
        if (recs.isEmpty()) {
            Response.respondAndReset servlet, "You have no journals to delete."
            return
        }

        if (journalName == null  ||  journalName.isEmpty()) {
            Response.buildDialog(servlet, mainIntent, "journalName", "What is the name of the journal you wish to delete?")
            return
        }
        if (journalName == "back") {
            Response.respondAndReset servlet, "Exiting journal delete function."
            return
        }
        if (journalName == "help"  ||  journalName == "list") {
            String resp = listjournals(db, servlet)
            Response.buildDialog(servlet, mainIntent, "journalName", resp + "  What is the name of the journal you wish to delete?")
            return
        }
        journalName = journalName.replaceAll("'", "")   //  remove inconsistent apostrophes

        recs = db.fetchAll "select * from journal_name where aid=? and name=?", servlet.aid, journalName
        if (recs.isEmpty()) {
            Response.respondAndReset servlet, "There are no journals named '" + journalName + "'."
            return
        }
        rec = recs.get(0)

        if (confirm == null  ||  confirm != "yes"  &&  confirm != "no") {
            Response.buildDialog(servlet, mainIntent, "confirm", "Are you sure you wish to delete journal '" + journalName + "'?")
            return
        }
        if (confirm != "yes") {
            Response.respondAndReset servlet, "Journal '" + journalName + "' has not been deleted."
            return
        }

        String jid = rec.getString "jid"
        db.execute "delete from journal_detail where jid=?", jid
        db.execute "delete from journal_name where jid=?", jid
        Response.respondAndReset servlet, "Journal '" + journalName + "' has been deleted."
    }

    private static String listjournals(Connection db, ProcessServlet servlet) {
        List<Record> recs
        String resp

        recs = db.fetchAll "select * from journal_name where aid=? order by name", servlet.aid
        if (recs.isEmpty())
            return "You have no journals"
        resp = "You have the following journals:<br>"

        for (Record rec : recs)
            resp += " " + rec.getString("name") + "<br>"
        return resp
    }

}
