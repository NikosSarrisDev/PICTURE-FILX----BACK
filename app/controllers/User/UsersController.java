package controllers.User;

import at.favre.lib.crypto.bcrypt.BCrypt;
import ch.qos.logback.core.encoder.EchoEncoder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigException;
import controllers.execition_context.DatabaseExecutionContext;
import controllers.mailer.EmailService;
import models.Movies.Movie;
import models.User.User;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

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
                        String role = json.findPath("role").asText();
                        String phone  = json.findPath("phone").asText();
                        String photo = json.findPath("photo").asText();
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
                        if(role.equals("USER") || role.equals("ADMIN")){
                            user.setRole(role);
                        } else {
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Ο σιγκεκριμένος ρόλος δεν υποστηρίζεται από το σύστημα");
                            return resultOfFuture;
                        }
                        user.setPassword(hashedPassword);
                        user.setPhone(phone);
                        user.setPhoto(photo);

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

    public Result updateUserDetails(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if(json == null) {
            return badRequest("Invalid JsonFormat");
        } else {

            ObjectNode result = Json.newObject();

            try {
                CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {

                        ObjectNode resultOfFuture = Json.newObject();

                        Long id = json.findPath("id").asLong();
                        String name = json.findPath("name").asText();
                        String email = json.findPath("email").asText();
                        String role = json.findPath("role").asText();
                        String phone = json.findPath("phone").asText();
                        String photo = json.findPath("photo").asText();
                        boolean hasEntered = json.findPath("hasEntered").asBoolean();
                        int numOfTickets = json.findPath("numOfTickets").asInt();

                        User user = entityManager.find(User.class, id);

                        if (json.has("name")) {
                            user.setName(name);
                        }
                        if (json.has("email")) {
                            user.setEmail(email);
                        }
                        if (json.has("role")) {
                            user.setRole(role);
                        }
                        if (json.has("phone")) {
                            user.setPhone(phone);
                        }
                        if (json.has("photo")) {
                            user.setPhoto(photo);
                        }
                        if (json.has("hasEntered")) {
                            user.setHasEntered(hasEntered);
                        }
                        if (json.has("numOfTickets")) {
                            user.setNumOfTickets(numOfTickets);
                        }

                        entityManager.merge(user);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", user.getId());
                        resultOfFuture.put("system", "USERS_ACTIONS");
                        resultOfFuture.put("message", "Η ενημέρωση ολοκληρώθηκε με επιτυχία!");
                        return resultOfFuture;
                    });
                }, executionContext);

                result = (ObjectNode) updateFuture.get();
                return ok(result);

            } catch (Exception e) {
                result.put("status", "error");
                result.put("message", "Σφάλμα κατά την ενημέρωση");
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
                if ("success".equals(result.get("status").asText())) {
                    return ok(views.html.verifyEmailTemplate.render(email));
                } else {
                    return internalServerError("Verification failed");
                }

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
                            resultOfFuture.put("message", "Ο λογαριασμός υπάρχει αλλά δεν έχει επαληθευτεί το email. Δες την αλληλογραφία σου!");
                            return resultOfFuture;
                        }


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

    public Result googleLoginSendCredentials(final Http.Request request) throws IOException{
        JsonNode json = request.body().asJson();
        if(json == null){
            return badRequest("Invalid Json Format");
        }else {

            ObjectNode result = Json.newObject();

            try{

                CompletableFuture<JsonNode> googleFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {
                        ObjectNode resultOfFuture = Json.newObject();

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("credential", json);
                        resultOfFuture.put("message", "Η σύνδεση με Google έγινε με επιτυχία");
                        return resultOfFuture;
                    });
                }, executionContext);

                result = (ObjectNode) googleFuture.get();
                return ok(result);

            }catch (Exception e){
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Κάτι πήγε στραβά με την σύνδεση με το email");
                return ok(result);
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
    public Result getUser(final Http.Request request) throws IOException, ExecutionException, InterruptedException{

        JsonNode json = request.body().asJson();

        if(json == null){
            return badRequest("Invalid Json Format");
        }
        else {
            ObjectNode result = Json.newObject();
            HashMap<String, Object> returnList = new HashMap<>();
            String jsonResult = "";


            CompletableFuture<HashMap<String, Object>> getFuture = CompletableFuture.supplyAsync(() -> {

                return jpaApi.withTransaction(entityManager -> {

                    String name = json.findPath("name").asText();
                    String email = json.findPath("email").asText();
                    String role = json.findPath("role").asText();
                    String phone = json.findPath("phone").asText();
                    String photo = json.findPath("photo").asText();
                    boolean hasEntered = json.findPath("hasEntered").asBoolean();
                    String numOfTickets = json.findPath("numOfTickets").asText();

                    String sql = "select * from users u where 1=1";

                    if (name != null && !name.equalsIgnoreCase("") && !name.equalsIgnoreCase("null")) {
                        sql += " and name = " + "'" + name + "'";
                    }
                    if (email != null && !email.equalsIgnoreCase("") && !email.equalsIgnoreCase("null")) {
                        sql += " and email = " + "'" + email + "'";
                    }
                    if (role != null && !role.equalsIgnoreCase("") && !role.equalsIgnoreCase("null")) {
                        sql += " and role = " + "'" + role + "'";
                    }
                    if (phone != null && !phone.equalsIgnoreCase("") && !phone.equalsIgnoreCase("null")) {
                        sql += " and phone = " + "'" + phone + "'";
                    }
                    if (photo != null && !photo.equalsIgnoreCase("") && !photo.equalsIgnoreCase("null")) {
                        sql += " and photo = " + "'" + photo + "'";
                    }
                    if (json.has("hasEntered")) {
                        sql += " and hasEntered = " + hasEntered;
                    }
                    if (numOfTickets != null && !numOfTickets.equalsIgnoreCase("") && !numOfTickets.equalsIgnoreCase("null")) {
                        sql += " and numOfTickets = " + numOfTickets;
                    }

                    HashMap<String, Object> returnListFuture = new HashMap<>();
                    List<HashMap<String, Object>> finalList = new ArrayList<>();
                    List<User> list = (List<User>) entityManager.createNativeQuery(sql, User.class).getResultList();

                    for (User user : list) {
                        HashMap<String, Object> officeMap = new HashMap<>();
                        officeMap.put("id", user.getId());
                        officeMap.put("name", user.getName());
                        officeMap.put("email", user.getEmail());
                        officeMap.put("role", user.getRole());
                        officeMap.put("phone", user.getPhone());
                        officeMap.put("photo", user.getPhoto());
                        officeMap.put("hasEntered", user.isHasEntered());
                        officeMap.put("numOfTickets", user.getNumOfTickets());

                        finalList.add(officeMap);
                    }
                    returnListFuture.put("data", finalList);
                    returnListFuture.put("total", list.size());
                    returnListFuture.put("status", "success");
                    returnListFuture.put("message", "ok");
                    return returnListFuture;

                });

            }, executionContext);

            returnList = getFuture.get();
            DateFormat myDateFormat = new SimpleDateFormat("yyyy/MM/dd");
            objectMapper.setDateFormat(myDateFormat);

            try {
                jsonResult = objectMapper.writeValueAsString(returnList);
            } catch (Exception e) {
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Πρόβλημα κατά την ανάγνωση των στοιχείων");
                return ok(result);
            }
            return ok(jsonResult);
        }
    }

    //Για την αλλαγή κωδικού
    public Result updateUserPassword(Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if(json == null){
            return badRequest("Invalid Json Format");
        }else {

            ObjectNode result = Json.newObject();

            try{
                CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {

                        ObjectNode resultOfFuture = Json.newObject();

                        Long id = json.findPath("id").asLong();

                        String unhashedPassword = json.findPath("password").asText();

                        long userCount = (long) entityManager.createNativeQuery("select count(*) from users u where id =" + id, Long.class).getSingleResult();
                        if(userCount == 0){
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "User with id: " + id + " does not exists!");
                            return resultOfFuture;
                        }

                        User user = entityManager.find(User.class, id);

                        //Hash the unhashed passsword
                        String hashedPassword = "@@@@";
                        try{
                            hashedPassword = encryptDecrypt.encrypt(unhashedPassword);
                        }catch (Exception e){
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Σφάλμα κατά την αλλαγή κωδικού");
                            return resultOfFuture;
                        }

                        user.setPassword(hashedPassword);

                        entityManager.merge(user);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", user.getId());
                        resultOfFuture.put("name", user.getName());
                        resultOfFuture.put("email", user.getEmail());
                        resultOfFuture.put("isVerified", user.isVerified());
                        resultOfFuture.put("system", "USERS_ACTIONS");
                        resultOfFuture.put("message", "Η αλλαγή κωδικού πραγματοποιήθηκε!");
                        return resultOfFuture;

                    });
                }, executionContext);

                result = (ObjectNode) updateFuture.get();
                return ok(result);

            }catch (Exception e){
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Σφάλμα κατά την αλλαγή κωδικού");
                return ok(result);
            }
        }
    }

    // Delete user by ID
    public Result deleteUser(Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null){
            return badRequest("Invalid Json Format");
        }else {

            ObjectNode result = Json.newObject();

            try{
                CompletableFuture<JsonNode> deleteFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {

                        ObjectNode resultOfFuture = Json.newObject();

                        Long id = json.findPath("id").asLong();

                        User user = entityManager.find(User.class, id);

                        entityManager.remove(user);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", user.getId());
                        resultOfFuture.put("system", "USERS_ACTIONS");
                        resultOfFuture.put("message", "Η διαγραφή ολοκληρώθηκε με επιτυχία!");
                        return resultOfFuture;

                    });
                }, executionContext);

                result = (ObjectNode) deleteFuture.get();
                return ok(result);

            }catch (Exception e){
                e.printStackTrace();
                result.put("status", "error");
                result.put("system", "USERS_ACTIONS");
                result.put("message", "Σφάλμα κατά την διαγραφή");
                return ok(result);
            }
        }
    }
}
