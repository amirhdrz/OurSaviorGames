package com.oursaviorgames.backend.utils;


import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static com.oursaviorgames.backend.utils.LogUtils.LOGW;
import static com.oursaviorgames.backend.utils.LogUtils.makeLogTag;

public class EmailUtils {

    private static final String TAG = makeLogTag(EmailUtils.class);

    /**
     * Sends email to FILL_EMAIL_ADDRESS from GAE admin account.
     * <p>
     * Returns immediately if either of subject or message are null.
     *
     * @param subject Email subject.
     * @param message Email message.
     */
    public static void sendAdminEmail(String subject, String message) {
        if (subject == null || message == null) {
            return;
        }
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("FILL_EMAIL_ADDRESS", "OurSaviorGames Admin"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("FILL_EMAIL_ADDRESS", "OurSaviorGames Admin Email"));
            msg.setSubject(subject);
            msg.setText(message);
            Transport.send(msg);

        } catch (MessagingException | UnsupportedEncodingException e) {
            LOGW(TAG, e.toString());
        }
    }

}
