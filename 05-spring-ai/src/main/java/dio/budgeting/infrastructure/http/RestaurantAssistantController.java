package dio.budgeting.infrastructure.http;

import dio.budgeting.application.*;
import dio.budgeting.application.input.CreateRestaurantOrderInput;
import dio.budgeting.application.output.FinancialSummaryOutput;
import dio.budgeting.application.output.RestaurantOrderOutput;
import dio.budgeting.domain.MenuItem;
import dio.budgeting.infrastructure.ai.RestaurantAiAssistantFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurant")
public class RestaurantAssistantController {
    private final RestaurantAiAssistantFacade aiFacade;
    private final ListRestaurantMenuUseCase listMenu;
    private final CreateRestaurantOrderUseCase createOrder;
    private final ListRestaurantOrdersUseCase listOrders;
    private final CloseTableOrderUseCase closeOrder;
    private final SummarizeTransactionsUseCase summarize;
    private final TranscriptionModel transcriptionModel;
    private final TextToSpeechModel textToSpeechModel;

    public RestaurantAssistantController(RestaurantAiAssistantFacade aiFacade, ListRestaurantMenuUseCase listMenu,
                                         CreateRestaurantOrderUseCase createOrder, ListRestaurantOrdersUseCase listOrders,
                                         CloseTableOrderUseCase closeOrder, SummarizeTransactionsUseCase summarize,
                                         TranscriptionModel transcriptionModel, TextToSpeechModel textToSpeechModel) {
        this.aiFacade = aiFacade;
        this.listMenu = listMenu;
        this.createOrder = createOrder;
        this.listOrders = listOrders;
        this.closeOrder = closeOrder;
        this.summarize = summarize;
        this.transcriptionModel = transcriptionModel;
        this.textToSpeechModel = textToSpeechModel;
    }

    public record ChatRequest(@NotBlank String message, String model, Integer tableNumber, String role) {}

    public record ChatResponse(String model, String reply) {}

    public record AudioChatResponse(String model, String transcript, String reply, String audioBase64) {}

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        var model = request.model() != null ? request.model() : aiFacade.defaultModel();
        String effectivePrompt = request.message();
        if ("CUSTOMER".equalsIgnoreCase(request.role()) && request.tableNumber() != null) {
            effectivePrompt = "[Contexto: Pedido do Cliente na Mesa " + request.tableNumber() + "]: " + request.message();
        }
        return new ChatResponse(model, aiFacade.chat(effectivePrompt, request.model()));
    }

    @PostMapping(value = "/chat-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AudioChatResponse chatAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "tableNumber", required = false) Integer tableNumber,
            @RequestParam(value = "role", defaultValue = "STAFF") String role
    ) {
        var transcript = transcriptionModel.transcribe(file.getResource());
        String effectivePrompt = transcript;
        if ("CUSTOMER".equalsIgnoreCase(role) && tableNumber != null) {
            effectivePrompt = "[Contexto: Pedido do Cliente na Mesa " + tableNumber + "]: " + transcript;
        }
        var effectiveModel = model != null ? model : aiFacade.defaultModel();
        var reply = aiFacade.chat(effectivePrompt, effectiveModel);
        byte[] audioBytes = textToSpeechModel.call(reply);
        String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);
        return new AudioChatResponse(effectiveModel, transcript, reply, audioBase64);
    }

    @GetMapping("/ai-status")
    public Map<String, Object> aiStatus() {
        return Map.of("defaultModel", aiFacade.defaultModel(), "models", aiFacade.availableModels());
    }

    @GetMapping("/menu")
    public List<MenuItem> menu() {
        return listMenu.execute();
    }

    @GetMapping("/orders")
    public List<RestaurantOrderOutput> orders() {
        return listOrders.execute();
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantOrderOutput createOrder(@RequestBody CreateRestaurantOrderInput input) {
        return createOrder.execute(input);
    }

    @PostMapping("/orders/{tableNumber}/close")
    public RestaurantOrderOutput close(@PathVariable int tableNumber) {
        return closeOrder.execute(tableNumber);
    }

    @GetMapping("/summary")
    public FinancialSummaryOutput summary() {
        return summarize.execute();
    }
}
