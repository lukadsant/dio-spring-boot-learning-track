package dio.budgeting.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record MenuItem(
        String code,
        String name,
        String itemType,
        String packagingOrStyle,
        String description,
        long priceCents,
        Category financialCategory
) {
    public double priceReais() {
        return BigDecimal.valueOf(priceCents, 2).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
