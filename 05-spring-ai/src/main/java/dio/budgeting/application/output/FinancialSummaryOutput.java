package dio.budgeting.application.output;

import java.util.Map;

public record FinancialSummaryOutput(
        int totalTransactions,
        double totalAmountReais,
        Map<String, Double> totalsByCategoryReais,
        String topCategory
) {
}
