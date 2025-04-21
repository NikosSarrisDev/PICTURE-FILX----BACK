package controllers.movies;

import akka.stream.SystemMaterializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jaiimageio.impl.common.ImageUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import controllers.base64Converter.ImageUtils;
import controllers.execition_context.DatabaseExecutionContext;
import controllers.mailer.EmailService;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import models.User.User;
import models.room.Room;
import models.seat.Seat;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SeatController extends Controller {

    private final JPAApi jpaApi;
    private final ObjectMapper objectMapper;
    private final DatabaseExecutionContext executionContext;
    private final EmailService emailService;

    @Inject()
    public SeatController(JPAApi jpaApi, ObjectMapper objectMapper, DatabaseExecutionContext executionContext, EmailService emailService) {
        this.jpaApi = jpaApi;
        this.objectMapper = objectMapper;
        this.executionContext = executionContext;
        this.emailService = emailService;
    }

    public Result addSeat(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Invalid Json Format");
        } else {

            ObjectNode result = Json.newObject();

            try {

                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {

                        ObjectNode resultOfFuture = Json.newObject();

                        String title = json.findPath("title").asText();
                        int row = json.findPath("rowSeat").asInt();
                        int col = json.findPath("colSeat").asInt();
                        boolean reserved = json.findPath("reserved").asBoolean();
                        boolean selected = json.findPath("selected").asBoolean();
                        Long roomId = json.findPath("room_id").asLong();

                        //Use the room_id from the json to find that room
                        Room room = entityManager.find(Room.class, roomId);

                        if (room == null){
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message","Το δωμάτιο αυτό δεν υπάρχει");
                            return resultOfFuture;
                        }

                        Seat seat = new Seat();

                        seat.setTitle(title);
                        seat.setRow(row);
                        seat.setCol(col);
                        seat.setReserved(reserved);
                        seat.setSelected(selected);
                        seat.setRoom(room);

                        entityManager.persist(seat);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", seat.getId());
                        resultOfFuture.put("system", "SEATS_ACTIONS");
                        resultOfFuture.put("message", "Η καταχώρηση ολοκληρώθηκε με επιτυχία!");
                        return resultOfFuture;

                    });
                }, executionContext);

                result = (ObjectNode) addFuture.get();
                return ok(result);

            } catch (Exception e) {
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Σφάλμα κατά την εισαγωγή");
                return ok(result);
            }
        }
    }

    public Result addAllSeats(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if(json == null) {
            return badRequest("Invalid Json Format");
        } else {

            ObjectNode result = Json.newObject();

            try {

                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {

                        ObjectNode resultOfFuture = Json.newObject();

                        Long roomId = json.findPath("roomId").asLong();
                        int total = json.findPath("total").asInt();

                        Query query = entityManager.createNativeQuery("CALL InsertTickets(" + total + "," + roomId + ")");
                        query.executeUpdate();

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("system", "SEATS_ACTIONS");
                        resultOfFuture.put("message", "Επιτυχία στην προσθήκη των θέσεων στο δωμάτιο");
                        return resultOfFuture;
                    });
                }, executionContext);

                result = (ObjectNode) addFuture.get();
                return ok(result);

            } catch (Exception e) {
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Κάτι πήγε στραβά με τη προσθήκη των θέσεων!");
                return ok(result);
            }
        }
    }

    public Result updateSeat(final Http.Request request) throws IOException {

        JsonNode json = request.body().asJson();

        if (json == null) {
            return badRequest("Invalid Json Format");
        } else {
            ObjectNode result = Json.newObject();
            try {

                CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {

                    return jpaApi.withTransaction(entityManager -> {
                        ObjectNode resultOfFuture = Json.newObject();

                        Long id = json.findPath("id").asLong();
                        String title = json.findPath("title").asText();
                        int row = json.findPath("row").asInt();
                        int col = json.findPath("col").asInt();
                        boolean reserved = json.findPath("reserved").asBoolean();
                        boolean selected = json.findPath("selected").asBoolean();
                        Long roomId = json.findPath("room_id").asLong();

                        Seat seat = entityManager.find(Seat.class, id);

                        if (seat == null) {
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Η θέση αυτή δεν υπάρχει");
                            return resultOfFuture;
                        }

                        // Only update fields that exist in the request
                        if (json.has("title") && !json.get("title").isNull()) {
                            seat.setTitle(title);
                        }
                        if (json.has("row") && !json.get("row").isNull()) {
                            seat.setRow(row);
                        }
                        if (json.has("col") && !json.get("col").isNull()) {
                            seat.setCol(col);
                        }
                        if (json.has("reserved") && !json.get("reserved").isNull()) {
                            seat.setReserved(reserved);
                        }
                        if (json.has("selected") && !json.get("selected").isNull()) {
                            seat.setSelected(selected);
                        }
                        if (json.has("room_id") && !json.get("room_id").isNull()) {
                            Room room = entityManager.find(Room.class, roomId);
                            if (room != null) {
                                seat.setRoom(room);
                            } else {
                                resultOfFuture.put("status", "error");
                                resultOfFuture.put("message", "Το δωμάτιο αυτό δεν υπάρχει");
                                return resultOfFuture;
                            }
                        }

                        entityManager.merge(seat);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", seat.getId());
                        resultOfFuture.put("system", "SEATS_ACTIONS");
                        resultOfFuture.put("message", "Η ενημέρωση ολοκληρώθηκε με επιτυχία!");
                        return resultOfFuture;
                    });

                }, executionContext);

                result = (ObjectNode) updateFuture.get();
                return ok(result);

            } catch (Exception e) {
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Σφάλμα κατά την ενημέρωση");
                return ok(result);
            }
        }
    }

    public Result updateAllSeat(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();

        if (json == null) {
            return badRequest("Invalid Json Format");
        } else {
            ObjectNode result = Json.newObject();
            try {
                CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {
                        ObjectNode resultOfFuture = Json.newObject();

                        String idList = json.findPath("idList").asText();
                        String seatTitleList = json.findPath("seatTitleList").asText();
                        boolean selected = json.findPath("selected").asBoolean();
                        boolean reserved = json.findPath("reserved").asBoolean();

                        String sql = "update seats set ";
                        if (json.has("selected")) {
                            sql += "selected = " + (selected ? "true" : "false");
                        }
                        if (json.has("reserved")) {
                            sql += "reserved = " + (reserved ? "true" : "false");
                        }
                        if (idList != null && !idList.equalsIgnoreCase("") && !idList.equalsIgnoreCase("null") && (json.has("selected") || json.has("reserved"))) {
                            sql += " where id in " + "(" + idList + ")";
                        }
                        if (seatTitleList != null && !seatTitleList.equalsIgnoreCase("") && !seatTitleList.equalsIgnoreCase("null") && (json.has("selected") || json.has("reserved"))) {
                            sql += " where title in " + "(" + seatTitleList + ")";
                        }

                        Query query = entityManager.createNativeQuery(sql);
                        int rowsUpdated = query.executeUpdate();

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("rows_updated", rowsUpdated);
                        resultOfFuture.put("system", "SEATS_ACTIONS");
                        resultOfFuture.put("message", "Η μαζική ενημέρωση ολοκληρώθηκε με επιτυχία!");
                        return resultOfFuture;
                    });
                }, executionContext);

                result = (ObjectNode) updateFuture.get();
                return ok(result);

            } catch (Exception e) {
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Σφάλμα κατά την ενημέρωση");
                return ok(result);
            }
        }
    }

    public Result deleteSeat(final Http.Request request) throws IOException {

        JsonNode json = request.body().asJson();

        if (json == null) {
            return badRequest("Invalid Json Format");
        } else {
            ObjectNode result = Json.newObject();
            try {

                CompletableFuture<JsonNode> deleteFuture = CompletableFuture.supplyAsync(() -> {

                    return jpaApi.withTransaction(entityManager -> {
                        ObjectNode resultOfFuture = Json.newObject();

                        Long id = json.findPath("id").asLong();

                        Seat seat = entityManager.find(Seat.class, id);

                        entityManager.remove(seat);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", seat.getId());
                        resultOfFuture.put("system", "SEATS_ACTIONS");
                        resultOfFuture.put("message", "Η διαγραφή ολοκληρώθηκε με επιτυχία!");
                        return resultOfFuture;
                    });

                }, executionContext);

                result = (ObjectNode) deleteFuture.get();
                return ok(result);

            } catch (Exception e) {
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Σφάλμα κατά την διαγραφή");
                return ok(result);
            }
        }
    }

    public Result sendTicketToUser(final Http.Request request) throws IOException, WriterException {
        JsonNode json = request.body().asJson();
        if(json==null){
            return badRequest("Invalid Json Format");
        } else {

            ObjectNode result = Json.newObject();

            //QR Code initialization
            String data = "http://localhost:9000/welcomeScanMessage";
            String path= "/Users/nikolaossarris/PictureFlixBack/public/images/qrCode.jpg";

            try{

                String userEmail = json.findPath("userEmail").asText();
                String movieTitle = json.findPath("movieTitle").asText();
                String roomTitle = json.findPath("roomTitle").asText();
                JsonNode seatsNode = json.findPath("seats");
                String date = json.findPath("date").asText();
                String time = json.findPath("time").asText();
                double amount = json.findPath("amount").asDouble();

                data += "?userEmail=" + userEmail.replace("@", "%40");
                BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 500, 500);

                //Store the qr code icon in the static files of play
                MatrixToImageWriter.writeToPath(matrix, "jpg", Paths.get(path));

                String[] seatsArray = {};
                if (seatsNode.isArray()) {
                    seatsArray = new String[seatsNode.size()];
                    for (int i = 0; i < seatsNode.size(); i++) {
                        seatsArray[i] = seatsNode.get(i).asText();
                    }
                }

                //Convert String date to java.sql.Date
                Date sqlDate = Date.valueOf(date);

                //Format the date in the ticket with a proper format
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
                String formattedDate = dateFormat.format(sqlDate);

                this.emailService.sendTicketToUserEmail(userEmail, movieTitle, roomTitle, seatsArray, formattedDate, time, amount);
                result.put("status", "success");
                result.put("message", "Τα εισιτήριά σας αποστάλθηκαν στην Αλληλογραφία σας!");
                return ok(result);
            } catch (Exception e){
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Σφάλμα κατά την αποστολή στην Αλληλογραφία");
                return ok(result);
            }
        }
    }

    //This Action is used for display the welcome message when after the scan of the qr code
    public Result welcomeScanMessage(final Http.Request request) throws IOException {
        ObjectNode result = Json.newObject();
        try {

            CompletableFuture<JsonNode> welcomeFuture = CompletableFuture.supplyAsync(() -> {
                return jpaApi.withTransaction(entityManager -> {
                    ObjectNode resultOfFuture = Json.newObject();

                    String email = request.getQueryString("userEmail").replace("%40", "@");
                    System.out.println(email);

                    String sql = "update users set hasEntered = true where email = " + "'" + email + "'";

                    Query query = entityManager.createNativeQuery(sql, User.class);
                    int rowUpdated = query.executeUpdate();

                    User user = (User) entityManager.createNativeQuery("select * from users where email = " + "'" + email + "'", User.class).getSingleResult();

                    resultOfFuture.put("status", "success");
                    resultOfFuture.put("row_updated", rowUpdated);
                    resultOfFuture.put("Username", user.getName());
                    resultOfFuture.put("system", "USERS_ACTIONS");
                    return resultOfFuture;
                });
            },executionContext);

            result = (ObjectNode) welcomeFuture.get();
            return ok(views.html.welcomeScreenMessage.render(result.get("Username").asText()));

        }catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Σφάλμα κατά την ενημέρωση");
            return ok(result);
        }
    }

    public Result getSeat(final Http.Request request) throws IOException, ExecutionException, InterruptedException{

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

                    String orderCol = json.findPath("orderCol").asText();
                    String descAsc = json.findPath("descAsc").asText();
                    String id = json.findPath("id").asText();
                    String title = json.findPath("title").asText();
                    String roomId = json.findPath("roomId").asText();
                    String roomTitle = json.findPath("roomTitle").asText();
                    boolean selected = json.findPath("selected").asBoolean();
                    boolean reserved = json.findPath("reserved").asBoolean();
                    String start = json.findPath("start").asText();
                    String limit = json.findPath("limit").asText();

                    String sql = "select s.id, s.rowSeat, s.colSeat, s.room_id, s.reserved as reserved, s.selected as selected, s.title AS seat_title, r.title AS room_title from seats s join rooms r on s.room_id = r.id where 1=1";

                    if(title != null && !title.equalsIgnoreCase("") && !title.equalsIgnoreCase("null")){
                        sql += " and (s.title) like " + "'%" + title + "%'";
                    }
                    if(roomTitle != null && !roomTitle.equalsIgnoreCase("") && !roomTitle.equalsIgnoreCase("null")){
                        sql += " and (r.title) like " + "'%" + roomTitle + "%'";
                    }
                    if(json.has("selected")){
                        sql += " and (s.selected) = " + (selected ? 1 : 0);
                    }
                    if(json.has("reserved")){
                        sql += " and (s.reserved) = " + (reserved ? 1 : 0);
                    }

                    List<Tuple> listAll = (List<Tuple>) entityManager.createNativeQuery(sql, Tuple.class).getResultList();

                    if(orderCol != null && !orderCol.equalsIgnoreCase("")){
                        sql += " order by " + orderCol + " " + descAsc;
                    }else {
                        sql += " order by s.id asc";
                    }
                    if(start != null && !start.equalsIgnoreCase("")){
                        sql += " limit " + start + "," + limit;
                    }

                    HashMap<String, Object> returnListFuture = new HashMap<>();
                    List<HashMap<String, Object>> finalList = new ArrayList<>();
                    List<Tuple> list = (List<Tuple>) entityManager.createNativeQuery(sql, Tuple.class).getResultList();

                    for (Tuple tuple : list) {
                        HashMap<String, Object> officeMap = new HashMap<>();
                        officeMap.put("id", tuple.get("id"));
                        officeMap.put("title", tuple.get("seat_title"));
                        officeMap.put("roomTitle", tuple.get("room_title"));
                        officeMap.put("row", tuple.get("rowSeat"));
                        officeMap.put("col", tuple.get("colSeat"));
                        officeMap.put("reserved", tuple.get("reserved"));
                        officeMap.put("selected", tuple.get("selected"));
                        officeMap.put("room_id", tuple.get("room_id"));

                        finalList.add(officeMap);
                    }
                    returnListFuture.put("data", finalList);
                    returnListFuture.put("total", listAll.size());
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

}
