package ppy.app.papaya;

import com.google.firebase.firestore.PropertyName;

public class IngredientItem {
    private String ingredientsName;
    private int ingredientsPrice;
    private String ingredientsType;
    private boolean isSelected;

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    // Firestore 需要無參數建構子
    public IngredientItem() {}

    public IngredientItem(String name, int price, String type) {
        this.ingredientsName = name;
        this.ingredientsPrice = price;
        this.ingredientsType = type;
    }

    @PropertyName("ingredients_name")
    public String getIngredientsName() {
        return ingredientsName;
    }

    @PropertyName("ingredients_name")
    public void setIngredientsName(String ingredientsName) {
        this.ingredientsName = ingredientsName;
    }

    @PropertyName("ingredients_price")
    public int getIngredientsPrice() {
        return ingredientsPrice;
    }

    @PropertyName("ingredients_price")
    public void setIngredientsPrice(int ingredientsPrice) {
        this.ingredientsPrice = ingredientsPrice;
    }

    @PropertyName("ingredients_type")
    public String getIngredientsType() {
        return ingredientsType;
    }

    @PropertyName("ingredients_type")
    public void setIngredientsType(String ingredientsType) {
        this.ingredientsType = ingredientsType;
    }
}
