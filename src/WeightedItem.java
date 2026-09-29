import java.math.BigDecimal;
import java.math.RoundingMode;

public class WeightedItem extends MenuItem {
    public WeightedItem(String id, String name, BigDecimal pricePerKg,
                        String category, int time,
                        boolean available, BigDecimal stockKg) {

        super(id, name, pricePerKg, category, time, available, stockKg);
    }

    @Override
    public BigDecimal calculatePrice(BigDecimal kilograms) {
        return price.multiply(kilograms).setScale(2, RoundingMode.HALF_UP);
    }
}