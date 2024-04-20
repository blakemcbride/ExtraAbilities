
import org.kissweb.database.Connection
import org.kissweb.rest.MainServlet

class KissInit {

    static void init() {
        MainServlet.setConnectionType Connection.ConnectionType.PostgreSQL
        MainServlet.setHost "localhost"
        MainServlet.setDatabase "extra_abilities"
        MainServlet.setUser "postgres"            // database user (not application user login)
        MainServlet.setPassword "postgres"        // database password (not application user password)
        MainServlet.setMaxWorkerThreads 40        // max number of simultaneous REST services (any additional are put on a queue)
        MainServlet.setAmazonApplicationId "amzn1.ask.skill.806640da-2a09-43cb-b1da-bac959fe3606"

        MainServlet.setSmtpHost "email-smtp.us-east-1.amazonaws.com"
        MainServlet.setSmtpUsername "AKIAJADABYZ7WHXJFXTQ"
        MainServlet.setSmtpPassword "AhNs13iGI525G2f4lulMFOaNUkYP90238CaDhBOqkv64"

        MainServlet.setDebug true                // if true print debug info
    }
}
