package agents;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

public class AuctionerAgent extends Agent {

    private Hashtable<String, Integer> catalogue;

    protected void setup() {
        System.out.println("Hello! Auctioneer-agent " + getAID().getName() + " is ready");

        registerWithDF();

        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //addBehaviour(new OfferRequestServer());
        // addBehaviour(new PurchaseOrderServer());
    }

    protected void takeDown() {
        System.out.println("Buyer-agent " + getAID().getName() + " terminating");
    }


    private void registerWithDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName( getAID() );
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "auctioneer" );
        sd.setName( getLocalName() );
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd );
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Auctioneer registered with DF Agent");
    }

}
