import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final String id;
    private final Customer customer;
    private final Restaurant restaurant;
    private final Address address;
    private final List<LineItem> lineItems;
    private final Promotion promotion;
    private final String notes;
    private final LocalDateTime placedAt;

    private OrderStatus status = OrderStatus.PLACED;
    private Rider rider;
    private boolean paid;

    private BigDecimal subtotal = new BigDecimal("0.00");
    private BigDecimal deliveryFee = new BigDecimal("0.00");
    private BigDecimal serviceFee = new BigDecimal("0.00");
    private BigDecimal promotionDiscount = new BigDecimal("0.00");
    private BigDecimal total = new BigDecimal("0.00");

    private final List<OrderObserver> observers = new ArrayList<>();

    private Order(Builder builder) {
        id = builder.id;
        customer = builder.customer;
        restaurant = builder.restaurant;
        address = builder.address;
        lineItems = new ArrayList<>(builder.lineItems);
        promotion = builder.promotion;
        notes = builder.notes;
        placedAt = LocalDateTime.now();
    }

    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    public void calculateTotal() {
        subtotal = lineItems.stream()
                .map(line -> line.getItem().calculatePrice(line.getQuantity()))
                .reduce(new BigDecimal("0.00"), BigDecimal::add);

        int distance = PlatformConfig.getInstance()
                .distance(restaurant.getDistrict(), address.getDistrict());

        deliveryFee = new BigDecimal("15.00");

        if (distance > 3) {
            deliveryFee = deliveryFee.add(
                    new BigDecimal(distance - 3).multiply(new BigDecimal("3.00"))
            );
        }

        if (customer.getTier() == LoyaltyTier.SILVER) {
            deliveryFee = deliveryFee.multiply(new BigDecimal("0.90"));
        } else if (customer.getTier() == LoyaltyTier.GOLD) {
            deliveryFee = BigDecimal.ZERO;
        }

        serviceFee = subtotal.multiply(new BigDecimal("0.10"));

        if (promotion != null) {
            promotion.validate(this);
            promotionDiscount = promotion.calculateDiscount(this);

            if (promotion.isFreeDelivery()) {
                deliveryFee = BigDecimal.ZERO;
            }
        }

        total = subtotal.add(deliveryFee)
                .add(serviceFee)
                .subtract(promotionDiscount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        subtotal = money(subtotal);
        deliveryFee = money(deliveryFee);
        serviceFee = money(serviceFee);
        promotionDiscount = money(promotionDiscount);
        total = money(total);
    }

    public void changeStatus(OrderStatus newStatus) {
        boolean valid = false;

        if (status == OrderStatus.PLACED) {
            valid = newStatus == OrderStatus.ACCEPTED
                    || newStatus == OrderStatus.CANCELLED;

        } else if (status == OrderStatus.ACCEPTED) {
            valid = newStatus == OrderStatus.PREPARING
                    || newStatus == OrderStatus.CANCELLED;

        } else if (status == OrderStatus.PREPARING) {
            valid = newStatus == OrderStatus.READY
                    || newStatus == OrderStatus.CANCELLED;

        } else if (status == OrderStatus.READY) {
            valid = newStatus == OrderStatus.ASSIGNED
                    || newStatus == OrderStatus.CANCELLED;

        } else if (status == OrderStatus.ASSIGNED) {
            valid = newStatus == OrderStatus.OUT_FOR_DELIVERY;

        } else if (status == OrderStatus.OUT_FOR_DELIVERY) {
            valid = newStatus == OrderStatus.DELIVERED;
        }

        if (!valid) {
            throw new InvalidTransitionException(
                    "Invalid transition: " + status + " -> " + newStatus
            );
        }

        OrderStatus oldStatus = status;
        status = newStatus;

        for (OrderObserver observer : observers) {
            observer.update(this, oldStatus, newStatus);
        }
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Restaurant getRestaurant() { return restaurant; }
    public Address getAddress() { return address; }
    public List<LineItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public BigDecimal getServiceFee() { return serviceFee; }
    public BigDecimal getPromotionDiscount() { return promotionDiscount; }
    public BigDecimal getTotal() { return total; }
    public boolean isPaid() { return paid; }

    public void setRider(Rider rider) {
        this.rider = rider;
    }

    public void markPaid() {
        paid = true;
    }

    public static class Builder {
        private final String id;
        private final Customer customer;
        private final Restaurant restaurant;
        private final Address address;
        private final List<LineItem> lineItems = new ArrayList<>();
        private Promotion promotion;
        private String notes = "";

        public Builder(String id, Customer customer,
                       Restaurant restaurant, Address address) {
            this.id = id;
            this.customer = customer;
            this.restaurant = restaurant;
            this.address = address;
        }

        public Builder addItem(MenuItem item, BigDecimal quantity) {
            lineItems.add(new LineItem(item, quantity));
            return this;
        }

        public Builder promotion(Promotion promotion) {
            this.promotion = promotion;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Order build() {
            if (lineItems.isEmpty()) {
                throw new ValidationException("Order must have at least one item");
            }

            return new Order(this);
        }
    }
}
