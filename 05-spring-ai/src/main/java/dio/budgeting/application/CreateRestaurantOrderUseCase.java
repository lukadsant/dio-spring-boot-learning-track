package dio.budgeting.application;

import dio.budgeting.application.input.CreateRestaurantOrderInput;
import dio.budgeting.application.output.RestaurantOrderOutput;
import dio.budgeting.domain.RestaurantOrder;
import dio.budgeting.domain.RestaurantOrderRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CreateRestaurantOrderUseCase {
    private final RestaurantOrderRepository orderRepository;

    public CreateRestaurantOrderUseCase(RestaurantOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Tool(name = "create-restaurant-order", description = "Abre uma comanda ou adiciona itens de pedido (pratos do Izakaya ou bebidas Jidouhanbaiki) em uma mesa do restaurante")
    public RestaurantOrderOutput createOrder(
            @ToolParam(description = "Número da mesa do restaurante (ex: 1, 2, 3...)") Integer tableNumber,
            @ToolParam(description = "Descrição dos pratos ou bebidas pedidos na mesa") String itemsDescription,
            @ToolParam(description = "Valor total em REAIS (opcional: se omitido, o sistema calcula pelo cardápio)", required = false) Double totalAmountReais) {
        if (tableNumber == null) {
            throw new IllegalArgumentException("Informe o número da mesa.");
        }
        long cents;
        if (totalAmountReais != null) {
            cents = BigDecimal.valueOf(totalAmountReais).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
        } else {
            cents = priceFromMenu(itemsDescription);
            if (cents <= 0) {
                throw new IllegalArgumentException(
                        "Não reconheci itens do cardápio na descrição; informe o valor total em reais.");
            }
        }
        return execute(new CreateRestaurantOrderInput(tableNumber, itemsDescription, cents));
    }

    /**
     * Calcula o total em centavos reconhecendo itens do cardápio (nome completo ou as duas primeiras palavras)
     * e a quantidade que antecede o nome (ex.: "2 Tonkotsu Ramen", "2x Matcha Latte"). Sem quantidade, assume 1.
     */
    static long priceFromMenu(String itemsDescription) {
        if (itemsDescription == null) return 0;
        String text = itemsDescription.toLowerCase();
        long total = 0;
        for (var item : ListRestaurantMenuUseCase.MENU) {
            String full = item.name().toLowerCase();
            String[] words = full.split("\\s+");
            String shortName = words.length > 2 ? words[0] + " " + words[1] : full;
            for (String name : new String[]{full, shortName}) {
                var m = java.util.regex.Pattern
                        .compile("(?:(\\d+)\\s*x?\\s+)?" + java.util.regex.Pattern.quote(name)).matcher(text);
                if (m.find()) {
                    total += item.priceCents() * (m.group(1) != null ? Long.parseLong(m.group(1)) : 1);
                    break;
                }
            }
        }
        return total;
    }

    public RestaurantOrderOutput execute(CreateRestaurantOrderInput input) {
        var existingOpenOrder = orderRepository.findOpenOrderByTableNumber(input.tableNumber());

        RestaurantOrder saved;
        if (existingOpenOrder.isPresent()) {
            RestaurantOrder order = existingOpenOrder.get();
            order.appendItems(input.itemsDescription(), input.totalAmountCents());
            saved = orderRepository.save(order);
        } else {
            RestaurantOrder newOrder = new RestaurantOrder(
                    input.tableNumber(),
                    input.itemsDescription(),
                    input.totalAmountCents()
            );
            saved = orderRepository.save(newOrder);
        }

        return RestaurantOrderOutput.from(saved);
    }
}
