package admin.example.com.androidstudy;

import admin.example.com.annotation.Factory;

@Factory(type = Meal.class,id = "Calzone")
public class CalzonePizza implements Meal{

    @Override
    public float getPrice() {
        return 8.5f;
    }
}
