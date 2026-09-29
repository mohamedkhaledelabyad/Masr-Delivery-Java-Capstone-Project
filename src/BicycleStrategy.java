import java.math.BigDecimal;

public class BicycleStrategy implements DispatchStrategy {
    @Override
    public boolean canDeliver(Order order) {
        return order.getTotal().compareTo(new BigDecimal("500")) <= 0;
    }
}
