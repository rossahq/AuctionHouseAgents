package auctionItems;

public class AuctionItem<String, Integer> {
    public final String desc;
    public final Integer price;
    public AuctionItem(String description, Integer price) {
        this.desc = description;
        this.price = price;
    }
}