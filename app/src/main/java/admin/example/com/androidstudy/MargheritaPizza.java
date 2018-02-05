package admin.example.com.androidstudy;

import admin.example.com.annotation.Factory;

@Factory(type=Meal.class,id = "Margherita")
public class MargheritaPizza implements Meal{

    @Override
    public float getPrice() {
        return 6.0f;
    }
}
