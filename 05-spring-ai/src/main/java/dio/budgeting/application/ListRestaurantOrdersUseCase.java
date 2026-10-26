package dio.budgeting.application;

import dio.budgeting.application.output.RestaurantOrderOutput;
import dio.budgeting.domain.RestaurantOrderRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListRestaurantOrdersUseCase {
    private final RestaurantOrderRepository orderRepository;

    public ListRestaurantOrdersUseCase(RestaurantOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Tool(name = "list-restaurant-orders", description = "Lista todas as comandas e pedidos de mesas do restaurante (abertas e fechadas)")
    public List<RestaurantOrderOutput> execute() {
        return orderRepository.findAll().stream()
                .map(RestaurantOrderOutput::from)
                .toList();
    }
}
