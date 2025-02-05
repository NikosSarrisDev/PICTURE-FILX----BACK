package controllers.movies;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execition_context.DatabaseExecutionContext;
import models.room.Room;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RoomsController extends Controller {

    private final JPAApi jpaApi;
    private final ObjectMapper objectMapper;
    private final DatabaseExecutionContext executionContext;

    @Inject()
    public RoomsController(JPAApi jpaApi, ObjectMapper objectMapper, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.objectMapper = objectMapper;
        this.executionContext = executionContext;
    }

    public Result addRoom(final Http.Request request) throws IOException {
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
                        String description = json.findPath("description").asText();
                        String quickText = json.findPath("quickText").asText();
                        int availableNumberOfSeats = json.findPath("availableNumberOfSeats").asInt();
                        double ticketPrice = json.findPath("ticketPrice").asDouble();
                        String thumbnail = json.findPath("thumbnail").asText();
                        String image1 = json.findPath("image1").asText();
                        String image2 = json.findPath("image2").asText();
                        String image3 = json.findPath("image3").asText();
                        String image4 = json.findPath("image4").asText();
                        String image5 = json.findPath("image5").asText();

                        Room room = new Room();

                        room.setTitle(title);
                        room.setDescription(description);
                        room.setQuickText(quickText);
                        room.setAvailableNumberOfSeats(availableNumberOfSeats);
                        room.setThumbnail(thumbnail);
                        room.setImage1(image1);
                        room.setImage2(image2);
                        room.setImage3(image3);
                        room.setImage4(image4);
                        room.setImage5(image5);

                        entityManager.persist(room);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", room.getId());
                        resultOfFuture.put("system", "ROOMS_ACTIONS");
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

    public Result updateRoom(final Http.Request request) throws IOException {

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
                        String description = json.findPath("description").asText();
                        String quickText = json.findPath("quickText").asText();
                        int availableNumberOfSeats = json.findPath("availableNumberOfSeats").asInt();
                        double ticketPrice = json.findPath("ticketPrice").asDouble();
                        String thumbnail = json.findPath("thumbnail").asText();
                        String image1 = json.findPath("image1").asText();
                        String image2 = json.findPath("image2").asText();
                        String image3 = json.findPath("image3").asText();
                        String image4 = json.findPath("image4").asText();
                        String image5 = json.findPath("image5").asText();

                        Room room = entityManager.find(Room.class, id);

                        room.setTitle(title);
                        room.setDescription(description);
                        room.setQuickText(quickText);
                        room.setAvailableNumberOfSeats(availableNumberOfSeats);
                        room.setThumbnail(thumbnail);
                        room.setImage1(image1);
                        room.setImage2(image2);
                        room.setImage3(image3);
                        room.setImage4(image4);
                        room.setImage5(image5);

                        entityManager.merge(room);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", room.getId());
                        resultOfFuture.put("system", "ROOMS_ACTIONS");
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

    public Result deleteRoom(final Http.Request request) throws IOException {

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

                        Room room = entityManager.find(Room.class, id);

                        entityManager.remove(room);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", room.getId());
                        resultOfFuture.put("system", "ROOMS_ACTIONS");
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

    public Result getRoom(final Http.Request request) throws IOException, ExecutionException, InterruptedException{

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
                    String description = json.findPath("description").asText();
                    String quickText = json.findPath("quickText").asText();
                    String start = json.findPath("start").asText();
                    String limit = json.findPath("limit").asText();

                    String sql = "select * from rooms r where 1=1";

                    if(title != null && !title.equalsIgnoreCase("") && !title.equalsIgnoreCase("null")){
                        sql += " and (r.title) like " + "'%" + title + "%'";
                    }
                    if(quickText != null && !quickText.equalsIgnoreCase("") && !quickText.equalsIgnoreCase("null")){
                        sql += " and (r.quickText) like " + "'%" + quickText + "%'";
                    }

                    //All the users till now i mean before the limit and order by
                    List<Room> listAll = (List<Room>) entityManager.createNativeQuery(sql, Room.class).getResultList();

                    if(orderCol != null && !orderCol.equalsIgnoreCase("")){
                        sql += " order by " + orderCol + " " + descAsc;
                    }else {
                        sql += " order by id asc";
                    }
                    if(start != null && !start.equalsIgnoreCase("")){
                        sql += " limit " + start + "," + limit;
                    }

                    HashMap<String, Object> returnListFuture = new HashMap<>();
                    List<HashMap<String, Object>> finalList = new ArrayList<>();
                    List<Room> list = (List<Room>) entityManager.createNativeQuery(sql, Room.class).getResultList();

                    for (Room room : list) {
                        HashMap<String, Object> officeMap = new HashMap<>();
                        officeMap.put("id", room.getId());
                        officeMap.put("orderCol", orderCol);
                        officeMap.put("descAsc", descAsc);
                        officeMap.put("title", room.getTitle());
                        officeMap.put("description", room.getDescription());
                        officeMap.put("quickText", room.getQuickText());
                        officeMap.put("availableNumberOfSeats", room.getAvailableNumberOfSeats());
                        officeMap.put("ticketPrice", room.getTicketPrice());
                        officeMap.put("thumbnail", room.getThumbnail());
                        officeMap.put("image1", room.getImage1());
                        officeMap.put("image2", room.getImage2());
                        officeMap.put("image3", room.getImage3());
                        officeMap.put("image4", room.getImage4());
                        officeMap.put("image5", room.getImage5());

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
