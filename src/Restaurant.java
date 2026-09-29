import java.util.*;

public class Restaurant {
    private final String id;
    private final String name;
    private final String district;
    private final Set<String> cuisines;
    private final double rating;
    private boolean open;

    private final Map<String, MenuItem> menu = new LinkedHashMap<>();

    public Restaurant(String id, String name, String district,
                      Collection<String> cuisines, double rating, boolean open) {

        if (rating < 0 || rating > 5) {
            throw new ValidationException("Rating must be from 0 to 5");
        }

        this.id = id;
        this.name = name;
        this.district = district;
        this.cuisines = new HashSet<>(cuisines);
        this.rating = rating;
        this.open = open;
    }

    public void addItem(MenuItem item) {
        if (menu.containsKey(item.getId())) {
            throw new ValidationException("Duplicate menu item id");
        }

        menu.put(item.getId(), item);
    }

    public MenuItem getItem(String id) {
        MenuItem item = menu.get(id);

        if (item == null) {
            throw new ValidationException("Menu item not found");
        }

        return item;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        return district;
    }

    public Set<String> getCuisines() {
        return Collections.unmodifiableSet(cuisines);
    }

    public double getRating() {
        return rating;
    }

    public boolean isOpen() {
        return open;
    }

    public Map<String, MenuItem> getMenu() {
        return Collections.unmodifiableMap(menu);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Restaurant
                && id.equals(((Restaurant) object).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}