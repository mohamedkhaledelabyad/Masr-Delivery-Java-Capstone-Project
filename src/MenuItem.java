import java.math.BigDecimal;

public abstract class MenuItem {
    protected final String id;
    protected final String name;
    protected final BigDecimal price;
    protected final String category;
    protected final int preparationTime;
    protected boolean available;
    protected BigDecimal stock;

    public MenuItem(String id, String name, BigDecimal price,
                    String category, int preparationTime,
                    boolean available, BigDecimal stock) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.preparationTime = preparationTime;
        this.available = available;
        this.stock = stock;
    }

    public abstract BigDecimal calculatePrice(BigDecimal quantity);

    public void reserveStock(BigDecimal quantity) {
        if (!available) {
            throw new ItemUnavailableException(name + " is unavailable");
        }

        if (stock.compareTo(quantity) < 0) {
            throw new StockShortageException("Not enough stock for " + name);
        }

        stock = stock.subtract(quantity);
    }

    public void restoreStock(BigDecimal quantity) {
        stock = stock.add(quantity);
    }

    public String getId() {
        return id;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}