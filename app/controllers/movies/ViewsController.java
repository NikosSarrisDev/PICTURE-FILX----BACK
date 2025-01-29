package controllers.movies;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execition_context.DatabaseExecutionContext;
import models.Movies.Movie;
import models.View.View;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ViewsController extends Controller {

    private final JPAApi jpaApi;
    private final ObjectMapper objectMapper;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public ViewsController(JPAApi jpaApi, ObjectMapper objectMapper, DatabaseExecutionContext executionContext) {
        this.jpaApi = jpaApi;
        this.objectMapper = objectMapper;
        this.executionContext = executionContext;
    }

    public Result addView(final Http.Request request) throws IOException {
        JsonNode json = request.body().asJson();
        if (json == null) {
            return badRequest("Invalid Json Format");
        } else {

            ObjectNode result = Json.newObject();

            try {

                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {
                    return jpaApi.withTransaction(entityManager -> {

                        ObjectNode resultOfFuture = Json.newObject();

                        Long movie_id = json.findPath("movie_id").asLong();
                        Long room_id = json.findPath("room_id").asLong();
                        String startTimeStr = json.findPath("startTime").asText();
                        String endTimeStr = json.findPath("endTime").asText();
                        String dateStr = json.findPath("date").asText();

                        LocalTime startTime = LocalTime.parse(startTimeStr);
                        LocalTime endTime = LocalTime.parse(endTimeStr);
                        LocalDate date = LocalDate.parse(dateStr);

                        if (startTime.isAfter(endTime)) {
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Η ώρα αρχής δεν μπορεί να είναι μεγαλύτερη από την ώρα τέλους!");
                            return resultOfFuture;
                        }

                        Movie movie = entityManager.find(Movie.class, movie_id);
                        Room room = entityManager.find(Room.class, room_id);

                        if (movie == null) {
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Η ταινία δεν βρέθηκε");
                            return resultOfFuture;
                        }

                        if (room == null) {
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Η αίθουσα δεν βρέθηκε");
                            return resultOfFuture;
                        }

                        //Έλενχος αν το υπάρχει εκείνη τη στιγμή μια προβολή στη σιγκεκριμένη αίθουσα
                        String query = "SELECT COUNT(*) FROM views " +
                                "WHERE room_id = " + room_id +
                                " AND date = '" + date + "'" +
                                " AND ((start_time < '" + endTime + "' AND end_time > '" + startTime + "'))";


                        long count = ((Number) entityManager.createNativeQuery(query).getSingleResult()).longValue();

                        if (count > 0) {
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Το δωμάτιο αυτό είναι πιασμένο για εκείνη τη χρονική περίοδο");
                            return resultOfFuture;
                        }

                        View view = new View();

                        view.setStartTime(startTime);
                        view.setEndTime(endTime);
                        view.setDate(date);
                        view.setMovie(movie);
                        view.setRoom(room);

                        entityManager.persist(view);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", view.getId());
                        resultOfFuture.put("system", "VIEWS_ACTIONS");
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

    public Result updateView(final Http.Request request) throws IOException {

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
                        Long movie_id = json.findPath("movie_id").asLong();
                        Long room_id = json.findPath("room_id").asLong();
                        String startTimeStr = json.findPath("startTime").asText();
                        String endTimeStr = json.findPath("endTime").asText();
                        String dateStr = json.findPath("date").asText();

                        LocalTime startTime = LocalTime.parse(startTimeStr);
                        LocalTime endTime = LocalTime.parse(endTimeStr);
                        LocalDate date = LocalDate.parse(dateStr);

                        Movie movie = entityManager.find(Movie.class, movie_id);
                        Room room = entityManager.find(Room.class, room_id);

                        if (movie == null) {
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Η ταινία δεν βρέθηκε");
                            return resultOfFuture;
                        }

                        if (room == null) {
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Η αίθουσα δεν βρέθηκε");
                            return resultOfFuture;
                        }

                        //Έλενχος αν το υπάρχει εκείνη τη στιγμή μια προβολή στη σιγκεκριμένη αίθουσα
                        String query = "SELECT COUNT(*) FROM views " +
                                "WHERE room_id = " + room_id +
                                " AND date = '" + date + "'" +
                                " AND ((start_time < '" + endTime + "' AND end_time > '" + startTime + "'))";


                        long count = ((Number) entityManager.createNativeQuery(query).getSingleResult()).longValue();

                        if (count > 0) {
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Το δωμάτιο αυτό είναι πιασμένο για εκείνη τη χρονική περίοδο");
                            return resultOfFuture;
                        }

                        View view = entityManager.find(View.class, id);

                        view.setStartTime(startTime);
                        view.setEndTime(endTime);
                        view.setDate(date);
                        view.setMovie(movie);
                        view.setRoom(room);

                        entityManager.merge(view);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", view.getId());
                        resultOfFuture.put("system", "VIEWS_ACTIONS");
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

    public Result deleteView(final Http.Request request) throws IOException {

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

                        View view = entityManager.find(View.class, id);

                        entityManager.remove(view);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", view.getId());
                        resultOfFuture.put("system", "VIEWS_ACTIONS");
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

    public Result getView(final Http.Request request) throws IOException, ExecutionException, InterruptedException {

        JsonNode json = request.body().asJson();

        if (json == null) {
            return badRequest("Invalid Json Format");
        } else {
            ObjectNode result = Json.newObject();
            HashMap<String, Object> returnList = new HashMap<>();
            String jsonResult = "";


            CompletableFuture<HashMap<String, Object>> getFuture = CompletableFuture.supplyAsync(() -> {

                return jpaApi.withTransaction(entityManager -> {

                    String orderCol = json.findPath("orderCol").asText();
                    String descAsc = json.findPath("descAsc").asText();
                    Long id = json.findPath("id").asLong();
                    Long movieId = json.findPath("movie_id").asLong();
                    Long roomId = json.findPath("room_id").asLong();
                    String startTimeStr = json.findPath("startTime").asText();
                    String endTimeStr = json.findPath("endTime").asText();
                    String dateStr = json.findPath("date").asText();
                    String start = json.findPath("start").asText();
                    String limit = json.findPath("limit").asText();

                    LocalTime startTime = startTimeStr.isEmpty() ? null : LocalTime.parse(startTimeStr);
                    LocalTime endTime = endTimeStr.isEmpty() ? null : LocalTime.parse(endTimeStr);
                    LocalDate date = dateStr.isEmpty() ? null : LocalDate.parse(dateStr);

                    String sql = "SELECT * FROM views v WHERE 1=1";

                    if (date != null) {
                        sql += " and v.date = '" + date + "'";
                    }
                    if (id != null && id > 0) {
                        sql += " and v.id = " + id;
                    }
                    if (movieId != null && movieId > 0) {
                        sql += " and v.movie_id = " + movieId;
                    }
                    if (roomId != null && roomId > 0) {
                        sql += " and v.room_id = " + roomId;
                    }
                    if (startTime != null && endTime != null) {
                        sql += " and (v.start_time < '" + endTime + "' and v.end_time > '" + startTime + "')";
                    }

                    //All the users till now i mean before the limit and order by
                    List<View> listAll = (List<View>) entityManager.createNativeQuery(sql, View.class).getResultList();

                    if (orderCol != null && !orderCol.equalsIgnoreCase("")) {
                        sql += " order by " + orderCol + " " + descAsc;
                    } else {
                        sql += " order by id asc";
                    }
                    if (start != null && !start.equalsIgnoreCase("")) {
                        sql += " limit " + start + "," + limit;
                    }

                    HashMap<String, Object> returnListFuture = new HashMap<>();
                    List<HashMap<String, Object>> finalList = new ArrayList<>();
                    List<View> list = (List<View>) entityManager.createNativeQuery(sql, View.class).getResultList();

                    for (View view : list) {
                        HashMap<String, Object> officeMap = new HashMap<>();
                        officeMap.put("id", view.getId());
                        officeMap.put("movieId", view.getMovie().getId());
                        officeMap.put("roomId", view.getRoom().getId());
                        officeMap.put("startTime", view.getStartTime().toString());
                        officeMap.put("endTime", view.getEndTime().toString());
                        officeMap.put("date", view.getDate().toString());

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
