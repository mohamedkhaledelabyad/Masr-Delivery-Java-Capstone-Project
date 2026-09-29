import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class BasePromotion implements Promotion {
    protected final String code;
    protected final BigDecimal minimumSubtotal;
    protected final LocalDate expiryDate;
    protected final String allowedDistrict;
    protected final boolean firstOrderOnly;

    public BasePromotion(String code, BigDecimal minimumSubtotal,
                         LocalDate expiryDate, String allowedDistrict,
                         boolean firstOrderOnly) {

        this.code = code.toUpperCase();
        this.minimumSubtotal = minimumSubtotal;
        this.expiryDate = expiryDate;
        this.allowedDistrict = allowedDistrict;
        this.firstOrderOnly = firstOrderOnly;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void validate(Order order) {
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            throw new PromotionException("Promotion expired");
        }

        if (order.getSubtotal().compareTo(minimumSubtotal) < 0) {
            throw new PromotionException("Minimum subtotal not reached");
        }

        if (allowedDistrict != null &&
                !allowedDistrict.equalsIgnoreCase(order.getAddress().getDistrict())) {
            throw new PromotionException("Promotion not valid in this district");
        }

        if (firstOrderOnly && order.getCustomer().getCompletedOrders() > 0) {
            throw new PromotionException("Promotion is for first order only");
        }
    }

    @Override
    public boolean isFreeDelivery() {
        return false;
    }
}
