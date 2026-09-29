public class Address {
    private final String district;
    private final String details;

    public Address(String district, String details) {
        this.district = district;
        this.details = details;
    }

    public String getDistrict() {
        return district;
    }

    @Override
    public String toString() {
        return district + " - " + details;
    }
}