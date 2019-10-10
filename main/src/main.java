import agents.AuctionerAgent;
import agents.BidderAgent;
import auctionItems.AuctionItem;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class main {



    public static void main(String[] args) {
        Profile myProfile = new ProfileImpl();
        Runtime myRuntime = Runtime.instance();

        ContainerController myContainer = myRuntime.createMainContainer(myProfile);
        try {

            AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
            rma.start();

            String[] sellerCatalogue = {"book"};

            AgentController myAgent = myContainer.createNewAgent("auctioneer",
                    AuctionerAgent.class.getCanonicalName(), sellerCatalogue);
            myAgent.start();

            Object[] bidder1Items = {new AuctionItem<>("book", 5)
            };

            AgentController myBidderAgent = myContainer.createNewAgent("bidder",
                    BidderAgent.class.getCanonicalName(),bidder1Items );
            myBidderAgent.start();

            Object[] bidder2Items = {new AuctionItem<>("book", 2)
            };


            AgentController myBidderAgent2 = myContainer.createNewAgent("bidder2",
                    BidderAgent.class.getCanonicalName(),bidder2Items );
            myBidderAgent2.start();


        } catch (Exception e) {
            System.out.println("Exception while starting agent" + e.toString());
        }
    }




}
