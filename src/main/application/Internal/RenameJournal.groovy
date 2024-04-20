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
class RenameJournal {

    private static final String mainIntent = "Rename"

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        String fromName = servlet.getAttribute 'fromName'
        String toName = servlet.getAttribute 'toName'
        String confirm = servlet.getAttribute "confirm"
        String journalType = servlet.getAttribute "journalType"
        List<Record> recs
        Record rec

        recs = db.fetchAll "select jid from journal_name where aid=?", servlet.aid
        if (recs.isEmpty()) {
            Response.respondAndReset servlet, "You have no journals to rename."
            return
        }

        if (fromName == null || fromName.isEmpty()) {
            Response.buildDialog(servlet, mainIntent, "fromName", "What is the name of the journal you wish to rename?")
            return
        }
        if (fromName == "back") {
            Response.respondAndReset servlet, "Exiting rename."
            return
        }
        fromName = fromName.replaceAll("'", "")

        recs = db.fetchAll "select * from journal_name where aid=? and name=?", servlet.aid, fromName
        if (recs.isEmpty()) {
            Response.buildDialog(servlet, mainIntent, "fromName", "There is no journal named '" + fromName + "'. What is the name of the journal you wish to rename?")
            return
        }
        rec = recs.get(0)

        if (toName == null || toName.isEmpty()) {
            Response.buildDialog(servlet, mainIntent, "toName", "What is the new name of the journal?")
            return
        }
        if (toName == "back" || toName == "end") {
            Response.respondAndReset servlet, "Rename aborted."
            return
        }
        if (toName == "help") {
            Response.buildDialog(servlet, mainIntent, "toName", "This will be the new name of the journal.  What is the new name of the journal?")
            return
        }
        toName = toName.replaceAll("'", "")

        rec = db.fetchOne("select * from journal_name where aid=? and name=?", servlet.aid, toName)
        if (rec != null) {
            Response.buildDialog(servlet, mainIntent, "toName", "Journal '" + toName + "' already exists.  What is the new name of the journal?")
            return
        }


        if (confirm == null || confirm != "yes" && confirm != "no") {
            Response.buildDialog(servlet, mainIntent, "confirm", "Are you sure you wish to rename " + journalType + " list '" + fromName + "' to '" + toName + "'?")
            return
        }
        if (confirm != "yes") {
            Response.respondAndReset servlet, journalType + " list '" + fromName + "' has not been renamed."
            return
        }

        db.execute "update journal_name set name=? where aid=? and name=?", toName, servlet.aid, fromName
        Response.respondAndReset servlet, "Journal '" + fromName + "' has been renamed to '" + toName + "'."
    }
}
