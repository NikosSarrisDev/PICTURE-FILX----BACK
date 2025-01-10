package controllers.contact;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execition_context.DatabaseExecutionContext;
import controllers.mailer.EmailService;
import models.contact.Contact;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ContactController extends Controller {

    private final JPAApi jpaApi;
    private final ObjectMapper objectMapper;
    private final DatabaseExecutionContext executionContext;

    private final EmailService emailService;

    @Inject()
    public ContactController(JPAApi jpaApi, ObjectMapper objectMapper, DatabaseExecutionContext executionContext, EmailService emailService) {
        this.jpaApi = jpaApi;
        this.objectMapper = objectMapper;
        this.executionContext = executionContext;
        this.emailService = emailService;
    }

    //sendContactForm
    public Result sendContactMessage(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Invalid Json Format");
        } else {

            ObjectNode result = Json.newObject();

            try {
                CompletableFuture<JsonNode> contactFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction( entityManager -> {

                        ObjectNode resultOfFuture = Json.newObject();

                        String text = json.findPath("text").asText();
                        String email = json.findPath("email").asText();

                        Contact contact = new Contact();
                        contact.setText(text);
                        contact.setEmail(email);

                        entityManager.persist(contact);

                        emailService.sendContactFormToAdminFromEmail(email, text);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("system", "contact");
                        resultOfFuture.put("message", "Το μύνημα εστάλη!");
                        return resultOfFuture;

                    });
                }, executionContext);

                result = (ObjectNode) contactFuture.get();
                return ok(result);

            } catch (Exception e) {
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Κάτι πήγε στραβά κατά την αποστολή του μηνύματος!");
                return ok(result);
            }
        }
    }

}
