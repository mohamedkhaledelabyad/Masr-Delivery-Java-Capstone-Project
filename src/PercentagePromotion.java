import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class PercentagePromotion extends BasePromotion {
    private final BigDecimal percentage;
    private final BigDecimal maximumDiscount;

    public PercentagePromotion(String code, BigDecimal percentage,
                               BigDecimal maximumDiscount,
                               BigDecimal minimumSubtotal,
                               LocalDate expiryDate, String district,
                               boolean firstOrderOnly) {

        super(code, minimumSubtotal, expiryDate, district, firstOrderOnly);
        this.percentage = percentage;
        this.maximumDiscount = maximumDiscount;
    }

    @Override
    public BigDecimal calculateDiscount(Order order) {
        BigDecimal discount = order.getSubtotal()
                .multiply(percentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        return discount.min(maximumDiscount);
    }
}