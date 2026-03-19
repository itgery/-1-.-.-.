package domain;
public enum Country {
    RUSSIA,
    UNITED_KINGDOM,
    FRANCE,
    SOUTH_KOREA;

    public static String getAvailableCountries() {
        return "RUSSIA, UNITED_KINGDOM, FRANCE, SOUTH_KOREA";
    }
}