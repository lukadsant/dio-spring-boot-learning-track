package dio.budgeting.application;

import dio.budgeting.application.output.FinancialSummaryOutput;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummarizeTransactionsUseCase {
    private final TransactionRepository transactionRepository;

    public SummarizeTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "summarize-transactions", description = "Calcula o resumo financeiro consolidado com total geral em reais, quantidade de transações e soma por categoria")
    public FinancialSummaryOutput execute() {
        List<Transaction> transactions = transactionRepository.findAll();

        long totalCents = transactions.stream()
                .mapToLong(Transaction::getAmount)
                .sum();

        Map<String, Long> centsByCategory = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().name(),
                        LinkedHashMap::new,
                        Collectors.summingLong(Transaction::getAmount)
                ));

        Map<String, Double> reaisByCategory = new LinkedHashMap<>();
        centsByCategory.forEach((cat, cents) ->
                reaisByCategory.put(cat, toReais(cents)));

        String topCategory = centsByCategory.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("NONE");

        return new FinancialSummaryOutput(
                transactions.size(),
                toReais(totalCents),
                reaisByCategory,
                topCategory
        );
    }

    private static double toReais(long cents) {
        return BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
