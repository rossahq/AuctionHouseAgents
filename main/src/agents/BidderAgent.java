package agents;
import auctionItems.AuctionItem;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

public class BidderAgent extends Agent {

    private AID auctioneerAgent;
    private ArrayList<AuctionItem> auctionItems;

    protected void setup() {
        System.out.println("Hello! Bidder-agent " + getAID().getName() + " is ready");

        Object[] items = getArguments();

        auctionItems = getAuctionItems(items);

        if (auctionItems.size() == 0) {
            doDelete();
        }

        try {
            registerWithDF();
            getAuctioneerAgent();
        } catch (FIPAException e) {
            e.printStackTrace();
            doDelete();
        }

        addBehaviour(new RequestPerformer());

    }

    protected void takeDown() {
        System.out.println("Buyer-agent " + getAID().getName() + " terminating");
    }


    private void getAuctioneerAgent() throws FIPAException {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("auctioneer");
        dfd.addServices(sd);


        DFAgentDescription[] result = DFService.search(this, dfd);
        if (result.length > 0)
            auctioneerAgent = result[0].getName();

    }


    private void registerWithDF() throws FIPAException {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("bidder");
        sd.setName(getLocalName());
        dfd.addServices(sd);

        DFService.register(this, dfd);

        System.out.println("Bidder registered with DF Agent");
    }

    private ArrayList<AuctionItem> getAuctionItems(Object[] items) {
        ArrayList<AuctionItem> auctionItems = new ArrayList<>();
        try {
            for (int i = 0; i < items.length; i++) {
                auctionItems.add((AuctionItem) items[i]);
                System.out.println("I want to buy: " + auctionItems.get(i).desc + " for: " + auctionItems.get(i).price.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return auctionItems;
    }

    private class RequestPerformer extends Behaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it
                String itemName = msg.getContent();
                ACLMessage reply = msg.createReply();

                for(int i=0; i<auctionItems.size();i++){
                    System.out.println("looking at auction : " + auctionItems.get(i).desc);
                    System.out.println(itemName);
                    if(auctionItems.get(i).desc.equals(itemName)){
                        System.out.println("Bidding on " + itemName);
                        //bidder wants the item and proposes a price
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContent(String.valueOf(auctionItems.get(i).price));
                    }else {
                        // The bidder does not want the item.
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("declined");
                    }
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }

}
