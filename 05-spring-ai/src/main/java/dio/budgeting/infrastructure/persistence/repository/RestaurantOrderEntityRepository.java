package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.OrderStatus;
import dio.budgeting.infrastructure.persistence.entity.RestaurantOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantOrderEntityRepository extends JpaRepository<RestaurantOrderEntity, UUID> {
    List<RestaurantOrderEntity> findAllByStatus(OrderStatus status);

    Optional<RestaurantOrderEntity> findFirstByTableNumberAndStatus(int tableNumber, OrderStatus status);
}
