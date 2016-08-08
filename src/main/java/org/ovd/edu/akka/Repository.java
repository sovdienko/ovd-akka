package org.ovd.edu.akka;

import akka.actor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


import static java.lang.String.format;

/**
 * Created by Sergey.Ovdienko on 06.08.2016.
 */
public class Repository extends UntypedActor{


    private static final Logger logger = LoggerFactory.getLogger(Worker.class);
    private ActorRef mailService;
    private String port;

    public static void main(String[] args) {
        final String[] ports = args.length == 0 ? new String[]{"2561", "2562"} : args;
        startup(ports);
    }

    public static void startup(String[] ports){
        for (String port: ports) {
            final Config config = ConfigFactory
                .parseString("akka.remote.netty.tcp.port=" + port)
                .withFallback(ConfigFactory.load().getConfig("repository"));

            ActorSystem actorSystem = ExtendedActorSystem.create("ClusterSystem", config);
            actorSystem.actorOf(Props.create(Repository.class, port), "repository");
            actorSystem.actorOf(Props.create(SimpleClusterListener.class));

        }
    }

    public Repository(String port){
        this.port = port;
    }


    public void save(String message) throws InterruptedException {
        // Имитация бурной деятельности
        Thread.sleep(100);
    };


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String){
            save(message.toString());
            mailService.tell(format("%s, (%s)",message, port), sender());
        }
    }

    @Override
    public void preStart(){
        logger.info("Start Repository");
         mailService = context().actorOf(Props.create(MailService.class));
    }

    @Override
    public void postStop(){
        logger.info("Stop Repository");
    }
}
