package controllers.mailer;

import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;

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
    public  void sendPasswordToEmail(String recipientEmail, String password) {
        Email email = new Email()
                .setSubject("Password Sent!")
                .setFrom("PICTURE FLIX <nikolaossarrisnode@gmail.com>")
                .addTo(recipientEmail)
                .setBodyText("Your password for PICTURE FLIX is : " + password);

        mailerClient.send(email);
    }

}
