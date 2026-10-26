package dio.budgeting.application;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PersistTransactionUseCase {
    private final TransactionRepository transactionRepository;

    public PersistTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "persist-transaction", description = "Persiste uma nova transação financeira")
    public TransactionOutput persist(
            @ToolParam(description = "Descrição do gasto") String description,
            @ToolParam(description = "Valor do gasto em REAIS (ex: 85 reais = 85, R$ 12,50 = 12.5)") Double amountReais,
            @ToolParam(description = "Categoria de uma transação") Category category) {
        if (amountReais == null) {
            throw new IllegalArgumentException("O valor do gasto em reais é obrigatório.");
        }
        long cents = BigDecimal.valueOf(amountReais).movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
        return execute(new PersistTransactionInput(description, cents, category));
    }

    public TransactionOutput execute(PersistTransactionInput input) {
        var transaction = transactionRepository.save(
                new Transaction(input.description(), input.amount(), input.category()));

        return TransactionOutput.from(transaction);
    }
}
