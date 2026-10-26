package dio.budgeting.domain;

import lombok.Getter;

import java.time.Instant;

@Getter
public class RestaurantOrder {
    private OrderId id;
    private int tableNumber;
    private String itemsDescription;
    private long totalAmountCents;
    private OrderStatus status;
    private Instant createdAt;

    public RestaurantOrder(OrderId id, int tableNumber, String itemsDescription, long totalAmountCents, OrderStatus status, Instant createdAt) {
        validate(tableNumber, itemsDescription, totalAmountCents, status);
        this.id = id != null ? id : new OrderId();
        this.tableNumber = tableNumber;
        this.itemsDescription = itemsDescription.trim();
        this.totalAmountCents = totalAmountCents;
        this.status = status;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public RestaurantOrder(int tableNumber, String itemsDescription, long totalAmountCents) {
        this(new OrderId(), tableNumber, itemsDescription, totalAmountCents, OrderStatus.OPEN, Instant.now());
    }

    public void appendItems(String additionalItems, long additionalAmountCents) {
        if (this.status == OrderStatus.CLOSED) {
            throw new IllegalStateException("Não é possível adicionar itens a uma comanda já fechada.");
        }
        if (additionalItems == null || additionalItems.isBlank()) {
            throw new IllegalArgumentException("A descrição dos itens adicionais não pode ser vazia.");
        }
        if (additionalAmountCents <= 0) {
            throw new IllegalArgumentException("O valor adicional deve ser maior que zero.");
        }
        this.itemsDescription = this.itemsDescription + " + " + additionalItems.trim();
        this.totalAmountCents += additionalAmountCents;
    }

    public void close() {
        if (this.status == OrderStatus.CLOSED) {
            throw new IllegalStateException("A comanda da mesa " + tableNumber + " já está fechada.");
        }
        this.status = OrderStatus.CLOSED;
    }

    private static void validate(int tableNumber, String itemsDescription, long totalAmountCents, OrderStatus status) {
        if (tableNumber <= 0) {
            throw new IllegalArgumentException("O número da mesa deve ser maior que zero.");
        }
        if (itemsDescription == null || itemsDescription.isBlank()) {
            throw new IllegalArgumentException("Os itens do pedido não podem ser vazios.");
        }
        if (totalAmountCents <= 0) {
            throw new IllegalArgumentException("O valor total do pedido (em centavos) deve ser maior que zero.");
        }
        if (status == null) {
            throw new IllegalArgumentException("O status do pedido é obrigatório.");
        }
    }
}
