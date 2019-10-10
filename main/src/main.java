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

//            String[] sellerCatalogue = {"computer", "book", "table", "chair"};

            AgentController myAgent = myContainer.createNewAgent("auctioneer",
                    AuctionerAgent.class.getCanonicalName(), null);
            myAgent.start();

            Object[] items = {new AuctionItem<>("book", 4)
            };

            AgentController myBidderAgent = myContainer.createNewAgent("bidder",
                    BidderAgent.class.getCanonicalName(),items );
            myBidderAgent.start();


        } catch (Exception e) {
            System.out.println("Exception while starting agent" + e.toString());
        }
    }




}
