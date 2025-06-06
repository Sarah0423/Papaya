package ppy.app.papaya;

public class DrinkItem {
    private String beverage_info;
    private String beverage_name;
    private String beverage_photo;
    private int beverage_price;
    private String beverage_type;
    private int beverage_index;

    // ✅ 必要的「無參數建構子」
    public DrinkItem() {}

    // ✅ 有參建構子（可選）
    public DrinkItem(int beverage_index, String beverage_info, String beverage_name, String beverage_photo, int beverage_price, String beverage_type) {
        this.beverage_info = beverage_info;
        this.beverage_name = beverage_name;
        this.beverage_photo = beverage_photo;
        this.beverage_price = beverage_price;
        this.beverage_type = beverage_type;
        this.beverage_index = beverage_index;
    }

    // ✅ Getter & Setter 都要有
    public int getBeverage_index() {
        return beverage_index;
    }

    public void setBeverage_index(int beverage_index) {
        this.beverage_index = beverage_index;
    }

    public String getBeverage_info() {
        return beverage_info;
    }

    public void setBeverage_info(String beverage_info) {
        this.beverage_info = beverage_info;
    }

    public String getBeverage_name() {
        return beverage_name;
    }

    public void setBeverage_name(String beverage_name) {
        this.beverage_name = beverage_name;
    }

    public String getBeverage_photo() {
        return beverage_photo;
    }

    public void setBeverage_photo(String beverage_photo) {
        this.beverage_photo = beverage_photo;
    }

    public int getBeverage_price() {
        return beverage_price;
    }

    public void setBeverage_price(int beverage_price) {
        this.beverage_price = beverage_price;
    }

    public String getBeverage_type() {
        return beverage_type;
    }

    public void setBeverage_type(String beverage_type) {
        this.beverage_type = beverage_type;
    }
}
