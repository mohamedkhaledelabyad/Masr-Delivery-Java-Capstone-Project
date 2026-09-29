import java.math.BigDecimal;

public class MenuItemFactory {
    public static MenuItem create(String type, String id, String name,
                                  BigDecimal price, String category,
                                  int time, boolean available, BigDecimal stock) {

        switch (type.toUpperCase()) {
            case "STANDARD":
                return new StandardItem(
                        id, name, price, category, time, available, stock
                );

            case "COMBO":
                return new ComboItem(
                        id, name, price, category, time, available, stock
                );

            case "WEIGHTED":
                return new WeightedItem(
                        id, name, price, category, time, available, stock
                );

            default:
                throw new ValidationException("Unknown item type");
        }
    }
}