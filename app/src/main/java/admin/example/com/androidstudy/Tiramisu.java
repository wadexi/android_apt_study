package admin.example.com.androidstudy;

import admin.example.com.annotation.Factory;

@Factory(type = Meal.class,id = "Tiramisu")
public class Tiramisu implements Meal{

    @Override
    public float getPrice() {
        return 4.5f;
    }
}
