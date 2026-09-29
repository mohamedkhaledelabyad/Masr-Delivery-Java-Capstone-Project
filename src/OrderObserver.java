public interface OrderObserver {
    void update(Order order, OrderStatus oldStatus, OrderStatus newStatus);
}
