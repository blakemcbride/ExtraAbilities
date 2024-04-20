package Internal

import com.arahant.SendEmailAWS
import org.json.JSONObject
import org.kissweb.DateTime
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.MainServlet
import org.kissweb.rest.ProcessServlet

import java.util.List

/**
 * Author: Blake McBride
 * Date: 12/13/19
 */
class EmailList {

    private static final String mainIntent = "SendEmail"

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {

        Record rec = db.fetchOne("select * from account where aid=?", servlet.aid)
        if (rec.getString("email_verified") != "Y") {
            Response.respondAndReset servlet, "You do not have a verified email address to send the list to.  Please set your email address first by saying 'my email is'."
            return
        }

        String listName = servlet.getAttribute("listName")
        String listType = servlet.getAttribute("listType")
        String email = rec.getString("email")

        if (listName == "back"  ||  listName == "end") {
            Response.respondAndReset servlet, "Email function aborted."
            return
        }
        if (listName == 'help') {
            String resp = listLists(db, servlet)
            Response.buildDialog(servlet, mainIntent, "listName", resp + " What is the name of the list you wish to email to yourself?")
            return
        }

        if (listName != null  &&  !listName.isEmpty())
            listName = listName.replaceAll("'", "")   //  remove inconsistent apostrophes

        List<Record> recs
        if (listType == null  ||  listType.isEmpty()) {
            recs = db.fetchAll("select * from list_name where aid=? and name=?", servlet.aid, listName)
            if (recs == null  ||  recs.isEmpty()) {
                Response.buildDialog(servlet, mainIntent, "listName", "There is no list named '" + listName + "'.  What is the name of the list you wish to email?")
                return
            }
            if (recs.size() > 1) {
                Response.buildDialog(servlet, mainIntent, "listType", "Do you wish to email the public or private list named '" + listName + "'?")
                return
            }
            rec = recs.get(0)
        } else if (listType == "public") {
            rec = db.fetchOne("select * from list_name where aid=? and pid is null and name=?", servlet.aid, listName)
            if (rec == null) {
                servlet.forgetAttribute("listType")
                Response.buildDialog(servlet, mainIntent, "listName", "There is no public list named '" + listName + "'.  What is the name of the list you wish to email?")
                return
            }
        } else if (listType == "private") {
            rec = db.fetchOne("select * from list_name where aid=? and pid=? and name=?", servlet.aid, servlet.pid, listName)
            if (rec == null) {
                servlet.forgetAttribute("listType")
                Response.buildDialog(servlet, mainIntent, "listName", "There is no private list named '" + listName + "'.  What is the name of the list you wish to email?")
                return
            }
        } else if (listType == "help") {
            Response.buildDialog(servlet, mainIntent, "listType", "A public list is viable to all of the members of your family.  A private list is visable only to you. Do you wish to email the public or private list named '" + listName + "'?")
            return
        } else if (listType == "back"  ||  listType == "end") {
            Response.respondAndReset(servlet, "Exiting email function.")
            return
        } else {
            Response.buildDialog(servlet, mainIntent, "listType", "'" + listType + "' is invalid. Do you wish to email the public or private list named '" + listName + "'?")
            return
        }
        sendEmail(servlet, db, email, listName, listType, rec.getString("listid"))
    }

    private static void sendEmail(ProcessServlet servlet, Connection db, String addr, String listName, String listType, String listid) {
        boolean succeed = true
        SendEmailAWS em = null
        Record rec = db.fetchOne("select timezone from account where aid=?", servlet.aid)
        String timezone = rec.getString("timezone")
        try {
            List<Record> recs = db.fetchAll "select seq, item from list_detail where listid=? order by seq", listid
            String resp
            if (recs.isEmpty())
                if (listType != null  &&  !listType.isEmpty())
                    resp = listType.capitalize() + " list '" + listName + "' is empty."
                else
                    resp = listName + " list is empty."
            else {
                if (listType != null  &&  !listType.isEmpty())
                    resp = listType.capitalize() + " list '" + listName + "' as of " + DateTime.currentDateTimeFormatted("MMM dd, yyyy  hh:mm a zzz", timezone == null ? "US/Central" : timezone) + "<br><br>"
                else
                    resp = listName + " list as of " + DateTime.currentDateTimeFormatted("MMM dd, yyyy  hh:mm a zzz", timezone == null ? "US/Central" : timezone) + "<br><br>"
                recs.each {
                    resp += it.getShort("seq") + ". " + it.getString("item") + "<br>"
                }
            }

            if (timezone == null)
                resp += "<br><br><br>Your time zone is not configured.  Defaulting to Central.  You can set your time zone by saying 'set my time zone'<br>"

            resp = resp + "<br><br><br><small>This list was provided by the Extra Abilities Alexa skill.</small><br>"
            resp = resp + "<small>For data recovery purposes, your Extra Abilities User Code is ${servlet.aid}</small><br>"

            em = new SendEmailAWS(MainServlet.getSmtpHost(), MainServlet.getSmtpUsername(), MainServlet.getSmtpPassword())
            em.setHTMLMessage(resp)
            em.sendEmail "DO_NOT_REPLY@arahant.com", "Alexa Extra Abilities Skill", addr, "", listName + " list"
        } catch (Exception e) {
            succeed = false
        } finally {
            if (em != null)
                try {
                    em.close()
                } catch (Exception e) {
                    succeed = false
                }
        }
        if (succeed)
            Response.respondAndReset servlet, "'" + listName + "' list has been emailed to you."
        else
            Response.respondAndReset servlet, "There was an error emailing the list."
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
