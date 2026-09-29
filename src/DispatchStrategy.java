public interface DispatchStrategy {
    boolean canDeliver(Order order);
}