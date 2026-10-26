package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.application.output.RestaurantOrderOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.RestaurantOrder;
import dio.budgeting.domain.RestaurantOrderRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class CloseTableOrderUseCase {
    private final RestaurantOrderRepository orderRepository;
    private final PersistTransactionUseCase persistTransactionUseCase;

    public CloseTableOrderUseCase(RestaurantOrderRepository orderRepository,
                                  PersistTransactionUseCase persistTransactionUseCase) {
        this.orderRepository = orderRepository;
        this.persistTransactionUseCase = persistTransactionUseCase;
    }

    @Tool(name = "close-table-order", description = "Fecha a comanda aberta de uma mesa do restaurante e lança automaticamente a transação financeira correspondente no caixa")
    public RestaurantOrderOutput execute(@ToolParam(description = "Número da mesa para fechar a conta") int tableNumber) {
        RestaurantOrder order = orderRepository.findOpenOrderByTableNumber(tableNumber)
                .orElseThrow(() -> new IllegalArgumentException("Não há comanda aberta para a mesa " + tableNumber + "."));

        order.close();
        RestaurantOrder savedOrder = orderRepository.save(order);

        Category category = resolveCategory(order.getItemsDescription());

        persistTransactionUseCase.execute(new PersistTransactionInput(
                "Fechamento Mesa " + tableNumber + " (" + order.getItemsDescription() + ")",
                order.getTotalAmountCents(),
                category
        ));

        return RestaurantOrderOutput.from(savedOrder);
    }

    private Category resolveCategory(String desc) {
        if (desc == null) return Category.RAMEN;
        String lower = desc.toLowerCase();
        if (lower.contains("ramen") || lower.contains("caldo") || lower.contains("tonkotsu")) {
            return Category.RAMEN;
        }
        if (lower.contains("mochi") || lower.contains("sobremesa") || lower.contains("dorayaki")) {
            return Category.DESSERTS;
        }
        if (lower.contains("latte") || lower.contains("soda") || lower.contains("milk") ||
            lower.contains("brew") || lower.contains("pop") || lower.contains("yuzu") || lower.contains("bebida")) {
            return Category.BEVERAGES;
        }
        if (lower.contains("gyoza") || lower.contains("sando") || lower.contains("yakitori") || lower.contains("bites")) {
            return Category.BITES;
        }
        return Category.RAMEN;
    }
}
