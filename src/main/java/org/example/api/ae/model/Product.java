package org.example.api.ae.model;

public record Product(int id, String name, String price, String brand, Category category) {

    public record Category(UserType usertype, String category) {
    }

    public record UserType(String usertype) {
    }

    /** "Rs. 500" -> 500 */
    public int priceValue() {
        return Integer.parseInt(price.replaceAll("\\D", ""));
    }
}
