import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;


public class Main {

    public static void main(String[] args) throws Exception {
        //init api context
        ApiContextInitializer.init();
        //init bots api
        TelegramBotsApi botsApi = new TelegramBotsApi();
        //register our bot
        try {
            botsApi.registerBot(new MotoMSKBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        MotoMSKBot.initMotoVk();


    }
}
