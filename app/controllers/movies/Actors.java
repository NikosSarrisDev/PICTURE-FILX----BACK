package controllers.movies;

import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.execition_context.DatabaseExecutionContext;
import play.db.jpa.JPAApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;

public class Actors extends Controller {

    private final JPAApi jpaApi;
    private final ObjectMapper objectMapper;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public Actors(JPAApi jpaApi, ObjectMapper objectMapper, DatabaseExecutionContext executionContext){
        this.jpaApi = jpaApi;
        this.objectMapper = objectMapper;
        this.executionContext = executionContext;
    }

//    public Result addActor(final Http.Request) throws IOException {
//
//    }
//
//    public Result updateActor(final Http.Request) throws IOException {
//
//    }
//
//    public Result deleteActor(final Http.Request) throws IOException {
//
//    }
//
//    public Result getActor(final Http.Request) throws IOException {
//
//    }

}
