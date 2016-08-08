package org.ovd.edu.akka;




import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.ExtendedActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.contrib.pattern.ClusterReceptionistExtension;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope;
import akka.routing.FromConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import static akka.actor.ActorRef.noSender;
import static java.lang.String.format;
/**
 * Created by Sergey.Ovdienko on 06.08.2016.
 */
public class Worker extends UntypedActor {

    //private static final Logger logger = LoggerFactory.getLogger(Worker.class);
    private LoggingAdapter logger = Logging.getLogger(context().system(), this);

    private int count = 0;

    private ActorRef router;


    public static void main(String[] args) {

        final Config config = ConfigFactory.load().getConfig("worker");
        ActorSystem actorSystem = ExtendedActorSystem.create("ClusterSystem", config);
        final ActorRef worker = actorSystem.actorOf(Props.create(Worker.class), "worker");

        final ClusterReceptionistExtension receptionist = ClusterReceptionistExtension.get(actorSystem);
        receptionist.registerService(worker);

        actorSystem.actorOf(Props.create(SimpleClusterListener.class));
    }

    private ConsistentHashableEnvelope response(Object message) {
        final String response = format("%d: %s", count++, message);
        return new ConsistentHashableEnvelope(response, response);
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
           router.tell(response(message), sender());
        }
    }

    @Override
    public void preStart(){
        logger.info("Start Worker");
        router = context().actorOf(FromConfig.getInstance().props(),"router");
    }

    @Override
    public void postStop(){
        router.tell(PoisonPill.getInstance(), noSender());
        logger.info("Stop Worker");

    }


}
