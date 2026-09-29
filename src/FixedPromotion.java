import java.math.BigDecimal;
import java.time.LocalDate;

public class FixedPromotion extends BasePromotion {
    private final BigDecimal amount;

    public FixedPromotion(String code, BigDecimal amount,
                          BigDecimal minimumSubtotal,
                          LocalDate expiryDate, String district,
                          boolean firstOrderOnly) {

        super(code, minimumSubtotal, expiryDate, district, firstOrderOnly);
        this.amount = amount;
    }

    @Override
    public BigDecimal calculateDiscount(Order order) {
        return amount.min(order.getSubtotal());
    }
}
