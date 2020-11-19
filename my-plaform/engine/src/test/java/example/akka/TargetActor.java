package example.akka;


import akka.actor.UntypedActor;

public class TargetActor extends UntypedActor {
    @Override
    public void onReceive(Object msg) throws Throwable, Throwable {
        System.out.println("target receive:"+msg);
    }
}
