public class MotorcycleStrategy implements DispatchStrategy {
    @Override
    public boolean canDeliver(Order order) {
        return true;
    }
}