package IntentRequest

import org.json.JSONObject
import org.kissweb.Response
import org.kissweb.database.Connection
import org.kissweb.rest.ProcessServlet

/**
 * Author: Blake McBride
 * Date: 12/12/19
 */
class HowDo {

    static void main(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet) {
        String doWhat = servlet.getSlot("rest")
        switch (doWhat) {
            case "create a new list":
            case "create a list":
                Response.respondAndReset servlet, "You can create a new list by saying 'create list'."
                break
            case "rename a list":
            case "change a lists name":
            case "change a list's name":
                Response.respondAndReset servlet, "You can change the name of a list by saying 'rename list'."
                break
            case "find out what lists I have":
            case "get a list of what lists I have":
            case "get a list of the lists I have":
                Response.respondAndReset servlet, "You can get a list of the lists you have by saying 'what lists do I have'."
                break
            case "delete a list":
            case "remove a list":
                Response.respondAndReset servlet, "You can delete a new list by saying 'delete list'."
                break
            case "display the contents of a list":
            case "get the contents of a list":
            case "echo the contents of a list":
            case "find what is in the list":
            case "get what is in the list":
            case "find what is on the list":
            case "get what is on the list":
                Response.respondAndReset servlet, "You can get the contents of a list by saying 'what is on list'."
                break
            case "edit a list":
            case "modify a list":
            case "change a list":
            case "add items to a list":
            case "delete an item from a list":
            case "delete an item":
            case "reorder an item":
            case "move an item":
            case "change an item on a list":
                Response.respondAndReset servlet, "Before changing, or deleting items on a list, you need to be editing the list.  To do that, you say 'Edit list'."
                break
            case "add to a list":
                Response.respondAndReset servlet, "You can add to a list by saying 'add to list' or 'add to end of list'.  You can also add to a list in list edit mode."
                break
            case "print a list":
            case "print my list":
            case "email a list":
            case "email my list":
            case "email a list to myself":
                Response.respondAndReset servlet, "You first need to set your email address by saying 'my email is'.  You can thereafter email lists to yourself by saying 'email list'."
                break
            case "set my time zone":
            case "configure my time zone":
                Response.respondAndReset servlet, "You set your time zone by saying 'set my time zone'."
                break
            case "tell what my time zone is set to":
                Response.respondAndReset servlet, "You find out what your time zone is set to by saying 'what is my time zone'."
                break
            default:
                Response.requestNotUnderstood("How do I", doWhat)
                Response.respondAndReset servlet, "I do not have any helpful comments about that."
                break
        }
    }

}
