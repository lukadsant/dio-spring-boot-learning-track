package dio.budgeting.infrastructure.persistence.entity;

import dio.budgeting.domain.OrderId;
import dio.budgeting.domain.OrderStatus;
import dio.budgeting.domain.RestaurantOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantOrderEntity {
    @Id
    private UUID id;
    private int tableNumber;
    private String itemsDescription;
    private long totalAmountCents;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private Instant createdAt;

    public static RestaurantOrderEntity from(RestaurantOrder order) {
        return new RestaurantOrderEntity(
                order.getId().uuid(),
                order.getTableNumber(),
                order.getItemsDescription(),
                order.getTotalAmountCents(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    public RestaurantOrder toDomain() {
        return new RestaurantOrder(
                new OrderId(this.id),
                this.tableNumber,
                this.itemsDescription,
                this.totalAmountCents,
                this.status,
                this.createdAt
        );
    }
}
