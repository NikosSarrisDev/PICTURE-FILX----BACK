package controllers.movies;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execition_context.DatabaseExecutionContext;
import jakarta.persistence.Tuple;
import models.room.Room;
import models.seat.Seat;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SeatController extends Controller {

    private final JPAApi jpaApi;
    private final ObjectMapper objectMapper;
    private final DatabaseExecutionContext executionContext;

    @Inject()
    public SeatController(JPAApi jpaApi, ObjectMapper objectMapper, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.objectMapper = objectMapper;
        this.executionContext = executionContext;
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
                    String selected = json.findPath("selected").asText();
                    String start = json.findPath("start").asText();
                    String limit = json.findPath("limit").asText();

                    String sql = "select s.id, s.rowSeat, s.colSeat, s.room_id, s.title, s.selected AS seat_title, r.title AS room_title from seats s join rooms r on s.room_id = r.id where 1=1";

                    if(title != null && !title.equalsIgnoreCase("") && !title.equalsIgnoreCase("null")){
                        sql += " and (s.title) like " + "'%" + title + "%'";
                    }
                    if(roomTitle != null && !roomTitle.equalsIgnoreCase("") && !roomTitle.equalsIgnoreCase("null")){
                        sql += " and (r.title) like " + "'%" + roomTitle + "%'";
                    }
                    if(selected != null){
                        sql += " and (s.selected) = " + selected;
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
