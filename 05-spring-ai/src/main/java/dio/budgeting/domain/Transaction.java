package dio.budgeting.domain;

import lombok.Getter;

@Getter
public class Transaction {
    private TransactionId id;
    private String description;
    private long amount;
    private Category category;

    public Transaction(TransactionId id, String description, long amount, Category category) {
        validate(description, amount, category);
        this.id = id != null ? id : new TransactionId();
        this.description = description.trim();
        this.amount = amount;
        this.category = category;
    }

    public Transaction(String description, long amount, Category category) {
        this(new TransactionId(), description, amount, category);
    }

    private static void validate(String description, long amount, Category category) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("A descrição da transação não pode ser vazia.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("O valor da transação (em centavos) deve ser maior que zero.");
        }
        if (category == null) {
            throw new IllegalArgumentException("A categoria da transação é obrigatória.");
        }
    }
}
