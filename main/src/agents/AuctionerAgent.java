package agents;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

public class AuctionerAgent extends Agent {

    private Hashtable<String, Integer> catalogue;
    private AID[] bidderAgents;
    private String[] items;

    protected void setup() {
        System.out.println("Hello! Auctioneer-agent " + getAID().getName() + " is ready");

        registerWithDF();
        items = (String[]) getArguments();

        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Add a TickerBehaviour that schedules a request to seller agents every minute
        addBehaviour(new Behaviour(this) {
            @Override
            public void action() {
                System.out.println("Trying to sell " + items[0] );
                // Update the list of bidder agents
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("bidder");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    System.out.println("Found the following bidder agents:");
                    bidderAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        bidderAgents[i] = result[i].getName();
                        System.out.println(bidderAgents[i].getName());
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }

                //let bidders know
                addBehaviour(new RequestPerformer());
            }

            @Override
            public boolean done() {
                return false;
            }


            //addBehaviour(new OfferRequestServer());
            //addBehaviour(new PurchaseOrderServer());
        });
    }

    private class RequestPerformer extends Behaviour {
        private AID bestSeller; // The agent who provides the best offer
        private int bestPrice = 0;  // The best offered price
        private int repliesCnt = 0; // The counter of replies from seller agents
        private MessageTemplate mt; // The template to receive replies
        private int step = 0;

        public void action() {
            String convID = items[0] + "-auction";

            switch (step) {
                case 0:
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < bidderAgents.length; ++i) {
                        cfp.addReceiver(bidderAgents[i]);
                    }
                    cfp.setContent(items[0]);
                    cfp.setConversationId(convID);
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId(convID),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    // Receive all proposals/refusals from bidder agents
                    ACLMessage reply = myAgent.receive(mt);
                    System.out.print("Receiving bids...");

                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer
                            int price = Integer.parseInt(reply.getContent());
                            System.out.println("Taking a bid for: " + reply.getContent());
                            if (bestSeller == null || price < bestPrice) {
                                // This is the best offer at present
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= bidderAgents.length) {
                            // We received all replies
                            System.out.println(repliesCnt + " bids received");
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    // Sell the item to the bidder that provided the best offer
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(items[0]);
                    order.setConversationId(convID);
                    order.setReplyWith("sale" + System.currentTimeMillis());
                    myAgent.send(order);
                    // Prepare the template to get the purchase order reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    System.out.println("Item: " + items[0] + " sold to: " + bestSeller.getName() + " for: " + bestPrice);
                    break;
            }
        }

        @Override
        public boolean done() {
            if (step == 3)
                doDelete();
            return false;
        }
    }


    protected void takeDown() {
        System.out.println("Auctioneer-agent " + getAID().getName() + " terminating");
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

