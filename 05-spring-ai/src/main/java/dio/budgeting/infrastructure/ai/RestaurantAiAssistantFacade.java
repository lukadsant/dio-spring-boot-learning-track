package dio.budgeting.infrastructure.ai;

import dio.budgeting.application.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Facade que orquestra o ChatClient (Ollama local via API compatível com OpenAI)
 * com todas as ferramentas @Tool dos casos de uso.
 */
@Component
public class RestaurantAiAssistantFacade {
    private final ChatClient chatClient;
    private final List<String> availableModels;
    private final String defaultModel;

    public RestaurantAiAssistantFacade(ChatClient.Builder chatClientBuilder,
                                       @Value("classpath:prompts/system-message.st") Resource systemPrompt,
                                       @Value("${app.ai.models}") String models,
                                       @Value("${spring.ai.openai.chat.options.model}") String defaultModel,
                                       PersistTransactionUseCase persistTransactionUseCase,
                                       ListTransactionsByCategoryUseCase listByCategoryUseCase,
                                       ListAllTransactionsUseCase listAllTransactionsUseCase,
                                       SummarizeTransactionsUseCase summarizeTransactionsUseCase,
                                       ListRestaurantMenuUseCase listRestaurantMenuUseCase,
                                       CreateRestaurantOrderUseCase createRestaurantOrderUseCase,
                                       ListRestaurantOrdersUseCase listRestaurantOrdersUseCase,
                                       CloseTableOrderUseCase closeTableOrderUseCase) throws IOException {
        this.availableModels = Arrays.stream(models.split(",")).map(String::trim).toList();
        this.defaultModel = defaultModel;
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt.getContentAsString(StandardCharsets.UTF_8))
                .defaultTools(persistTransactionUseCase, listByCategoryUseCase, listAllTransactionsUseCase,
                        summarizeTransactionsUseCase, listRestaurantMenuUseCase, createRestaurantOrderUseCase,
                        listRestaurantOrdersUseCase, closeTableOrderUseCase)
                .build();
    }

    public String chat(String message, String model) {
        String chosen = (model != null && availableModels.contains(model)) ? model : defaultModel;
        return chatClient.prompt()
                .user(message)
                .options(OpenAiChatOptions.builder().model(chosen).temperature(0.1).build())
                .call()
                .content();
    }

    public List<String> availableModels() {
        return availableModels;
    }

    public String defaultModel() {
        return defaultModel;
    }
}
