package controllers.execition_context;

import akka.actor.ActorSystem;
import javax.inject.Inject;
import play.libs.concurrent.CustomExecutionContext;

public class DatabaseExecutionContext extends CustomExecutionContext {

    @Inject
    public DatabaseExecutionContext(ActorSystem actorSystem) {
        // Specifies the custom thread pool defined in application.conf
        super(actorSystem, "database.dispatcher");
    }

}
