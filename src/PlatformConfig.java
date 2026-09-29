public class PlatformConfig {
    private static final PlatformConfig INSTANCE = new PlatformConfig();

    private PlatformConfig() {
    }

    public static PlatformConfig getInstance() {
        return INSTANCE;
    }

    public int distance(String restaurantDistrict, String customerDistrict) {
        if (restaurantDistrict.equalsIgnoreCase(customerDistrict)) {
            return 0;
        }

        if (restaurantDistrict.equalsIgnoreCase("Maadi")
                && customerDistrict.equalsIgnoreCase("Faisal")) {
            return 12;
        }

        return 5;
    }
}
