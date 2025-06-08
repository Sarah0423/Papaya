package ppy.app.papaya;

public class OrderItem {
    private String orderId;
    private String mapImageName;
    private long finalPrice;

    public OrderItem(String orderId, String mapImageName, long finalPrice) {
        this.orderId = orderId;
        this.mapImageName = mapImageName;
        this.finalPrice = finalPrice;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getMapImageName() {
        return mapImageName;
    }

    public long getFinalPrice() {
        return finalPrice;
    }
}
