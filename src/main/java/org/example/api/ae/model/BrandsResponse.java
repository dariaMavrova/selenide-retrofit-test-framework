package org.example.api.ae.model;

import java.util.List;

public record BrandsResponse(int responseCode, List<Brand> brands) {
}
