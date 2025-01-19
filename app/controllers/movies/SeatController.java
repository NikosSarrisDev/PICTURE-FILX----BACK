package controllers.movies;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execition_context.DatabaseExecutionContext;
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
                        int row = json.findPath("row").asInt();
                        int col = json.findPath("col").asInt();
                        boolean reserved = json.findPath("reserved").asBoolean();
                        Long roomId = json.findPath("room_id").asLong();

                        //Use the room_id from the json to find that room
                        Room room = entityManager.find(Room.class, roomId);

                        Seat seat = new Seat();

                        seat.setTitle(title);
                        seat.setRow(row);
                        seat.setCol(col);
                        seat.setReserved(reserved);
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
                        Long roomId = json.findPath("room_id").asLong();

                        //Use the room_id from the json to find that room
                        Room room = entityManager.find(Room.class, roomId);

                        Seat seat = entityManager.find(Seat.class, id);

                        seat.setTitle(title);
                        seat.setRow(row);
                        seat.setCol(col);
                        seat.setReserved(reserved);
                        seat.setRoom(room);

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
                    String start = json.findPath("start").asText();
                    String limit = json.findPath("limit").asText();

                    String sql = "select * from seats s where 1=1";

                    if(title != null && !title.equalsIgnoreCase("") && !title.equalsIgnoreCase("null")){
                        sql += " and (r.title) like " + "'%" + title + "%'";
                    }

                    //All the users till now i mean before the limit and order by
                    List<Seat> listAll = (List<Seat>) entityManager.createNativeQuery(sql, Seat.class).getResultList();

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
                    List<Seat> list = (List<Seat>) entityManager.createNativeQuery(sql, Seat.class).getResultList();

                    for (Seat seat : list) {
                        HashMap<String, Object> officeMap = new HashMap<>();
                        officeMap.put("id", seat.getId());
                        officeMap.put("orderCol", orderCol);
                        officeMap.put("descAsc", descAsc);
                        officeMap.put("title", seat.getTitle());
                        officeMap.put("row", seat.getRow());
                        officeMap.put("col", seat.getCol());
                        officeMap.put("room_id", seat.getRoom());

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
