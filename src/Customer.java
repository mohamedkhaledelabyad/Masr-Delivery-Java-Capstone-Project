import java.math.BigDecimal;
import java.util.*;

public class Customer {
    private final String id;
    private final String name;
    private final String mobile;
    private final List<Address> addresses;
    private BigDecimal walletBalance;
    private int completedOrders;
    private final Deque<String> lastSearches = new ArrayDeque<>();

    public Customer(String id, String name, String mobile,
                    List<Address> addresses, BigDecimal walletBalance) {

        if (!mobile.matches("^(010|011|012|015)\\d{8}$")) {
            throw new ValidationException("Invalid Egyptian mobile number");
        }

        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.addresses = new ArrayList<>(addresses);
        this.walletBalance = walletBalance;
    }

    public LoyaltyTier getTier() {
        if (completedOrders >= 30) return LoyaltyTier.GOLD;
        if (completedOrders >= 10) return LoyaltyTier.SILVER;
        return LoyaltyTier.BRONZE;
    }

    public void pay(BigDecimal amount) {
        if (walletBalance.compareTo(amount) < 0) {
            throw new InsufficientWalletException("Insufficient wallet balance");
        }

        walletBalance = walletBalance.subtract(amount);
    }

    public void refund(BigDecimal amount) {
        walletBalance = walletBalance.add(amount);
    }

    public void addCompletedOrder() {
        completedOrders++;
    }

    public void addSearch(String search) {
        lastSearches.remove(search);
        lastSearches.addFirst(search);

        if (lastSearches.size() > 5) {
            lastSearches.removeLast();
        }
    }

    public String getId() {
        return id;
    }

    public int getCompletedOrders() {
        return completedOrders;
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public List<String> getLastSearches() {
        return new ArrayList<>(lastSearches);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Customer
                && id.equals(((Customer) object).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}