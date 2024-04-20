
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
        MainServlet.setAmazonApplicationId "amzn1.ask.skill.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

        MainServlet.setSmtpHost "email-smtp.us-east-1.amazonaws.com"
        MainServlet.setSmtpUsername "xxxxxxxxxxxxxxxxxxxx"
        MainServlet.setSmtpPassword "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

        MainServlet.setDebug true                // if true print debug info
    }
}
