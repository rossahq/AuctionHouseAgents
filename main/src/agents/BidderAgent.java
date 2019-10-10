package agents;
import auctionItems.AuctionItem;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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

    /**
     * Inner class RequestPerformer.
     * This is the behaviour used by Book-buyer agents to request seller
     * agents the target book.
     */
    private class RequestPerformer extends Behaviour {
        // The template to receive replies
        private MessageTemplate mt;
        private int step = 0;

        public void action() {
            switch (step) {
                case 0:
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.addReceiver(auctioneerAgent);
                    cfp.setContent("");
                    cfp.setConversationId(auctionItems.get(0).desc + "bid");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId(auctionItems.get(0).desc + "bid"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // This tells us whether the item is available
                            boolean available  = Boolean.parseBoolean(reply.getContent());
                            if (available = true) {
                                step = 2;
                            } else {
                                doDelete();
                            }

            break;
            // Send the purchase order to the seller that provided the best offer
            ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            order.addReceiver(auctionerAgent);
            order.setContent(targetBookTitle);
            order.setConversationId("book-trade");
            order.setReplyWith("order" + System.currentTimeMillis());
            myAgent.send(order);
            // Prepare the template to get the purchase order reply
            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                    MessageTemplate.MatchInReplyTo(order.getReplyWith()));
            step = 3;
            break;
            case 3:
            // Receive the purchase order reply
            reply = myAgent.receive(mt);
            if (reply != null) {
                // Purchase order reply received
                if (reply.getPerformative() == ACLMessage.INFORM) {
                    // Purchase successful. We can terminate
                    System.out.println(targetBookTitle + " successfully purchased from agent " + reply.getSender().getName());
                    System.out.println("Price = " + bestPrice);
                    myAgent.doDelete();
                } else {
                    System.out.println("Attempt failed: requested book already sold.");
                }

                step = 4;
            } else {
                block();
            }
            break;
        }


        @Override
        public boolean done() {
            return (step == 4);
        }
    }

}
