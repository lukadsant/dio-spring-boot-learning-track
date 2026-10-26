package dio.budgeting.application.output;

import dio.budgeting.domain.RestaurantOrder;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record RestaurantOrderOutput(
        String id,
        int tableNumber,
        String itemsDescription,
        long totalAmountCents,
        double totalAmountReais,
        String status,
        String createdAt
) {
    public static RestaurantOrderOutput from(RestaurantOrder order) {
        return new RestaurantOrderOutput(
                order.getId().uuid().toString(),
                order.getTableNumber(),
                order.getItemsDescription(),
                order.getTotalAmountCents(),
                BigDecimal.valueOf(order.getTotalAmountCents(), 2).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                order.getStatus().name(),
                order.getCreatedAt().toString()
        );
    }
}
