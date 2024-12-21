package controllers.User;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.qos.logback.core.encoder.EchoEncoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigException;
import controllers.execition_context.DatabaseExecutionContext;
import controllers.mailer.EmailService;
import models.User.User;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import controllers.EncryptDecrypt.EncryptDecrypt;
public class UsersController  extends Controller {

    private final JPAApi jpaApi;
    private final ObjectMapper objectMapper;
    private final DatabaseExecutionContext executionContext;
    private final EmailService emailService;

    private final EncryptDecrypt encryptDecrypt;

    @Inject
    public UsersController(JPAApi jpaApi, ObjectMapper objectMapper, DatabaseExecutionContext executionContext, EmailService emailService, EncryptDecrypt encryptDecrypt){
        this.jpaApi = jpaApi;
        this.objectMapper = objectMapper;
        this.executionContext = executionContext;
        this.emailService = emailService;
        this.encryptDecrypt = encryptDecrypt;
    }

    // Create a new user
    public Result createUser(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null){
            return badRequest("Invalid Json Format");
        }else {
            try{
                ObjectNode result;
                CompletableFuture<JsonNode> getFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {
                        ObjectNode resultOfFuture = Json.newObject();

                        String name = json.findPath("name").asText();
                        String email = json.findPath("email").asText();
                        String password = json.findPath("password").asText();
                        String hashedPassword = "@@@@";

                        try {
                            hashedPassword = encryptDecrypt.encrypt(password);
                        }catch (Exception e){
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Σφάλμα κατά την εγγραφή");
                        }

                        // Check if email already exists
                        long emailCount = (long) entityManager.createNativeQuery(
                                        "SELECT COUNT(*) FROM users u WHERE u.email = '" + email + "'", Long.class)
                                .getSingleResult();
                        if (emailCount > 0) {
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Το email υπάρχει ήδη!");
                            return resultOfFuture;
                        }

                        User user = new User();

                        user.setName(name);
                        user.setEmail(email);
                        user.setPassword(hashedPassword);

                        entityManager.persist(user);

                        String verificationLink = "http://localhost:9000/verify-email?email=" + email;

                        // Send verification email
                        emailService.sendVerificationEmail(email, verificationLink);

                        //The final json
                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", user.getId());
                        resultOfFuture.put("system", "USERS_ACTIONS");
                        resultOfFuture.put("message", "Η εγγραφή ολοκληρώθηκε με επιτυχία!");
                        return resultOfFuture;
                    });
                }, executionContext);
                result = (ObjectNode) getFuture.get();
                return ok(result);
            }catch (Exception e){
                ObjectNode result = Json.newObject();
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Πρόβλημα κατά την εγγραφή");
                return ok(result);
            }
        }
    }


    public Result forgotPasswordSendEmail(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null){
            return badRequest("Invalid Json Format");
        }else {
            try{
                ObjectNode result;
                CompletableFuture<JsonNode> forgotFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {
                        ObjectNode resultOfFuture = Json.newObject();

                        String email = json.findPath("email").asText();

                        // Check if email already exists
                        long emailCount = (long) entityManager.createNativeQuery(
                                        "SELECT COUNT(*) FROM users u WHERE u.email = '" + email + "'", Long.class)
                                .getSingleResult();
                        if(emailCount == 0){
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Ο χρήστης με το email " + email + " δεν βρέθηκε");
                            return resultOfFuture;
                        }

                        User user  = (User) entityManager.createNativeQuery("select * from users u where u.email = '" + email + "'", User.class).getSingleResult();

                        //Αποστολή του αποκρυτογραφημένου κωδικού στο email του χρήστη
                        try{
                            emailService.sendPasswordToEmail(email, encryptDecrypt.decrypt(user.getPassword()));
                        }catch (Exception e){
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Πρόβλημα κατά την ανάκτηση κωδικού");
                            return resultOfFuture;
                        }

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("system", "USERS_ACTIONS");
                        resultOfFuture.put("message", "Το email στάλθηκε");
                        return resultOfFuture;
                    });
                }, executionContext);
                result = (ObjectNode) forgotFuture.get();
                return ok(result);
            }catch (Exception e){
                ObjectNode result = Json.newObject();
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Πρόβλημα κατά την ανάκτηση κωδικού");
                return ok(result);
            }
        }
    }

    public Result verifyEmail(String email) throws IOException{
        if(email == null){
            return badRequest("Το email δεν έχει τη σωστή μορφή");
        }else {
            ObjectNode result = Json.newObject();

            try{
                CompletableFuture verifyFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {

                        ObjectNode resultOfFuture = Json.newObject();

                        User user = (User) entityManager.createNativeQuery("SELECT * FROM users u WHERE u.email = '" + email + "'", User.class)
                                .getSingleResult();
                        user.setVerified(true);
                        entityManager.merge(user);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("system", "USERS_ACTIONS");
                        resultOfFuture.put("message","Το email επικυρώθηκε");
                        return resultOfFuture;
                    });
                }, executionContext);

                result = (ObjectNode) verifyFuture.get();
                return ok(result);

            }catch (Exception e){
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Σφάλμα κατά την επικύρωση");
                return ok(result);
            }
        }
    }


    //login User
    public Result loginUser(final Http.Request request) throws IOException{
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Invalid JSON format");
        }else {
            try {
                ObjectNode result;
                CompletableFuture<JsonNode> getFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {

                        ObjectNode resultOfFuture = Json.newObject();

                        String email = json.findPath("email").asText();
                        String password = json.findPath("password").asText();

                        // Check if email already exists
                        long emailCount = (long) entityManager.createNativeQuery(
                                        "SELECT COUNT(*) FROM users u WHERE u.email = '" + email + "'", Long.class)
                                .getSingleResult();
                        if(emailCount == 0){
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Ο χρήστης με το email " + email + " δεν βρέθηκε!");
                            return resultOfFuture;
                        }

                        //Retrieve user by email
                        User user  = (User) entityManager.createNativeQuery("select * from users u where u.email = '" + email + "'", User.class).getSingleResult();

                        //Έλεγχος του αποκρυπτογραφημένου κωδικού
                        String passwordResult = "@@@@";
                        try{
                            passwordResult = encryptDecrypt.decrypt(user.getPassword());
                        }catch (Exception e){
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Πρόβλημα κατά την είσοδο");
                            return resultOfFuture;
                        }

                        if (!passwordResult.equals(password)) {
                            // Incorrect password
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Λανθασμένος κωδικός");
                            return resultOfFuture;
                        }

                        if (!user.isVerified()){
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Ο λογαριασμός υπάρχει αλλά δεν έχει επαληθευτεί το email");
                            return resultOfFuture;
                        }

                        // Success - return user details or token
                        //If is not verified this will not be executed
                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", user.getId());
                        resultOfFuture.put("name", user.getName());
                        resultOfFuture.put("email", user.getEmail());
                        resultOfFuture.put("isVerified", user.isVerified());
                        resultOfFuture.put("system", "USERS_ACTIONS");
                        resultOfFuture.put("message", "Η σύνδεση πραγματοποιήθηκε!");
                        return resultOfFuture;

                    });
                }, executionContext);

                result = (ObjectNode) getFuture.get();
                return ok(result);

            } catch (Exception e) {
                e.printStackTrace();
                ObjectNode result = Json.newObject();
                result.put("status", "error");
                result.put("message", "Σφάλμα κατά την είσοδο");
                return internalServerError(result);
            }
        }

    }

    // Get all users good for testing
    public CompletionStage<Result> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> jpaApi.withTransaction(em -> {
            List<User> users = em.createQuery("SELECT u FROM User u", User.class).getResultList();
            JsonNode json = objectMapper.valueToTree(users);
            return ok(json);
        }));
    }

    // Get user by ID
    public CompletionStage<Result> getUser(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            JsonNode json = request.body().asJson();
            if (json == null || !json.has("id")) {
                return badRequest("Missing user ID in JSON");
            }

            Long userId = json.get("id").asLong();
            return jpaApi.withTransaction(em -> {
                User user = em.find(User.class, userId);
                if (user == null) {
                    return notFound("User not found");
                }
                JsonNode userJson = objectMapper.valueToTree(user);
                return ok(userJson);
            });
        });
    }

    // Update user by ID
    public CompletionStage<Result> updateUser(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            JsonNode json = request.body().asJson();
            if (json == null || !json.has("id")) {
                return badRequest("Missing user ID in JSON");
            }

            Long userId = json.get("id").asLong();
            return jpaApi.withTransaction(em -> {
                User user = em.find(User.class, userId);
                if (user == null) {
                    return notFound("User not found");
                }

                User updatedUser = objectMapper.convertValue(json, User.class);
                user.setName(updatedUser.getName());
                user.setEmail(updatedUser.getEmail());
                user.setPassword(updatedUser.getPassword());
                em.merge(user);
                return ok("User updated successfully!");
            });
        });
    }

    // Delete user by ID
    public CompletionStage<Result> deleteUser(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            JsonNode json = request.body().asJson();
            if (json == null || !json.has("id")) {
                return badRequest("Missing user ID in JSON");
            }

            Long userId = json.get("id").asLong();
            return jpaApi.withTransaction(em -> {
                User user = em.find(User.class, userId);
                if (user == null) {
                    return notFound("User not found");
                }
                em.remove(user);
                return ok("User deleted successfully!");
            });
        });
    }
}
