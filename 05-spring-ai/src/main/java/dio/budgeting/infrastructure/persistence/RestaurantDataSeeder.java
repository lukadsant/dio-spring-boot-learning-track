package dio.budgeting.infrastructure.persistence;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.RestaurantOrder;
import dio.budgeting.domain.RestaurantOrderRepository;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RestaurantDataSeeder implements CommandLineRunner {
    private final TransactionRepository transactionRepository;
    private final RestaurantOrderRepository orderRepository;

    public RestaurantDataSeeder(TransactionRepository transactionRepository,
                                RestaurantOrderRepository orderRepository) {
        this.transactionRepository = transactionRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        if (transactionRepository.findAll().isEmpty()) {
            transactionRepository.save(new Transaction(
                    "Venda Balcão: 3x Tonkotsu Ramen Especial", 16200, Category.RAMEN));
            transactionRepository.save(new Transaction(
                    "Venda Salão: 2x Shoyu Chashu Ramen", 9800, Category.RAMEN));
            transactionRepository.save(new Transaction(
                    "Venda Salão: 3x Gyoza na Chapa + 2x Katsu Sando Wagyu", 18800, Category.BITES));
            transactionRepository.save(new Transaction(
                    "Vendas Vitrine 3D: 8x Bebidas Artesanais (Matcha, Yuzu, Sakura)", 16500, Category.BEVERAGES));
            transactionRepository.save(new Transaction(
                    "Venda Sobremesas: 4x Mochi Gelado de Matcha", 5000, Category.DESSERTS));
            transactionRepository.save(new Transaction(
                    "Insumos de Cozinha: Mercado Liberdade (Missô, Nori e Chashu)", 14500, Category.SUPPLIES));
        }

        if (orderRepository.findAll().isEmpty()) {
            orderRepository.save(new RestaurantOrder(
                    2, "1x Tonkotsu Ramen Especial + 1x Matcha Latte Okinawa (Slot A1)", 7200));
            orderRepository.save(new RestaurantOrder(
                    5, "2x Katsu Sando Wagyu + 2x Sakura Sparkling Soda (Slot A2)", 13200));
        }
    }
}
