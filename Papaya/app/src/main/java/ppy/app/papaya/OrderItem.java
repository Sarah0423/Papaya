package ppy.app.papaya;

public class OrderItem {
    private String orderId;
    private String mapImageName;
    private long finalPrice;
    private String address;
    private long orderTimeMillis;

    public OrderItem(String orderId, String mapImageName, long finalPrice, String address, long orderTimeMillis) {
        this.orderId = orderId;
        this.mapImageName = mapImageName;
        this.finalPrice = finalPrice;
        this.address = address;
        this.orderTimeMillis = orderTimeMillis;
    }

    public String getOrderId() { return orderId; }

    public String getMapImageName() { return mapImageName; }

    public long getFinalPrice() { return finalPrice; }

    public String getAddress() { return address; }
    public long getOrderTimeMillis() { return orderTimeMillis; }
}
