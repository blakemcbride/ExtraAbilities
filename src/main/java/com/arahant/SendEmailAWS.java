package com.arahant;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * User: Blake McBride
 * Date: 7/29/18
 *
 * In order for this to work, you do not need the underlying OS to be able to send email.  This works without the server needing
 * to be able to send email.  You don't need Postfix or SendMail.
 *
 * This module does depend on javax.mail-1.6.1.jar
 */
public class SendEmailAWS implements AutoCloseable {

    private Session session;
    private Transport transport;

    // Replace smtp_username with your Amazon SES SMTP user name.
    private String SMTP_USERNAME;

    // Replace smtp_password with your Amazon SES SMTP password.
    private String SMTP_PASSWORD;

    // Amazon SES SMTP host name. This example uses the US West (Oregon) region.
    // See http://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html#region-endpoints
    // for more information.
    private String HOST;

    // The port you will connect to on the Amazon SES SMTP endpoint.
    private int PORT = 25;

    private static class Attachment {
        String attachmentName;
        String diskFileName;
        byte [] byteArray;
        String type;

        Attachment(String diskFileName, String attachementName) {
            this.diskFileName = diskFileName;
            this.attachmentName = attachementName;
        }

        Attachment(byte [] data, String attachementName, String type) {
            this.byteArray = data;
            this.attachmentName = attachementName;
        }
    }

    private String message;
    private boolean isHTML;

    public SendEmailAWS setTextMessage(String txt) {
        message = txt;
        isHTML = false;
        return this;
    }

    public SendEmailAWS setHTMLMessage(String txt) {
        message = txt;
        isHTML = true;
        return this;
    }

    private List<Attachment> attachments = null;

    private static final String SUBJECT = "Amazon SES test 2";

    private static final String BODY =
            "<h1>Amazon SES SMTP Email Test</h1>" +
            "<p>This email was sent with Amazon SES using the " +
            "<a href='https://github.com/javaee/javamail'>Javamail Package</a>" +
            " for <a href='https://www.java.com'>Java</a>.";

    public static void main(String[] args) throws Exception {
        SendEmailAWS em = new SendEmailAWS("email-smtp.us-east-1.amazonaws.com", "AKIAJADABYZ7WHXJFXTQ", "AhNs13iGI525G2f4lulMFOaNUkYP90238CaDhBOqkv64");
        em.addAttachement("/home/blake/Arahant/WP - Sales/Deep Dive Questions.pdf", "file1.pdf");
        em.addAttachement("/home/blake/Arahant/WP - Sales/Why_Arahant_2.pdf", "file2.pdf");
        em.setHTMLMessage(BODY);
        Date beg = new Date();
        for (int i=0 ; i < 1 ; i++) {
            System.out.println(i);
            em.sendEmail("tony@wtgmerch.com", "Tony", "blake1024@gmail.com", "To Person", (new Date()).toString() + " " + i);
        }
        Date end = new Date();
        em.close();
        long diff = end.getTime() - beg.getTime();
        System.out.println(diff / 1000L);
    }

    public SendEmailAWS(String host, String user, String pw) throws MessagingException {
        HOST = host;
        SMTP_USERNAME = user;
        SMTP_PASSWORD = pw;
        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties.
        session = Session.getDefaultInstance(props);
        // Create a transport.
        transport = session.getTransport();
        transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
    }


    public SendEmailAWS sendEmail(String from, String fromname, String to, String toname, String subject) throws Exception {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from, fromname));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to, toname));
        msg.setSubject(subject);
        if (attachments != null) {
            BodyPart messageBodyPart = new MimeBodyPart();
            if (isHTML)
                messageBodyPart.setContent(message, "text/html");
            else
                messageBodyPart.setText(message);
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            for (Attachment att : attachments) {
                messageBodyPart = new MimeBodyPart();
                DataSource source;
                if (att.diskFileName != null)
                    source = new FileDataSource(att.diskFileName);
                else
                    source = new ByteArrayDataSource(att.byteArray, "image/" + att.type);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(att.attachmentName);
                multipart.addBodyPart(messageBodyPart);
            }
            msg.setContent(multipart);
        } else
            if (isHTML)
                msg.setContent(message, "text/html");
            else
                msg.setText(message);
        transport.sendMessage(msg, msg.getAllRecipients());
        return this;
    }

    public SendEmailAWS addAttachement(String diskFileName, String attachementName) {
        if (attachments == null)
            attachments = new ArrayList<>();
        attachments.add(new Attachment(diskFileName, attachementName));
        return this;
    }

    public SendEmailAWS addAttachement(byte [] data, String attachementName, String type) {
        if (attachments == null)
            attachments = new ArrayList<>();
        attachments.add(new Attachment(data, attachementName, type));
        return this;
    }

    public void close() throws MessagingException {
        transport.close();
    }

}
