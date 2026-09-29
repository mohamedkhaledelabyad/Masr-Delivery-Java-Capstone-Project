public class CarStrategy implements DispatchStrategy {
    @Override
    public boolean canDeliver(Order order) {
        return true;
    }
}