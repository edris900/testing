package com.example.springcashier;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.util.Random;

@Entity
@Table(name = "orders")
@Data
@RequiredArgsConstructor

class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String drink;
    private String milk;
    private String size;
    private String total;
    private String register;
    private String status;

    public static Order GetNewOrder() {
        String[] drinks = {"Caffe Latte", "Caffe Americano", "Caffe Mocha", "Espresso", "Cappuccino"};
        String[] milks = {"Whole Milk", "2% Milk", "Nonfat Milk", "Almond Milk", "Soy Milk"};
        String[] sizes = {"Short", "Tall", "Grande", "Venti", "Your Own Cup"};

        Random random = new Random();
        DecimalFormat df = new DecimalFormat("#.00");

        Order o = new Order();

        int drinkIndex = random.nextInt(drinks.length);
        int milkIndex = random.nextInt(milks.length);
        int sizeIndex = random.nextInt(sizes.length);

        o.drink = drinks[drinkIndex];
        o.milk = milks[milkIndex];
        o.size = sizes[sizeIndex];
        o.status = "Ready for Payment";

        // Calculate the total price based on drink and size
        double totalPrice = calculatePrice(drinkIndex, sizeIndex);
        o.total = "$" + df.format(totalPrice);

        return o;
    }

    private static double calculatePrice(int drinkIndex, int sizeIndex) {
        double[][] prices = {
                {2.45, 2.95, 3.65, 3.95, 4.25},
                {1.85, 2.25, 2.65, 2.95, 3.25},
                {2.95, 3.45, 4.15, 4.45, 4.75},
                {1.55, 1.75, 1.95, 2.15, 2.35},
                {2.45, 2.95, 3.65, 3.95, 4.25}
        };

        return prices[drinkIndex][sizeIndex];
    }
//    Order class ends here
}
