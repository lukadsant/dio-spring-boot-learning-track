package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.OrderStatus;
import dio.budgeting.domain.RestaurantOrder;
import dio.budgeting.domain.RestaurantOrderRepository;
import dio.budgeting.infrastructure.persistence.entity.RestaurantOrderEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaRestaurantOrderRepository implements RestaurantOrderRepository {
    private final RestaurantOrderEntityRepository entityRepository;

    public JpaRestaurantOrderRepository(RestaurantOrderEntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }

    @Override
    public RestaurantOrder save(RestaurantOrder order) {
        var entity = RestaurantOrderEntity.from(order);
        return entityRepository.save(entity).toDomain();
    }

    @Override
    public List<RestaurantOrder> findAll() {
        return entityRepository.findAll().stream()
                .map(RestaurantOrderEntity::toDomain)
                .toList();
    }

    @Override
    public List<RestaurantOrder> findAllByStatus(OrderStatus status) {
        return entityRepository.findAllByStatus(status).stream()
                .map(RestaurantOrderEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<RestaurantOrder> findOpenOrderByTableNumber(int tableNumber) {
        return entityRepository.findFirstByTableNumberAndStatus(tableNumber, OrderStatus.OPEN)
                .map(RestaurantOrderEntity::toDomain);
    }
}
