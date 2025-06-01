package ppy.app.papaya;

public class CartItem {
    private String item_photo;
    private String item_name;
    private String item_selected;
    private String item_user_id;
    private int item_quantity;
    private int item_price;

    public CartItem() {
        // Firestore needs a public no-arg constructor
    }

    public String getItem_photo() { return item_photo; }
    public String getItem_name() { return item_name; }
    public String getItem_selected() { return item_selected; }
    public int getItem_quantity() { return item_quantity; }
    public int getItem_price() { return item_price; }
    public String getItem_id(){return item_user_id;}
}
