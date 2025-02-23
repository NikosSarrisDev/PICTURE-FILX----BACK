package controllers.mailer;

import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import play.twirl.api.Html;

import javax.inject.Inject;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;

public class EmailService {

    private final MailerClient mailerClient;

    @Inject
    public EmailService(MailerClient mailerClient) {
        this.mailerClient = mailerClient;
    }

    //Method for verify emails
    public void sendVerificationEmail(String recipientEmail, String verificationLink) {
        Email email = new Email()
                .setSubject("Verify Your Email")
                .setFrom("PICTURE FLIX <nikolaossarrisnode@gmail.com>")
                .addTo(recipientEmail)
                .setBodyText("Click the link below to verify your email: " + verificationLink)
                .setBodyHtml("<p>Click the link below to verify your email:</p><a href='" + verificationLink + "'>Verify Email</a>");

        mailerClient.send(email);
    }

    //Method for sending the password to the email(forgot Password feature)
    public void sendPasswordToEmail(String recipientEmail, String password) {
        Email email = new Email()
                .setSubject("Password Sent!")
                .setFrom("PICTURE FLIX <nikolaossarrisnode@gmail.com>")
                .addTo(recipientEmail)
                .setBodyText("Your password for PICTURE FLIX is : " + password);

        mailerClient.send(email);
    }

    public void sendContactFormToAdminFromEmail(String recipientEmail, String text) {
        Email email = new Email()
                .setSubject("You have a message from a user!")
                .setFrom(recipientEmail)
                .addTo("nikolaossarrisnode@gmail.com")
                .setBodyText("The user with email " + recipientEmail + " send you this text: " + text);

        mailerClient.send(email);
    }

    public void sendTicketToUserEmail(String recipientEmail, String movieTitle, String roomTitle, String[] seats, Date date, Time startTime, float amount) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        String formattedDate = dateFormat.format(date);

        Html emailHtml = views.html.ticketInstance.render(recipientEmail, movieTitle, roomTitle, seats, formattedDate, startTime.toString(), amount);

        Email email = new Email()
                .setSubject("Το Εισιτήριό σας! Καλή σας απόλαυση")
                .setFrom("PICTURE FLIX <nikolaossarrisnode@gmail.com>")
                .addTo(recipientEmail)
                .setBodyHtml(emailHtml.body());

        mailerClient.send(email);
    }

}
