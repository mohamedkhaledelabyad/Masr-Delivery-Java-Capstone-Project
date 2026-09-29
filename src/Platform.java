import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Platform {
    private final Map<String, Restaurant> restaurants = new HashMap<>();
    private final Map<String, Customer> customers = new HashMap<>();
    private final Map<String, Rider> riders = new HashMap<>();
    private final Map<String, Order> orders = new HashMap<>();
    private final Map<String, Promotion> promotions = new HashMap<>();
    private final Set<String> cuisines = new HashSet<>();

    private final PriorityQueue<Order> readyOrders = new PriorityQueue<>(
            Comparator.comparing((Order order) ->
                            order.getCustomer().getTier() != LoyaltyTier.GOLD)
                    .thenComparing(Order::getPlacedAt)
    );

    public void addRestaurant(Restaurant restaurant) {
        if (restaurants.containsKey(restaurant.getId())) {
            throw new ValidationException("Duplicate restaurant id");
        }

        restaurants.put(restaurant.getId(), restaurant);
        cuisines.addAll(restaurant.getCuisines());
    }

    public void addCustomer(Customer customer) {
        customers.put(customer.getId(), customer);
    }

    public void addRider(Rider rider) {
        riders.put(rider.getId(), rider);
    }

    public void addPromotion(Promotion promotion) {
        promotions.put(promotion.getCode(), promotion);
    }

    public Promotion getPromotion(String code) {
        return promotions.get(code.toUpperCase());
    }

    public void placeOrder(Order order) {
        if (!order.getRestaurant().isOpen()) {
            throw new RestaurantClosedException("Restaurant is closed");
        }

        if (!order.getCustomer().getAddresses().contains(order.getAddress())) {
            throw new ValidationException("Address does not belong to customer");
        }

        for (LineItem line : order.getLineItems()) {
            line.getItem().reserveStock(line.getQuantity());
        }

        try {
            order.calculateTotal();
            order.addObserver(new NotificationObserver());
            order.addObserver(new AuditObserver());
            orders.put(order.getId(), order);

        } catch (RuntimeException e) {
            for (LineItem line : order.getLineItems()) {
                line.getItem().restoreStock(line.getQuantity());
            }

            throw e;
        }
    }

    public void payFromWallet(Order order) {
        if (order.isPaid()) {
            throw new ValidationException("Order is already paid");
        }

        order.getCustomer().pay(order.getTotal());
        order.markPaid();
    }

    public void cancelOrder(Order order) {
        order.changeStatus(OrderStatus.CANCELLED);

        if (order.isPaid()) {
            order.getCustomer().refund(order.getTotal());
        }

        for (LineItem line : order.getLineItems()) {
            line.getItem().restoreStock(line.getQuantity());
        }
    }

    public void addReadyOrder(Order order) {
        readyOrders.add(order);
    }

    public Order assignNextOrder(Rider rider) {
        if (rider.getActiveOrder() != null) {
            throw new RiderBusyException("Rider already has an order");
        }

        List<Order> skipped = new ArrayList<>();

        while (!readyOrders.isEmpty()) {
            Order order = readyOrders.poll();

            if (rider.canTake(order)) {
                readyOrders.addAll(skipped);

                rider.assign(order);
                order.setRider(rider);
                order.changeStatus(OrderStatus.ASSIGNED);

                return order;
            }

            skipped.add(order);
        }

        readyOrders.addAll(skipped);
        throw new ValidationException("No eligible ready order");
    }

    public void completeDelivery(Rider rider) {
        Order order = rider.getActiveOrder();

        if (order == null) {
            throw new ValidationException("Rider has no active order");
        }

        order.changeStatus(OrderStatus.DELIVERED);
        order.getCustomer().addCompletedOrder();
        rider.completeDelivery();
    }

    public List<Restaurant> searchRestaurants(Predicate<Restaurant> filter) {
        return restaurants.values().stream()
                .filter(Restaurant::isOpen)
                .filter(filter)
                .sorted(Comparator.comparing(Restaurant::getRating)
                        .reversed()
                        .thenComparing(Restaurant::getName))
                .collect(Collectors.toList());
    }

    public BigDecimal totalRevenue(LocalDate from, LocalDate to) {
        return orders.values().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .filter(order -> {
                    LocalDate date = order.getPlacedAt().toLocalDate();
                    return !date.isBefore(from) && !date.isAfter(to);
                })
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Restaurant> topFiveRestaurantsByRevenue(YearMonth month) {
        return orders.values().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .filter(order -> YearMonth.from(order.getPlacedAt()).equals(month))
                .collect(Collectors.groupingBy(
                        Order::getRestaurant,
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotal, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<Restaurant, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Map<String, Double> averageOrderValuePerDistrict() {
        return orders.values().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .collect(Collectors.groupingBy(
                        order -> order.getAddress().getDistrict(),
                        Collectors.averagingDouble(order -> order.getTotal().doubleValue())
                ));
    }

    public Map<OrderStatus, Long> ordersByStatus() {
        return orders.values().stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
    }

    public Optional<MenuItem> mostOrderedItem() {
        return orders.values().stream()
                .flatMap(order -> order.getLineItems().stream())
                .collect(Collectors.groupingBy(
                        LineItem::getItem,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                LineItem::getQuantity,
                                BigDecimal::add
                        )
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public Optional<Integer> peakOrderingHour() {
        return orders.values().stream()
                .collect(Collectors.groupingBy(
                        order -> order.getPlacedAt().getHour(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public List<Order> customerHistory(Customer customer) {
        return orders.values().stream()
                .filter(order -> order.getCustomer().equals(customer))
                .sorted(Comparator.comparing(Order::getPlacedAt).reversed())
                .collect(Collectors.toList());
    }

    public BigDecimal customerTotalSpent(Customer customer) {
        return customerHistory(customer).stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Customer> inactiveCustomers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        return customers.values().stream()
                .filter(customer -> orders.values().stream()
                        .filter(order -> order.getCustomer().equals(customer))
                        .map(Order::getPlacedAt)
                        .max(Comparator.naturalOrder())
                        .map(lastOrder -> lastOrder.isBefore(cutoff))
                        .orElse(true))
                .collect(Collectors.toList());
    }
}
