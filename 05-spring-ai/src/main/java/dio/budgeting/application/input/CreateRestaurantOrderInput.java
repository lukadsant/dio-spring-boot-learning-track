package dio.budgeting.application.input;

import org.springframework.ai.tool.annotation.ToolParam;

public record CreateRestaurantOrderInput(
        @ToolParam(description = "Número da mesa do restaurante (ex: 1, 2, 3, 4...)") int tableNumber,
        @ToolParam(description = "Descrição dos pratos ou bebidas Jidouhanbaiki pedidos na mesa") String itemsDescription,
        @ToolParam(description = "Valor total do pedido em centavos (ex: R$ 45,00 = 4500 centavos)") long totalAmountCents
) {
}
