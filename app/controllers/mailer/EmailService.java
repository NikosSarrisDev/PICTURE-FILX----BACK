package controllers.mailer;

import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import play.twirl.api.Html;

import javax.inject.Inject;
import java.io.File;
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
                .setSubject("Επαλλήθευση του email")
                .setFrom("PICTURE FLIX <nikolaossarrisnode@gmail.com>")
                .addTo(recipientEmail)
                .setBodyText("Πάτα το link που βλέπεις παρακάτω για να επαλληθεύσεις το email σου: " + verificationLink)
                .setBodyHtml("<p>Το link επαλλήθευσης:</p><a href='" + verificationLink + "'>Επαλλήθευση Email</a>");

        mailerClient.send(email);
    }

    //Method for sending the password to the email(forgot Password feature)
    public void sendPasswordToEmail(String recipientEmail, String password) {
        Email email = new Email()
                .setSubject("Ο Κωδικός Πρόσβασης στάλθηκε!")
                .setFrom("PICTURE FLIX <nikolaossarrisnode@gmail.com>")
                .addTo(recipientEmail)
                .setBodyText("Ο κωδικός πρόσβασής σου για το PICTURE FLIX είναι : " + password);

        mailerClient.send(email);
    }

    public void sendContactFormToAdminFromEmail(String recipientEmail, String text) {
        Email email = new Email()
                .setSubject("Έχεις ένα μύνημα από έναν χρήστη!")
                .setFrom(recipientEmail)
                .addTo("nikolaossarrisnode@gmail.com")
                .setBodyText("Ο χρήστης με το email " + recipientEmail + " σου έστειλε αυτό το κείμενο: " + text);

        mailerClient.send(email);
    }

    public void sendTicketToUserEmail(String recipientEmail, String movieTitle, String roomTitle, String[] seats, String date, String startTime, double amount) {

        Html emailHtml = views.html.ticketInstance.render(recipientEmail, movieTitle, roomTitle, seats, date, startTime, amount);

        Email email = new Email()
                .setSubject("Το Εισιτήριό σας! Καλή σας απόλαυση")
                .setFrom("PICTURE FLIX <nikolaossarrisnode@gmail.com>")
                .addTo(recipientEmail)
                .addAttachment("qrCode.jpg", new File("public/images/qrCode.jpg"), "qrcode")
                .addAttachment("logo.svg", new File("public/images/logo.jpg"), "logo")
                .setBodyHtml(emailHtml.body());

        mailerClient.send(email);
    }

}
