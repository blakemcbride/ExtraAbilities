package Internal

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 12/21/19
 */
class CreateJournal {

    private static final String mainIntent = "Create"

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        String journalName = servlet.getAttribute 'journalName'
        String journalType = servlet.getAttribute 'journalType'

        if (journalName == null || journalName.isEmpty()) {
            Response.buildDialog(servlet, mainIntent, "journalName", "What is the name of the journal you wish to create?")
            return
        }
        if (journalName == "back") {
            Response.respondAndReset servlet, "Exiting create journal function."
            return
        }
        journalName = journalName.replaceAll("'", "")   //  remove inconsistent apostrophes

        if (journalType == null || journalType.isEmpty()) {
            Response.buildDialog(servlet, mainIntent, "journalType", "Is this a public or private journal?")
            return
        }
        if (journalName == "back") {
            Response.respondAndReset servlet, "Exiting create journal function."
            return
        }

        if (journalType != "private"  &&  journalType != "public")
            if (servlet.pid == null  ||  servlet.pid.is())
                servlet.rememberAttribute("journalType", journalType="public")
            else {
                Response.buildDialog(servlet, mainIntent, "journalType", "I'm sorry, is this a public or private journal?")
                return
            }

        if (journalType == "private"  &&  (servlet.pid == null  ||  servlet.pid.isEmpty())) {
            Response.respondAndReset servlet, "I cannot create a private journal because I do not know who you are. Please tell me who you are first by saying 'My name is, followed by your name.'"
            return
        }

        Record rec

        if (journalType == "private") {
            rec = db.fetchOne "select * from journal_name where aid=? and pid=? and name=?", servlet.aid, servlet.pid, journalName
            if (rec != null) {
                Response.respondAndReset servlet, "Private journal '" + journalName + "' already exists."
                return
            }
        } else {
            rec = db.fetchOne "select * from journal_name where aid=? and pid is null and name=?", servlet.aid, journalName
            if (rec != null) {
                Response.respondAndReset servlet, "Public journal '" + journalName + "' already exists."
                return
            }
        }
        rec = db.newRecord "journal_name"
        rec.set "jid", UUID.randomUUID().toString()
        rec.set "aid", servlet.aid
        rec.set "pid", servlet.pid
        rec.set "name", journalName
        rec.addRecord()
        Response.respondAndReset servlet, "Journal '" + journalName + "' has been created."
    }

}
