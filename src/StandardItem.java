import java.math.BigDecimal;
import java.math.RoundingMode;

public class StandardItem extends MenuItem {
    public StandardItem(String id, String name, BigDecimal price,
                        String category, int time,
                        boolean available, BigDecimal stock) {

        super(id, name, price, category, time, available, stock);
    }

    @Override
    public BigDecimal calculatePrice(BigDecimal quantity) {
        return price.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
    }
}