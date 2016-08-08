package org.ovd.edu.akka;


import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.perf4j.slf4j.Slf4JStopWatch;
import scala.concurrent.duration.Duration;

import static akka.actor.ActorRef.noSender;
import static scala.concurrent.duration.Duration.Zero;


/**
 * Created by Sergey.Ovdienko on 05.08.2016.
 */
public class Client extends UntypedActor {

   // Logger logger = LoggerFactory.getLogger(Client.class);
    private LoggingAdapter logger = Logging.getLogger(context().system(), this);

    private ActorSelection worker;

    public static void main(String args[]) throws Exception {
        startSystem();
    }

    private static void startSystem(){
        final Config config = ConfigFactory.load().getConfig("client");
        ActorSystem actorSystem = ActorSystem.create("ClientSystem",config);
        actorSystem.actorOf(Props.create(Client.class));
    }

    @Override
    public void preStart() throws Exception{
        logger.info("Start Client");
        final ActorSystem actorSystem = context().system();
        actorSystem.scheduler().schedule(
            Zero(),
            Duration.apply(100, "millisecond"),
            self(),
            new Start(),
            actorSystem.dispatcher(),
            noSender()
        );
        final String path = "akka.tcp://ClusterSystem@127.0.0.1:2550/user/worker";
        worker = actorSystem.actorSelection(path);
    }


    private static class Start {
    }

    @Override
    public void postStop() {
        worker.tell(PoisonPill.getInstance(), noSender());
        context().system().shutdown();
        logger.info("Stop Client");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Start) {
            worker.tell(makeRequest(), self());
        }
        if (message instanceof String){
            Slf4JStopWatch stopWatch = new Slf4JStopWatch("Client");
            System.out.println(message);
            stopWatch.stop();
        }
    }


    private String makeRequest(){return "Hello";}
}
