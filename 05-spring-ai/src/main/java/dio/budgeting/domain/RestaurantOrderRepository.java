package dio.budgeting.domain;

import java.util.List;
import java.util.Optional;

public interface RestaurantOrderRepository {
    RestaurantOrder save(RestaurantOrder order);

    List<RestaurantOrder> findAll();

    List<RestaurantOrder> findAllByStatus(OrderStatus status);

    Optional<RestaurantOrder> findOpenOrderByTableNumber(int tableNumber);
}
