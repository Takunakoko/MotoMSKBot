import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MotoMSKBot extends TelegramLongPollingBot {

    private VkApi vkApi = new VkApi();
    boolean isStarted = false;
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public void onUpdateReceived(Update update) {
        try {
            if ((update.getMessage().getFrom().getUserName().equals("kuraev_alex"))
                    || (update.getMessage().getFrom().getUserName().equals("Recedivist"))) {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    if (update.getMessage().getText().equalsIgnoreCase("/botstart")) {
                        if (!isStarted) {
                            isStarted = true;
                            execute(new SendMessage().setChatId(update.getMessage().getChatId()).setText("Бот запущен"));
                            executor.scheduleWithFixedDelay(new BotRunner(this, vkApi), 0, 20, TimeUnit.SECONDS);
                        } else {
                            execute(new SendMessage().setChatId(update.getMessage().getChatId()).setText("Бот уже запущен!"));
                        }
                    }
                    if (update.getMessage().getText().equalsIgnoreCase("/isBotAlive")) {
                        execute(new SendMessage().setChatId(update.getMessage().getChatId()).setText("Я ЖИВОЙ!"));
                    }
                    if (update.getMessage().getText().equalsIgnoreCase("/botstop")) {
                        execute(new SendMessage().setChatId(update.getMessage().getChatId()).setText("Экстренная остановка бота"));
                        isStarted = false;
                        System.exit(1);
                    }
                }
            } else {
                execute(new SendMessage().setChatId(update.getMessage().getChatId()).setText("У вас нет прав доступа"));
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }


    public String getBotUsername() {
        return Constants.BOT_USER_NAME;
    }

    public String getBotToken() {
        return Constants.BOT_TOKEN;
    }

    void sendMsg(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    void sendMsg(SendPhoto sendPhoto) {
        try {
            sendPhoto(sendPhoto);
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

}
