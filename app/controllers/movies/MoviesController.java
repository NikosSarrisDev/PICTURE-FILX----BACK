package controllers.movies;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.execition_context.DatabaseExecutionContext;
import models.Movies.Movie;
import models.actors.Actor;
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

public class MoviesController extends Controller {

    private final JPAApi jpaApi;
    private final ObjectMapper objectMapper;
    private final DatabaseExecutionContext executionContext;

    @Inject()
    public MoviesController(JPAApi jpaApi, ObjectMapper objectMapper, DatabaseExecutionContext executionContext){
        this.jpaApi = jpaApi;
        this.objectMapper = objectMapper;
        this.executionContext = executionContext;
    }

    //add Movies
    @SuppressWarnings({"unchecked", "duplicates"})
    public Result addMovie(final Http.Request request) throws IOException {

        JsonNode json = request.body().asJson();

        if(json == null){
            return badRequest("Invalid Json Format");
        }
        else {
            ObjectNode result = Json.newObject();
            try{

                CompletableFuture<JsonNode> addFuture = CompletableFuture.supplyAsync(() -> {

                    return jpaApi.withTransaction(entityManager -> {
                         ObjectNode resultOfFuture = Json.newObject();

                         String title = json.findPath("title").asText();
                         String description = json.findPath("description").asText();
                         String director = json.findPath("director").asText();
                         String producer = json.findPath("producer").asText();
                         double rating = json.findPath("rating").asDouble();
                         int duration = json.findPath("duration").asInt();
                         String type = json.findPath("type").asText();
                         String trailerCode = json.findPath("trailerCode").asText();
                         String rated = json.findPath("rated").asText();
                         String wikiLink = json.findPath("wikiLink").asText();
                         String releaseDate = json.findPath("releaseDate").asText();
                         int ticketCount = json.findPath("ticketCount").asInt();
                         String thumbnail = json.findPath("thumbnail").asText();

                        //Convert Actors JSON to Set in order to persist it
                        Set<Actor> actors = new HashSet<>();
                        JsonNode actorsNode = json.findPath("actors");
                        if (actorsNode.isArray()) {
                            for (JsonNode actorNode : actorsNode) {
                                String actorName = actorNode.findPath("name").asText();
                                Actor actor = new Actor();
                                actor.setName(actorName);
                                actors.add(actor);
                            }
                        }

                        Movie movie = new Movie();

                        movie.setTitle(title);
                        movie.setDescription(description);
                        movie.setDirector(director);
                        movie.setProducer(producer);
                        movie.setRating(rating);
                        movie.setDuration(duration);
                        movie.setType(type);
                        movie.setTrailerCode(trailerCode);
                        movie.setRated(rated);
                        movie.setWikiLink(wikiLink);
                        try{
                            movie.setReleaseDate(new SimpleDateFormat("yyyy-MM-dd").parse(releaseDate));
                        }catch (Exception e){
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Η ημερομηνία δεν έχει τη σωστή μορφή");
                            return resultOfFuture;
                        }
                        movie.setTicketCount(ticketCount);
                        movie.setThumbnail(thumbnail);
                        movie.setActors(actors);

                        entityManager.persist(movie);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", movie.getId());
                        resultOfFuture.put("system", "MOVIES_ACTIONS");
                        resultOfFuture.put("message", "Η καταχώρηση ολοκληρώθηκε με επιτυχία!");
                        return resultOfFuture;
                    });

                }, executionContext);

                result = (ObjectNode) addFuture.get();
                return ok(result);

            }catch (Exception e){
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Σφάλμα κατά την εισαγωγή");
                return ok(result);
            }
        }
    }

    @SuppressWarnings({"unchecked", "duplicates"})
    public Result updateMovie(final Http.Request request) throws IOException {

        JsonNode json = request.body().asJson();

        if(json == null){
            return badRequest("Invalid Json Format");
        }
        else {
            ObjectNode result = Json.newObject();
            try{

                CompletableFuture<JsonNode> updateFuture = CompletableFuture.supplyAsync(() -> {

                    return jpaApi.withTransaction(entityManager -> {
                        ObjectNode resultOfFuture = Json.newObject();

                        Long id = json.findPath("id").asLong();
                        String title = json.findPath("title").asText();
                        String description = json.findPath("description").asText();
                        String director = json.findPath("director").asText();
                        String producer = json.findPath("producer").asText();
                        double rating = json.findPath("rating").asDouble();
                        int duration = json.findPath("duration").asInt();
                        String type = json.findPath("type").asText();
                        String trailerCode  = json.findPath("trailerCode").asText();
                        String rated = json.findPath("rated").asText();
                        String wikiLink = json.findPath("wikiLink").asText();
                        String releaseDate = json.findPath("releaseDate").asText();
                        int ticketCount = json.findPath("ticketCount").asInt();
                        String thumbnail = json.findPath("thumbnail").asText();

                        //Convert Actors JSON to Set in order to persist it
                        Set<Actor> actors = new HashSet<>();
                        JsonNode actorsNode = json.findPath("actors");
                        if (actorsNode.isArray()) {
                            for (JsonNode actorNode : actorsNode) {
                                String actorName = actorNode.findPath("name").asText();
                                Actor actor = new Actor();
                                actor.setName(actorName);
                                actors.add(actor);
                            }
                        }

                        Movie movie = entityManager.find(Movie.class, id);

                        movie.setTitle(title);
                        movie.setDescription(description);
                        movie.setDirector(director);
                        movie.setProducer(producer);
                        movie.setRating(rating);
                        movie.setDuration(duration);
                        movie.setType(type);
                        movie.setTrailerCode(trailerCode);
                        movie.setRated(rated);
                        movie.setWikiLink(wikiLink);
                        try{
                            movie.setReleaseDate(new SimpleDateFormat("yyyy-MM-dd").parse(releaseDate));
                        }catch (Exception e){
                            resultOfFuture.put("status", "error");
                            resultOfFuture.put("message", "Η ημερομηνία δεν έχει τη σωστή μορφή");
                            return resultOfFuture;
                        }
                        movie.setTicketCount(ticketCount);
                        movie.setThumbnail(thumbnail);
                        movie.setActors(actors);

                        entityManager.merge(movie);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", movie.getId());
                        resultOfFuture.put("system", "MOVIES_ACTIONS");
                        resultOfFuture.put("message", "Η ενημέρωση ολοκληρώθηκε με επιτυχία!");
                        return resultOfFuture;
                    });

                }, executionContext);

                result = (ObjectNode) updateFuture.get();
                return ok(result);

            }catch (Exception e){
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Σφάλμα κατά την ενημέρωση");
                return ok(result);
            }
        }
    }

    @SuppressWarnings({"unchecked", "duplicates"})
    public Result deleteMovie(final Http.Request request) throws IOException {

        JsonNode json = request.body().asJson();

        if(json == null){
            return badRequest("Invalid Json Format");
        }
        else {
            ObjectNode result = Json.newObject();
            try{

                CompletableFuture<JsonNode> deleteFuture = CompletableFuture.supplyAsync(() -> {

                    return jpaApi.withTransaction(entityManager -> {
                        ObjectNode resultOfFuture = Json.newObject();

                        Long id = json.findPath("id").asLong();

                        Movie movie = entityManager.find(Movie.class, id);

                        entityManager.remove(movie);

                        resultOfFuture.put("status", "success");
                        resultOfFuture.put("DO_IT", movie.getId());
                        resultOfFuture.put("system", "MOVIES_ACTIONS");
                        resultOfFuture.put("message", "Η διαγραφή ολοκληρώθηκε με επιτυχία!");
                        return resultOfFuture;
                    });

                }, executionContext);

                result = (ObjectNode) deleteFuture.get();
                return ok(result);

            }catch (Exception e){
                e.printStackTrace();
                result.put("status", "error");
                result.put("message", "Σφάλμα κατά την διαγραφή");
                return ok(result);
            }
        }
    }

    @SuppressWarnings({"unchecked", "duplicates"})
    public Result getMovie(final Http.Request request) throws IOException, ExecutionException, InterruptedException{

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
                        String director = json.findPath("director").asText();
                        String producer = json.findPath("producer").asText();
                        String releaseDate = json.findPath("releaseDate").asText();
                        String type = json.findPath("type").asText();
                        String rated = json.findPath("rated").asText();
                        String start = json.findPath("start").asText();
                        String limit = json.findPath("limit").asText();

                        String sql = "select * from movies m where 1=1";

                        if(title != null && !title.equalsIgnoreCase("") && !title.equalsIgnoreCase("null")){
                            sql += " and (m.title) like " + "'%" + title + "%'";
                        }
                        if(director != null && !director.equalsIgnoreCase("") && !director.equalsIgnoreCase("null")){
                            sql += " and (m.director) like " + "'%" + director + "%'";
                        }
                        if(producer != null && !producer.equalsIgnoreCase("") && !producer.equalsIgnoreCase("null")){
                            sql += " and (m.producer) like " + "'%" + producer + "%'";
                        }
                        if(releaseDate != null && !releaseDate.equalsIgnoreCase("") && !releaseDate.equalsIgnoreCase("null")){
                            sql += " and (m.release_date) like " + "'%" + releaseDate + "%'";
                        }
                        if(type != null && !type.equalsIgnoreCase("") && !type.equalsIgnoreCase("null")){
                            sql += " and (m.type) in " + "(" + type + ")";
                        }
                        if(rated !=null && !rated.equalsIgnoreCase("") && !rated.equalsIgnoreCase("null")){
                            sql += " and (m.rated) is " + rated;
                        }

                        //All the users till now i mean before the limit and order by
                        List<Movie> listAll = (List<Movie>) entityManager.createNativeQuery(sql, Movie.class).getResultList();

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
                        List<Movie> list = (List<Movie>) entityManager.createNativeQuery(sql, Movie.class).getResultList();

                        for (Movie movie : list) {
                            HashMap<String, Object> officeMap = new HashMap<>();
                            officeMap.put("id", movie.getId());
                            officeMap.put("orderCol", orderCol);
                            officeMap.put("descAsc", descAsc);
                            officeMap.put("title", movie.getTitle());
                            officeMap.put("director", movie.getDirector());
                            officeMap.put("producer", movie.getProducer());
                            officeMap.put("releaseDate", movie.getReleaseDate());
                            officeMap.put("type", movie.getType());
                            officeMap.put("rated", movie.getRated());
                            officeMap.put("wikiLink", movie.getWikiLink());
                            officeMap.put("rating", movie.getRating());
                            officeMap.put("duration", movie.getDuration());
                            officeMap.put("trailerCode", movie.getTrailerCode());
                            officeMap.put("description", movie.getDescription());
                            officeMap.put("thumbnail", movie.getThumbnail());

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
