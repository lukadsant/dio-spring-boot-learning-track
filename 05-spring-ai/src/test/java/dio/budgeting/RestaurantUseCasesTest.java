package dio.budgeting;

import dio.budgeting.application.CloseTableOrderUseCase;
import dio.budgeting.application.CreateRestaurantOrderUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.application.SummarizeTransactionsUseCase;
import dio.budgeting.application.input.CreateRestaurantOrderInput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestaurantUseCasesTest {

    static class FakeTransactionRepository implements TransactionRepository {
        final List<Transaction> data = new ArrayList<>();

        public Transaction save(Transaction t) { data.add(t); return t; }
        public List<Transaction> findAllByCategory(Category c) {
            return data.stream().filter(t -> t.getCategory() == c).toList();
        }
        public List<Transaction> findAll() { return List.copyOf(data); }
    }

    static class FakeOrderRepository implements RestaurantOrderRepository {
        final List<RestaurantOrder> data = new ArrayList<>();

        public RestaurantOrder save(RestaurantOrder o) { data.removeIf(x -> x.getId().equals(o.getId())); data.add(o); return o; }
        public List<RestaurantOrder> findAll() { return List.copyOf(data); }
        public List<RestaurantOrder> findAllByStatus(OrderStatus s) {
            return data.stream().filter(o -> o.getStatus() == s).toList();
        }
        public Optional<RestaurantOrder> findOpenOrderByTableNumber(int n) {
            return data.stream().filter(o -> o.getTableNumber() == n && o.getStatus() == OrderStatus.OPEN).findFirst();
        }
    }

    @Test
    void transaction_rejectsInvalidData() {
        assertThatThrownBy(() -> new Transaction(" ", 100, Category.AUTO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Transaction("x", 0, Category.AUTO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Transaction("x", 100, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void output_convertsCentsToReais() {
        var output = TransactionOutput.from(new Transaction("Farmácia", 5000, Category.PHARMA));
        assertThat(output.value()).isEqualTo(50.00);
    }

    @Test
    void closeOrder_createsFinancialTransaction() {
        var txRepo = new FakeTransactionRepository();
        var orderRepo = new FakeOrderRepository();
        var create = new CreateRestaurantOrderUseCase(orderRepo);
        var close = new CloseTableOrderUseCase(orderRepo, new PersistTransactionUseCase(txRepo));

        create.execute(new CreateRestaurantOrderInput(3, "2x Ramen", 10800));
        create.execute(new CreateRestaurantOrderInput(3, "1x Matcha Latte", 1800));
        var closed = close.execute(3);

        assertThat(closed.status()).isEqualTo("CLOSED");
        assertThat(closed.totalAmountCents()).isEqualTo(12600);
        assertThat(txRepo.data).hasSize(1);
        assertThat(txRepo.data.get(0).getCategory()).isEqualTo(Category.RAMEN);
        assertThat(txRepo.data.get(0).getAmount()).isEqualTo(12600);
    }

    @Test
    void closeOrder_withoutOpenOrder_fails() {
        var orderRepo = new FakeOrderRepository();
        var close = new CloseTableOrderUseCase(orderRepo, new PersistTransactionUseCase(new FakeTransactionRepository()));
        assertThatThrownBy(() -> close.execute(9)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void summary_groupsByCategory() {
        var txRepo = new FakeTransactionRepository();
        txRepo.save(new Transaction("a", 1000, Category.AUTO));
        txRepo.save(new Transaction("b", 2500, Category.AUTO));
        txRepo.save(new Transaction("c", 500, Category.PHARMA));

        var summary = new SummarizeTransactionsUseCase(txRepo).execute();

        assertThat(summary.totalTransactions()).isEqualTo(3);
        assertThat(summary.totalAmountReais()).isEqualTo(40.00);
        assertThat(summary.totalsByCategoryReais()).containsEntry("AUTO", 35.00);
        assertThat(summary.topCategory()).isEqualTo("AUTO");
    }

    @Test
    void persistTool_convertsReaisToCents() {
        var txRepo = new FakeTransactionRepository();
        var useCase = new PersistTransactionUseCase(txRepo);

        useCase.persist("Insumos", 85.0, Category.INGREDIENTS);
        useCase.persist("Café", 12.5, Category.OTHER);

        assertThat(txRepo.data.get(0).getAmount()).isEqualTo(8500);
        assertThat(txRepo.data.get(1).getAmount()).isEqualTo(1250);
        assertThatThrownBy(() -> useCase.persist("x", null, Category.OTHER)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createOrderTool_requiresTableAndTotal() {
        var useCase = new CreateRestaurantOrderUseCase(new FakeOrderRepository());
        assertThatThrownBy(() -> useCase.createOrder(4, "algo desconhecido", null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(useCase.createOrder(4, "1x Ramen", 54.0).totalAmountCents()).isEqualTo(5400);
    }

    @Test
    void createOrderTool_computesTotalFromMenuWhenMissing() {
        var useCase = new CreateRestaurantOrderUseCase(new FakeOrderRepository());
        // Katsu Sando Wagyu (46,00) + Ramune Melon Pop (19,00)
        assertThat(useCase.createOrder(4, "1 Katsu Sando Wagyu e 1 Ramune Melon Pop", null).totalAmountCents()).isEqualTo(6500);
        // 2 Tonkotsu Ramen (2x54,00) + 2 Matcha Latte (2x18,00)
        assertThat(useCase.createOrder(5, "2 Tonkotsu Ramen e 2 Matcha Latte", null).totalAmountCents()).isEqualTo(14400);
    }
}
