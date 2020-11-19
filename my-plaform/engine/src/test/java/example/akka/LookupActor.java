package example.akka;

import akka.actor.*;

public class LookupActor extends UntypedActor {
    private ActorRef targetActor = null;

    /**
     * java中的代码块，在构造方法调用之前调用
     * new LookupActor()
     */
    {
        targetActor = getContext().actorOf(Props.create(TargetActor.class), "targetActor");
    }

    @Override
    public void onReceive(Object msg) throws Throwable, Throwable {
        if(msg instanceof String){
            if("find".equals(msg)) {
                ActorSelection as = getContext().actorSelection("targetActor");
                //我告诉你
                as.tell(new Identify("A001"),getSelf());
            }
        }else if( msg instanceof ActorIdentity){
            ActorIdentity ai = (ActorIdentity)msg;
            if(ai.correlationId().equals("A001")){
                ActorRef   ref = ai.getRef();
                if(ref!=null){
                    System.out.println("ActorIdentity:"+ai.correlationId()+" "+ref);
                    ref.tell("hello targetActor",getSelf());
                }
            }

        }
    }


    /**
     * 在actorsystem中查找一个actor
     * @param args
     */
    public static void main(String[] args) {
        //
        ActorSystem actorSystem = ActorSystem.create("sys");
        ActorRef lookupActor = actorSystem.actorOf(Props.create(LookupActor.class),"lookupActor");
        lookupActor.tell("find",ActorRef.noSender());
    }
}
