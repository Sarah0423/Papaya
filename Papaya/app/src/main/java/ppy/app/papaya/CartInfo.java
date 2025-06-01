package ppy.app.papaya;

public class CartInfo {
    private long total_price;
    private int total_quantity;

    public CartInfo() {}  // Firestore 用

    public CartInfo(long total_price, int total_quantity) {
        this.total_price = total_price;
        this.total_quantity = total_quantity;
    }

    public long getTotal_price() { return total_price; }
    public int getTotal_quantity() { return total_quantity; }
}
