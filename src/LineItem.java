import java.math.BigDecimal;

public class LineItem {
    private final MenuItem item;
    private final BigDecimal quantity;

    public LineItem(MenuItem item, BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Quantity must be greater than zero");
        }

        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem getItem() {
        return item;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
}
