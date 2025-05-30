package ppy.app.papaya;

import com.google.firebase.firestore.PropertyName;

public class ToastItem {

    @PropertyName("toast_name")
    private String name;

    @PropertyName("toast_info")
    private String info;

    @PropertyName("toast_price")
    private int price;

    @PropertyName("toast_photo")
    private String imageName; // ← 不是 imageUrl，而是圖片名稱

    @PropertyName("toast_index")
    private int index;

    public ToastItem() {}

    public ToastItem(String name, String info, int price, String imageName,int index) {
        this.name = name;
        this.info = info;
        this.price = price;
        this.imageName = imageName;
        this.index = index;
    }

    @PropertyName("toast_name")
    public String getToastName() { return name; }

    @PropertyName("toast_info")
    public String getToastInfo() { return info; }

    @PropertyName("toast_price")
    public int getToastPrice() { return price; }

    @PropertyName("toast_photo")
    public String getToastImageName() { return imageName; }

    @PropertyName("toast_index")
    public int getToastIndex() { return index; }
}