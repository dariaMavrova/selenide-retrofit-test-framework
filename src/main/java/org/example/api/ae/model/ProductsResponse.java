package org.example.api.ae.model;

import java.util.List;

public record ProductsResponse(int responseCode, List<Product> products) {
}
