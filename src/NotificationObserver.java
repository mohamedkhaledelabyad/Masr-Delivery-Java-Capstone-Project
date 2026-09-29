public class NotificationObserver implements OrderObserver {
    @Override
    public void update(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        System.out.println("[Notification] Order " + order.getId()
                + " changed to " + newStatus);
    }
}