import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        Platform platform = new Platform();

        // Restaurant
        Restaurant restaurant = new Restaurant(
                "R1",
                "Nile Kitchen",
                "Maadi",
                Arrays.asList("Egyptian", "Grill"),
                4.8,
                true
        );

        restaurant.addItem(
                MenuItemFactory.create(
                        "STANDARD",
                        "M1",
                        "Koshary",
                        new BigDecimal("60.00"),
                        "Main",
                        15,
                        true,
                        new BigDecimal("100")
                )
        );

        restaurant.addItem(
                MenuItemFactory.create(
                        "WEIGHTED",
                        "M2",
                        "Grilled Meat",
                        new BigDecimal("320.00"),
                        "Grill",
                        25,
                        true,
                        new BigDecimal("50")
                )
        );

        restaurant.addItem(
                MenuItemFactory.create(
                        "COMBO",
                        "M3",
                        "Family Combo",
                        new BigDecimal("180.00"),
                        "Combo",
                        20,
                        true,
                        new BigDecimal("30")
                )
        );

        // Customer
        Customer customer = new Customer(
                "C1",
                "Ahmed",
                "01012345678",
                Arrays.asList(
                        new Address("Faisal", "12 El Haram Street")
                ),
                new BigDecimal("1000.00")
        );

        // Rider
        Rider rider = new Rider(
                "D1",
                "Mona",
                "Motorcycle",
                "Maadi"
        );

        // Promotion: 20% discount, maximum 50 EGP
        Promotion nile20 = new PercentagePromotion(
                "NILE20",
                new BigDecimal("20"),
                new BigDecimal("50"),
                BigDecimal.ZERO,
                LocalDate.now().plusDays(30),
                null,
                false
        );

        platform.addRestaurant(restaurant);
        platform.addCustomer(customer);
        platform.addRider(rider);
        platform.addPromotion(nile20);

        // 4 x 60 = 240 EGP
        // Maadi -> Faisal = 12 km
        // Expected total = 258 EGP
        Order order = new Order.Builder(
                "O1",
                customer,
                restaurant,
                customer.getAddresses().get(0)
        )
                .addItem(restaurant.getItem("M1"), new BigDecimal("4"))
                .promotion(platform.getPromotion("NILE20"))
                .notes("No onions")
                .build();

        platform.placeOrder(order);

        System.out.println("========== ORDER SUMMARY ==========");
        System.out.println("Order ID: " + order.getId());
        System.out.println("Subtotal: " + order.getSubtotal());
        System.out.println("Delivery fee: " + order.getDeliveryFee());
        System.out.println("Service fee: " + order.getServiceFee());
        System.out.println("Promotion discount: " + order.getPromotionDiscount());
        System.out.println("Total: " + order.getTotal());

        // Restaurant workflow
        order.changeStatus(OrderStatus.ACCEPTED);
        order.changeStatus(OrderStatus.PREPARING);
        order.changeStatus(OrderStatus.READY);

        // Dispatch
        platform.addReadyOrder(order);
        platform.assignNextOrder(rider);

        // Rider workflow
        order.changeStatus(OrderStatus.OUT_FOR_DELIVERY);
        platform.completeDelivery(rider);

        System.out.println("\n========== REPORTS ==========");
        System.out.println("Revenue: " + platform.totalRevenue(
                LocalDate.now(),
                LocalDate.now()
        ));

        System.out.println("Orders by status: " + platform.ordersByStatus());

        System.out.println("Most ordered item: " +
                platform.mostOrderedItem()
                        .map(MenuItem::getId)
                        .orElse("No orders"));

        System.out.println("Peak ordering hour: " +
                platform.peakOrderingHour()
                        .map(Object::toString)
                        .orElse("No orders"));

        System.out.println("Customer total spent: " +
                platform.customerTotalSpent(customer));
    }
}