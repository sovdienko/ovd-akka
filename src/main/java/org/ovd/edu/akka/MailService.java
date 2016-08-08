package org.ovd.edu.akka;


import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import static akka.actor.ActorRef.noSender;



/**
 * Created by Sergey.Ovdienko on 06.08.2016.
 */
public class MailService extends UntypedActor{

    //private Logger logger = LoggerFactory.getLogger(Worker.class);
    private LoggingAdapter logger = Logging.getLogger(context().system(), this);


    public void send(String message) throws InterruptedException {
        // Имитация бурной деятельности
        Thread.sleep(100);
    };


    @Override
    public void preStart() throws Exception {
        logger.info("Start MailService");
    }

    @Override
    public void postStop() throws Exception {
        logger.info("Stop MailService");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof String){
            send(message.toString());
            sender().tell(message, noSender());
        }

    }
}


