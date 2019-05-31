package lab_7.crypto;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class Mail {

    private static final String USERNAME = "testemailnikolaev";
    private static final String FROMMAIL = "@yandex.ru";
    private static final String PASSWORD = "lab7isthebest";
    private static final int PORT = 465;

    public static void sendMessage(String email, String message){
        String from = USERNAME + FROMMAIL;
        String host = "smtp.yandex.com";

        Properties props = new Properties();

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "true");
        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            //System.out.println(email);
            InternetAddress[] address = {new InternetAddress(email)};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject("ЗБС - Золотой Болт Сантехника. Всегда рядом с вами.");
            msg.setSentDate(new Date());
            msg.setText(message);
            Transport.send(msg);
        }
        catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
