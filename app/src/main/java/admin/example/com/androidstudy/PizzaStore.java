package admin.example.com.androidstudy;

import java.util.Scanner;

public class PizzaStore {
    private MealFactory factory = new MealFactory();

    public Meal order(String mealName) {
        return factory.create(mealName);
    }

    private static String readConsole() {
        Scanner scanner = new Scanner(System.in);
        String meal = scanner.nextLine();
        scanner.close();
        return meal;
    }

    public static void main(String[] args) {
        System.out.println("welcome to pizza store");
        PizzaStore pizzaStore = new PizzaStore();
        Meal meal = pizzaStore.order(readConsole());
        System.out.println("Bill:$" + meal.getPrice());
    }

}
