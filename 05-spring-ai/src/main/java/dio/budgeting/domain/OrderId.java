package dio.budgeting.domain;

import java.util.UUID;

public record OrderId(UUID uuid) {
    public OrderId() {
        this(UUID.randomUUID());
    }
}
