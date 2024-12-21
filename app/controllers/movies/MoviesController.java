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
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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

                CompletableFuture addFuture = CompletableFuture.supplyAsync(() -> {

                    return jpaApi.withTransaction(entityManager -> {
                         ObjectNode resultOfFuture = Json.newObject();

                         String title = json.findPath("title").asText();
                         String description = json.findPath("description").asText();
                         String director = json.findPath("director").asText();
                         String producer = json.findPath("producer").asText();
                         double rating = json.findPath("rating").asDouble();
                         int duration = json.findPath("duration").asInt();
                         int ticketCount = json.findPath("ticketCount").asInt();
                         String thumbnailBase64 = json.findPath("thumbnail").asText();

                         //Conversion to Base64 in order to persist them
                        byte[] thumbnail = null;
                        if (thumbnailBase64 != null && !thumbnailBase64.isEmpty()) {
                            thumbnail = Base64.getDecoder().decode(thumbnailBase64);
                        }

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

                CompletableFuture addFuture = CompletableFuture.supplyAsync(() -> {

                    return jpaApi.withTransaction(entityManager -> {
                        ObjectNode resultOfFuture = Json.newObject();

                        Long id = json.findPath("id").asLong();
                        String title = json.findPath("title").asText();
                        String description = json.findPath("description").asText();
                        String director = json.findPath("director").asText();
                        String producer = json.findPath("producer").asText();
                        double rating = json.findPath("rating").asDouble();
                        int duration = json.findPath("duration").asInt();
                        int ticketCount = json.findPath("ticketCount").asInt();
                        String thumbnailBase64 = json.findPath("thumbnail").asText();

                        //Conversion to Base64 in order to persist them
                        byte[] thumbnail = null;
                        if (thumbnailBase64 != null && !thumbnailBase64.isEmpty()) {
                            thumbnail = Base64.getDecoder().decode(thumbnailBase64);
                        }

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

                result = (ObjectNode) addFuture.get();
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

                CompletableFuture addFuture = CompletableFuture.supplyAsync(() -> {

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

                result = (ObjectNode) addFuture.get();
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
    public Result getMovie(final Http.Request request) throws IOException {

        JsonNode json = request.body().asJson();

        if(json == null){
            return badRequest("Invalid Json Format");
        }
        else {
            ObjectNode result = Json.newObject();
            try{

                CompletableFuture addFuture = CompletableFuture.supplyAsync(() -> {

                    return jpaApi.withTransaction(entityManager -> {
                        ObjectNode resultOfFuture = Json.newObject();

                        String title = json.findPath("title").asText();
                        String description = json.findPath("description").asText();
                        String director = json.findPath("director").asText();
                        String producer = json.findPath("producer").asText();
                        double rating = json.findPath("rating").asDouble();
                        int duration = json.findPath("duration").asInt();
                        int ticketCount = json.findPath("ticketCount").asInt();
                        String thumbnailBase64 = json.findPath("thumbnail").asText();

                        //Conversion to Base64 in order to persist them
                        byte[] thumbnail = null;
                        if (thumbnailBase64 != null && !thumbnailBase64.isEmpty()) {
                            thumbnail = Base64.getDecoder().decode(thumbnailBase64);
                        }

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

}
