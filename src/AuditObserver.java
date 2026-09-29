public class AuditObserver implements OrderObserver {
    @Override
    public void update(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        System.out.println("[Audit] " + order.getId()
                + ": " + oldStatus + " -> " + newStatus);
    }
}