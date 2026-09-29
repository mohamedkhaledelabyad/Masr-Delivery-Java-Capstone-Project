import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Rider {
    private final String id;
    private final String name;
    private final String vehicleType;
    private final String district;
    private boolean available = true;
    private int completedDeliveries;
    private Order activeOrder;
    private final List<Duration> deliveryDurations = new ArrayList<>();
    private final DispatchStrategy strategy;

    public Rider(String id, String name, String vehicleType, String district) {
        this.id = id;
        this.name = name;
        this.vehicleType = vehicleType;
        this.district = district;

        if (vehicleType.equalsIgnoreCase("bicycle")) {
            strategy = new BicycleStrategy();
        } else if (vehicleType.equalsIgnoreCase("car")) {
            strategy = new CarStrategy();
        } else {
            strategy = new MotorcycleStrategy();
        }
    }

    public boolean canTake(Order order) {
        return available && activeOrder == null && strategy.canDeliver(order);
    }

    public void assign(Order order) {
        if (!canTake(order)) {
            throw new RiderBusyException("Rider is busy or not eligible");
        }

        activeOrder = order;
        available = false;
    }

    public void completeDelivery() {
        completedDeliveries++;

        deliveryDurations.add(Duration.between(
                activeOrder.getPlacedAt(),
                LocalDateTime.now()
        ));

        activeOrder = null;
        available = true;
    }

    public String getId() {
        return id;
    }

    public Order getActiveOrder() {
        return activeOrder;
    }

    public int getCompletedDeliveries() {
        return completedDeliveries;
    }

    public Duration getAverageDuration() {
        if (deliveryDurations.isEmpty()) {
            return Duration.ZERO;
        }

        long seconds = (long) deliveryDurations.stream()
                .mapToLong(Duration::toSeconds)
                .average()
                .orElse(0);

        return Duration.ofSeconds(seconds);
    }

    @Override
    public String toString() {
        return name + " | Completed: " + completedDeliveries
                + " | Average duration: " + getAverageDuration().toMinutes() + " min";
    }
}