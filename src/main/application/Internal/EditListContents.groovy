package Internal

import org.json.JSONObject
import org.kissweb.Matcher
import org.kissweb.Response
import org.kissweb.WordToNumber
import org.kissweb.database.Connection
import org.kissweb.database.Record
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 11/17/19
 */
class EditListContents {

    private static final boolean noPrivateLists = false
    private static final String mainIntent = "Open"
    private static final String internalClass = "EditListContents"

    private JSONObject injson
    private JSONObject outjson
    private Connection db
    private ProcessServlet servlet

    private String listName
    private String publicPrivate
    private String action
    private String item
    private String itemNumber
    private String newLocation
    private String listid
    private String operation, arg1, arg2
    private String fromExternal
    private String newName
    private String areYouSure

    private static final boolean EXIT = true                // exit the processing of this run of the intent (the function determines whether to leave the intent or not)
    private static final boolean CONTINUE = false           // continue in the intent


    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        (new EditListContents()).run(injson, outjson, db, servlet)
    }

    private void run(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        this.injson = injson
        this.outjson = outjson
        this.db = db
        this.servlet = servlet

        listName = servlet.getAttribute 'listName'
        publicPrivate = servlet.getAttribute 'publicPrivate'
        action = servlet.getAttribute 'action'
        item = servlet.getAttribute 'item'
        itemNumber = servlet.getAttribute 'itemNumber'
        newLocation = servlet.getAttribute "newLocation"
        fromExternal = servlet.getAttribute "fromExternal"
        newName = servlet.getAttribute "newName"
        areYouSure = servlet.getAttribute "areYouSure"

        if (noPrivateLists) {
            servlet.rememberAttribute "publicPrivate", (publicPrivate="public")
        }

        if (anyLists())
            return
        if (getListName())
            return
        if (getPublicPrivate())
            return
        if (getAction())
            return
        if (performAction())
            return
    }

    private boolean anyLists() {
        List<Record> recs;

        // any lists at all?
        if (servlet.pid == null  ||  servlet.pid.isEmpty())
            recs = db.fetchAll "select * from list_name where aid=? and pid is null", servlet.aid
        else
            recs = db.fetchAll "select * from list_name where aid=? and (pid=? or pid is null)", servlet.aid, servlet.pid
        if (recs.isEmpty())
            return Response.respondAndReset(servlet, "There are no lists to edit.")
        return CONTINUE
    }

    private boolean getListName() {
        // get list name
        if (listName == null  ||  listName.isEmpty())
            return Response.buildDialog(servlet, mainIntent, "listName", "Which list do you wish to edit?")
        if (listName == "back"  ||  listName == 'end')
            return Response.respondAndReset(servlet, "List edit complete.")
        if (listName == "help") {
            String help = listLists db, servlet
            return Response.buildDialog(servlet, mainIntent, "listName", help + ". Which list do you wish to edit?")
        }
        listName = listName.replaceAll("'", "")   //  remove inconsistent apostrophes

        List<Record> recs
        if (servlet.pid == null  ||  servlet.pid.isEmpty())
            recs = db.fetchAll "select * from list_name where aid=? and name=?", servlet.aid, listName
        else
            recs = db.fetchAll "select * from list_name where aid=? and (pid=? or pid is null) and name=?", servlet.aid, servlet.pid, listName
        if (recs == null  ||  recs.isEmpty())
            return Response.buildDialog(servlet, mainIntent, "listName", "There is no list named '" + listName + "'. Which list do you wish to edit?")
        return CONTINUE
    }

    private boolean performAction() {
        switch (operation) {
            case "add":
                return performAdd()
            case "list":
                return performList()
            case "delete":
                return performDelete()
            case "edit":
                return performEdit()
            case "move":
                return performMove()
            case "which":
                return performWhich()
            case 'rename':
                return performRename()
            case "clear list":
                return performClearList()
            default:
                return getActionPrompt(operation + " is not a valid response.  Please say 'help' if you need help.")
        }
    }

    private boolean getPublicPrivate() {
        List<Record> recs

        if (publicPrivate == null  ||  publicPrivate.isEmpty()) {
            if (servlet.pid == null  ||  servlet.pid.isEmpty())
                recs = db.fetchAll "select * from list_name where aid=? and name=?", servlet.aid, listName
            else
                recs = db.fetchAll "select * from list_name where aid=? and (pid=? or pid is null) and name=?", servlet.aid, servlet.pid, listName
            if (recs.size() > 1)
                return Response.buildDialog(servlet, mainIntent, "publicPrivate", "Do you wish to edit the public or private list named '" + listName + "'?")
            if (recs.isEmpty()) {
                /*
                ProcessServlet.logMessage("Can't find list " + listName)
                ProcessServlet.logMessage("Have lists:")
                recs = db.fetchAll "select * from list_name where aid=? order by list_name desc", servlet.aid
                recs.forEach {
                    String pid = it.getString "pid"
                    ProcessServlet.logMessage "    " + it.getString("name") + " (" + (pid == null  ||  pid.isEmpty() ? "public" : "private") + ")"
                }
                 */
                return Response.buildDialog(servlet, mainIntent, "listName", "There is no list named '" + listName + "'. Please state the list name again.")
            }
            String rpid = recs.get(0).getString("pid")
            publicPrivate = rpid == null  ||  rpid.isEmpty() ? "public" : "private"
            servlet.rememberAttribute("publicPrivate", publicPrivate)
            listid = recs.get(0).getString("listid")
        } else {
            if (publicPrivate == "back")
                return Response.respondAndReset(servlet, "Exiting list edit.")
            if (publicPrivate == "help")
                return Response.buildDialog(servlet, mainIntent, "publicPrivate", "Respond with 'public' or 'private'. Do you wish to edit the public or private list named " + listName)
            if (publicPrivate == "common") {
                publicPrivate = "public"
                servlet.rememberAttribute("publicPrivate", publicPrivate)
            }
            if (!publicPrivate.startsWith("public")  &&  !publicPrivate.startsWith("private"))
                return Response.buildDialog(servlet, mainIntent, "publicPrivate", "Is this the public or private list named " + listName)
            if (publicPrivate.startsWith("public")) {
                recs = db.fetchAll "select * from list_name where aid=? and pid is null and name=?", servlet.aid, listName
                if (recs.isEmpty())
                    return Response.respondAndReset(servlet, noPrivateLists ? "There is no list named " + listName : "There is no public list named " + listName)
            } else {
                recs = db.fetchAll "select * from list_name where aid=? and pid=? and name=?", servlet.aid, servlet.pid, listName
                if (recs.isEmpty())
                    return Response.respondAndReset(servlet, "There is no private list named " + listName)
            }
            listid = recs.get(0).getString("listid")
        }
        return CONTINUE
    }

    private boolean getActionPrompt(String pre) {
        servlet.forgetAttribute "item"
        servlet.forgetAttribute "itemNumber"
        servlet.forgetAttribute "newLocation"
        servlet.forgetAttribute "newName"
        if (pre == null  ||  pre.isEmpty())
            Response.buildDialog(servlet, mainIntent, internalClass, "action", "What action do you wish to perform on " + publicPrivate + " list '" + listName + "'?")
        else
            Response.buildDialog(servlet, mainIntent, internalClass, "action", pre + " What action do you wish to perform on " + publicPrivate + " list '" + listName + "'?")
        return EXIT
    }

    private boolean getAction() {
        //  At this point we know which list in listid

        if (action == null  ||  action.isEmpty())
            return getActionPrompt(null)

        (operation, arg1, arg2) = parse(action)
        if (operation == null) {
            Response.requestNotUnderstood("EditListContents(action)", action)
            return getActionPrompt("Invalid action. Remember, you can always say 'help' or 'back'.")
        }

        if (operation == "back")
            return Response.respondAndReset(servlet, "Exiting list edit.")

        if (operation == "help")
            return getActionPrompt("You can add, change, move, delete, or list the contents of the list.")

        // to get around a bug in Alexa
        if (operation == "add"  &&  arg2 == "list") {
            operation = "list"
            arg2 = null
            servlet.rememberAttribute "action", operation
        }

        return CONTINUE
    }

    private boolean performAdd() {
        if (arg2 != null) {
            item = arg2
            servlet.rememberAttribute "item", item
        }
        if (item == null  ||  item.isEmpty()) {
            //ProcessServlet.logMessage("performAdd: mainIntent = " + mainIntent)
            return Response.buildDialog(servlet, mainIntent, "item", "What item would you like to add?")
        }
        if (item == "back"  ||  item == "nothing"  ||  item == "done"  ||  item == "end")
            if (fromExternal == "true")
                return Response.respondAndReset(servlet, "Done adding to list.")
            else
                return getActionPrompt(null)
        short seq
        Record rec
        if (arg1 == "bottom") {
            rec = db.fetchOne "select seq from list_detail where listid=? order by seq desc", listid
            seq = rec == null ? 1 : rec.getShort("seq") + 1
        } else {
            db.execute "update list_detail set seq=seq+1 where listid=?", listid
            seq = 1
        }
        rec = db.newRecord("list_detail")
        rec.set("listid", listid)
        rec.set("seq", seq)
        rec.set("item", item)
        rec.addRecord()

        if (fromExternal == "true")
            return Response.buildDialog(servlet, mainIntent, "item", "Item added.  What additional item would you like to add?")
        else
            return getActionPrompt("Item has been added.")
    }

    private boolean performRename() {
        if (arg1 != null)
            newName = arg1
        if (newName == null || newName.isEmpty()) {
            servlet.rememberAttribute "action", "rename"
            return Response.buildDialog(servlet, mainIntent, "newName", "What is the new list name?")
        }
        if (newName == "back")
            return getActionPrompt("Exiting rename.")
        if (newName == "help") {
            return Response.buildDialog(servlet, mainIntent, "newName", "The will be the new name you assign to list " + listName + ". What is the new list name?")
        }
        db.execute("update list_name set name=? where listid=?", newName, listid)
        servlet.rememberAttribute "listName", newName
        String resp = "List '" + listName + "' has been renamed '" + newName + "'."
        listName = newName
        return getActionPrompt(resp)
    }

    private boolean performClearList() {
        Record rec = db.fetchOne("select count(*) from list_detail where listid=?", listid)
        long count = rec.getLong("count")
        if (count == 0)
            return getActionPrompt("The list is already empty.")
        if (areYouSure == null || areYouSure.isEmpty()) {
            servlet.rememberAttribute "action", "clear list"
            return Response.buildDialog(servlet, mainIntent, "areYouSure", "Are you sure you wish to delete all items in list '" + listName + "'?")
        }
        if (newName == "help") {
            return Response.buildDialog(servlet, mainIntent, "areYouSure", "This action will remove all items in the list. Are you sure you wish to delete all items in list '" + listName + "'?")
        }
        if (newName == "back"  ||  areYouSure != 'yes')
            return getActionPrompt("Exiting list clear.")
        db.execute("delete from list_detail where listid=?", listid)
        return getActionPrompt("All items have been removed.")
    }

    private boolean performList() {
        List<Record> recs = db.fetchAll "select seq, item from list_detail where listid=? order by seq", listid
        String resp = ""
        if (recs.isEmpty())
            getActionPrompt("List '" + listName + "' is empty.")
        else {
            recs.each {
                resp += it.getShort("seq") + ". " + it.getString("item") + "<br>"
            }
            getActionPrompt(resp + "<br> <br>")
        }
        return EXIT
    }

    private boolean performWhich() {
        return getActionPrompt(noPrivateLists ? "This is list named '" + listName + "'." : "This is " + publicPrivate + " list named '" + listName + "'.")
    }

    private boolean performDelete() {
        int n
        Record rec
        if (arg1 == null  ||  arg1.isEmpty())
            arg1 = itemNumber
        if (arg1 == "back")
            return getActionPrompt("Delete function terminated.")
        if (arg1 != null  &&  !arg1.isEmpty()) {
            try {
                n = WordToNumber.parseWordToLong(arg1)
            } catch (Exception e) {
                rec = db.fetchOne("select * from list_detail where listid=? and item=?", listid, arg1)
                if (rec != null) {
                    n = rec.getShort("seq")
                } else {
                    servlet.rememberAttribute "action", "delete"
                    return Response.buildDialog(servlet, mainIntent, "itemNumber", arg1 + " is not an item or item number. What item do you wish to delete?")
                }
            }
        } else {
            servlet.rememberAttribute "action", "delete"
            return Response.buildDialog(servlet, mainIntent, "itemNumber", "What item do you wish to delete?")
        }
        rec = db.fetchOne "select seq from list_detail where listid=? order by seq desc", listid
        int nseq = rec == null ? 0 : rec.getShort("seq")
        if (nseq == 0)
            return getActionPrompt("The list is empty.")
        if (n < 1  ||  n > nseq) {
            servlet.rememberAttribute "action", "delete"
            return Response.buildDialog(servlet, mainIntent, "itemNumber", "Item " + n + " doesn't exist.  What item or item number do you wish to delete?")
        }
        db.execute "delete from list_detail where listid=? and seq=?", listid, n
        db.execute "update list_detail set seq=seq-1 where listid=? and seq > ?", listid, n
        return getActionPrompt("Item " + arg1 + " has been deleted.")
    }

    private boolean performMove() {
        Record rec = db.fetchOne("select count(*) from list_detail where listid=?", listid)
        long numrecs = rec.getLong("count")
        if (numrecs == 0)
            return getActionPrompt("There are no items to move.")
        if (numrecs == 1)
            return getActionPrompt("There is only one item.  Move doesn't make sense.")
        int n
        if (arg1 == null  ||  arg1.isEmpty())
            arg1 = itemNumber
        if (arg1 == null) {
            servlet.rememberAttribute "action", "move"
            return Response.buildDialog(servlet, mainIntent, "itemNumber", "What item or item number do you wish to move?")
        } else {
            if (arg1 == "back" ||  arg1 == "end"  ||  arg1 == "quit"  ||  arg1 == "never mind")
                return getActionPrompt("Aborting move.")
            try {
                n = WordToNumber.parseWordToLong(arg1)
            } catch (Exception e) {
                rec = db.fetchOne("select * from list_detail where listid=? and item=?", listid, arg1)
                if (rec != null) {
                    n = rec.getShort("seq")
                } else {
                    servlet.rememberAttribute "action", "move"
                    return Response.buildDialog(servlet, mainIntent, "itemNumber", arg1 + " is not a valid item or item number. What item number do you wish to move?")
                }
            }
        }
        if (n < 1  ||  n > numrecs) {
            servlet.rememberAttribute "action", "move"
            return Response.buildDialog(servlet, mainIntent, "itemNumber", arg1 + " is out of range. What item or item number do you wish to move?")
        }
        if (arg2 == null  ||  arg2.isEmpty())
            arg2 = newLocation
        if (arg2 == null  ||  arg2.isEmpty()) {
            servlet.rememberAttribute "action", "move"
            servlet.rememberAttribute "itemNumber", arg1
            return Response.buildDialog(servlet, mainIntent, "newLocation", "Where do you wish to move item " + arg1 + " to?")
        }
        if (arg2 == "back")
            return getActionPrompt(null)
        if (arg2 == "help") {
            servlet.rememberAttribute "action", "move"
            servlet.rememberAttribute "itemNumber", arg1
            return Response.buildDialog(servlet, mainIntent, "newLocation", "Choose 'to top' or 'to bottom'.  Where do you wish to move item " + arg1 + " to?")
        }
        if (arg2 == "not understood") {
            servlet.rememberAttribute "action", "move"
            servlet.rememberAttribute "itemNumber", arg1
            return Response.buildDialog(servlet, mainIntent, "newLocation", "I do not understand. Please choose 'to top' or 'to bottom'.  Where do you wish to move item " + arg1 + " to?")
        }
        if ((arg2.contains("top") || arg2.contains("beginning")) && n == 1)
            return getActionPrompt("Item " + arg1 + " is already at the top of the list.")
        if ((arg2.contains("bottom") || arg2.contains("end")) && n == numrecs)
            return getActionPrompt("Item " + arg1 + " is already at the bottom of the list.")
        rec = db.fetchOne("select * from list_detail where listid=? and seq=?", listid, n)
        String item = rec.getString("item")
        rec.delete()
        if (arg2.contains("top") || arg2.contains("beginning")) {
            db.execute("update list_detail set seq=seq+1 where listid=? and seq < ?", listid, n)
            rec = db.newRecord("list_detail")
            rec.set("listid", listid)
            rec.set("seq", 1)
            rec.set("item", item)
            rec.addRecord()
            return getActionPrompt("Item " + arg1 + " has been moved to the top of the list.")
        } else {
            db.execute("update list_detail set seq=seq-1 where listid=? and seq > ?", listid, n)
            rec = db.newRecord("list_detail")
            rec.set("listid", listid)
            rec.set("seq", numrecs)
            rec.set("item", item)
            rec.addRecord()
            return getActionPrompt("Item " + arg1 + " has been moved to the bottom of the list.")
        }
    }

    private boolean performEdit() {
        int n
        Record rec
        if (arg1 == null  ||  arg1.isEmpty())
            arg1 = itemNumber
        if (arg1 == 'back' || arg1 == 'end') {
            return getActionPrompt("Exiting edit.")
        }
        if (arg1 != null  &&  !arg1.isEmpty()) {
            try {
                n = WordToNumber.parseWordToLong(arg1)
            } catch (Exception e) {
                rec = db.fetchOne("select * from list_detail where listid=? and item=?", listid, arg1)
                println "rec is " + (rec == null ? "null" : "not null")
                if (rec != null) {
                    n = rec.getShort("seq")
                } else {
                    servlet.rememberAttribute "action", "edit"
                    return Response.buildDialog(servlet, mainIntent, "itemNumber", "'" + arg1 + "' is not an item or item number. What item or item number do you wish to edit?")
                }
            }
        } else {
            servlet.rememberAttribute "action", "edit"
            return Response.buildDialog(servlet, mainIntent, "itemNumber", "What item or item number do you wish to edit?")
        }
        rec = db.fetchOne "select seq from list_detail where listid=? order by seq desc", listid
        int nseq = rec == null ? 0 : rec.getShort("seq")
        if (nseq == 0)
            return getActionPrompt("The list is empty.")
        if (n < 1  ||  n > nseq) {
            servlet.rememberAttribute "action", "edit"
            return Response.buildDialog(servlet, mainIntent, "itemNumber", "Item '" + arg1 + "' doesn't exist.  What item number do you wish to edit?")
        }
        if (item == null  ||  item.isEmpty())
            return Response.buildDialog(servlet, mainIntent, "item", "What would you like item '" + arg1 + "' to be changed to?")
        db.execute "update list_detail set item=? where listid=? and seq=?", item, listid, n
        return getActionPrompt("Item '" + arg1 + "' has been changed to '" + item + "'.")
    }

    private static String listLists(Connection db, ProcessServlet servlet) {

        List<Record> recs
        int npers=0, ncommon=0
        String resp = ""

        if (servlet.pid == null  ||  servlet.pid.isEmpty())
            recs = db.fetchAll "select * from list_name where aid=? and pid is null order by name", servlet.aid
        else
            recs = db.fetchAll "select * from list_name where aid=? and (pid=? or pid is null) order by name", servlet.aid, servlet.pid
        if (recs.isEmpty()) {
            Response.respondAndReset servlet, "You have no lists."
            return ""
        }
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

    private static List parse(String str) {
        Matcher mr = new Matcher();

        if (mr.match(str, /^add (.*) (to the end of the list|to the beginning of the list|to the top of the list|to the bottom of the list|to the end|to the beginning|to the top|to the bottom|to end|to beginning|to top|to bottom)$/)) {
            String place = fix_place mr.matchingSegment(1)
            return ["add", place, fix_what(mr.matchingSegment(0))]
        }
        if (mr.match(str, /^add ?(to the top|to the bottom|to the beginning|to the end|to top|to bottom|to beginning|to end|top|bottom|beginning|end)? ?(.*)?/)) {
            String place = fix_place mr.matchingSegment(0)
            return ["add", place, fix_what(mr.matchingSegment(1))]
        }
        if (mr.match(str, /^(erase all items|delete all items|remove all items|clear list|clear)?$/)) {
            return ["clear list", null, null]
        }
        if (mr.match(str, /^(change|modify|edit|delete) ?(item )?(.*)?$/)) {
            String place = mr.matchingSegment(2)
            return [fix_operation(mr.matchingSegment(0)), fix_what(place), null]
        }
        if (mr.match(str, /^(back|end)$/)) {
            return ["back", null, null]
        }
        if (mr.match(str, /^help$/)) {
            return ["help", null, null]
        }
        if (mr.match(str, /^(list|help|back) ?(.*)?$/)) {
            return [mr.matchingSegment(0), null, null]
        }
        if (mr.match(str, /^(what's|what is) in (my|the) list$/)) {
            return ["list", null, null]
        }
        if (mr.match(str, /^(which|what) ?(.*)?$/)) {
            return ["which", null, null]
        }
        if (mr.match(str, /^(move)$/)) {
            return [mr.matchingSegment(0), null, null]
        }
        if (mr.match(str, /^(move)( item)? (.*) (to top|to bottom|to beginning|to end).*$/)) {
            String place = mr.matchingSegment(3)
            if (place == null)
                return [null, null, null]
            return [mr.matchingSegment(0), mr.matchingSegment(2), fix_place(place)]
        }
        if (mr.match(str, /^(move)( item)? (.*)$/)) {
            return [mr.matchingSegment(0), mr.matchingSegment(2), null]
        }
        if (mr.match(str, /^(change name|rename|rename list|change name of list)$/)) {
            return ['rename', null, null]
        }
        if (mr.match(str, /^(rename list to|rename list|rename this list|rename this list to|change name to|change the name of this list to) (.*)$/)) {
            return ["rename", mr.matchingSegment(1), null]
        }
        return [null, null, null]
    }
    
    private static String fix_operation(String s) {
        if (s == null || s.isEmpty())
            return s
        switch (s) {
            case 'change':
            case 'modify':
            case 'edit':
                return "edit"
            case 'delete':
                return "delete"
            default:
                return null
        }
    }

    private static String fix_place(String s) {
        if (s == null  ||  s.isEmpty())
            return null
        switch (s) {
            case "top":
            case "beginning":
            case "to top":
            case "to beginning":
            case "to the beginning":
            case "to the top":
            case "to the beginning of the list":
            case "to the top of the list":
                return "top"
            case "bottom":
            case "to end":
            case "to bottom":
            case "to the bottom":
            case "to the end":
            case "to the bottom of the list":
            case "to the end of the list":
                return "bottom"
            case "help":
                return "help"
            case "back":
                return "back"
            default:
                return "not understood"
        }
    }

    private static String fix_what(String s) {
        return (s == null  ||  s.isEmpty()) ? null : s;
    }
}
