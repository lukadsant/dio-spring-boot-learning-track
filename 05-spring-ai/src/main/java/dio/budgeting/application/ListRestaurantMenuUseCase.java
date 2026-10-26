package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.MenuItem;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListRestaurantMenuUseCase {

    static final List<MenuItem> MENU = List.of(
            // Vitrine Jidouhanbaiki 3D (Slots A1 - B4)
            new MenuItem("A1", "Matcha Latte Okinawa", "JIDOUHANBAIKI_DRINK", "can (Lata Gordinha 350ml)",
                    "Matcha cerimonial japonês com leite cremoso de aveia da vitrine Jidouhanbaiki", 1800, Category.BEVERAGES),
            new MenuItem("A2", "Sakura Sparkling Soda", "JIDOUHANBAIKI_DRINK", "rounded (Garrafa Gordinha 400ml)",
                    "Soda artesanal de flor de cerejeira com toque cítrico de lichia", 2000, Category.BEVERAGES),
            new MenuItem("A3", "Yuzu Citrus Milk", "JIDOUHANBAIKI_DRINK", "milk (Caixinha Gable-Top 500ml)",
                    "Bebida láctea refrescante de yuzu japonês na caixinha Gable-Top", 2200, Category.BEVERAGES),
            new MenuItem("A4", "Hojicha Cold Brew", "JIDOUHANBAIKI_DRINK", "jar (Pote Kawaii c/ Tampa Kraft)",
                    "Chá verde tostado japonês extraído a frio com notas de caramelo", 2400, Category.BEVERAGES),
            new MenuItem("B1", "Ramune Melon Pop", "JIDOUHANBAIKI_DRINK", "rounded (Garrafa Gordinha 400ml)",
                    "Clássico refrigerante japonês de melão Hokkaido", 1900, Category.BEVERAGES),
            new MenuItem("B2", "Ichigo Strawberry Milk", "JIDOUHANBAIKI_DRINK", "milk (Caixinha Gable-Top 500ml)",
                    "Leite de morango japonês estilo konbini de Tóquio", 2100, Category.BEVERAGES),
            // Pratos Clássicos do Izakaya
            new MenuItem("IZ1", "Tonkotsu Ramen Especial", "IZAKAYA_DISH", "Bowl Tradicional",
                    "Caldo rico de 12h com chashu, ajitama, nori e macarrão artesanal", 5400, Category.RAMEN),
            new MenuItem("IZ2", "Gyoza Artesanal (6 unid.)", "IZAKAYA_DISH", "Chapa de Ferro",
                    "Guiozas grelhados na chapa com molho ponzu picante", 3200, Category.BITES),
            new MenuItem("IZ3", "Katsu Sando Wagyu", "IZAKAYA_DISH", "Shokupan Tostado",
                    "Sanduíche japonês no pão de leite com empanado crocante e molho tonkatsu", 4600, Category.BITES),
            new MenuItem("IZ4", "Yakitori Trio Skewers", "IZAKAYA_DISH", "Grelha Binchotan",
                    "Trio de espetinhos grelhados no carvão com glacê tare artesanal", 3600, Category.BITES),
            // Sobremesas do Izakaya
            new MenuItem("IZ5", "Mochi Gelado Artesanal (2 unid.)", "IZAKAYA_DISH", "Prato de Cerâmica",
                    "Mochis recheados de sorvete de matcha e gergelim preto", 2500, Category.DESSERTS)
    );

    @Tool(name = "list-restaurant-menu", description = "Lista o cardápio completo do restaurante Izakaya e as bebidas da vitrine Jidouhanbaiki 3D com códigos, embalagens e preços em reais e centavos")
    public List<MenuItem> execute() {
        return MENU;
    }
}
