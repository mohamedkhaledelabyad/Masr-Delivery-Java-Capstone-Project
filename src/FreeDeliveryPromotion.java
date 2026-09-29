import java.math.BigDecimal;
import java.time.LocalDate;

public class FreeDeliveryPromotion extends BasePromotion {
    public FreeDeliveryPromotion(String code, BigDecimal minimumSubtotal,
                                 LocalDate expiryDate, String district,
                                 boolean firstOrderOnly) {

        super(code, minimumSubtotal, expiryDate, district, firstOrderOnly);
    }

    @Override
    public BigDecimal calculateDiscount(Order order) {
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isFreeDelivery() {
        return true;
    }
}
