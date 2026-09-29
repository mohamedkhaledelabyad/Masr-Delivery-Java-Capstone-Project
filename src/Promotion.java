import java.math.BigDecimal;

public interface Promotion {
    String getCode();
    void validate(Order order);
    BigDecimal calculateDiscount(Order order);
    boolean isFreeDelivery();
}
