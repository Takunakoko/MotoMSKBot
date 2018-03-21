import com.vk.api.sdk.objects.wall.WallPost;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


public class MotoMSKBot extends TelegramLongPollingBot {
    private boolean isFirstStart = true;
    private ArrayList<Integer> alredyExistNumbers = new ArrayList<Integer>();
    private static GetResponse responseFromWall;
    private static VkApi vkApi;
    private ArrayList<Integer> idNewPosts = new ArrayList<Integer>();

    static void initMotoVk() throws Exception {
        vkApi = new VkApi();
        vkApi.initApi();
        System.out.println("Vk api init");
    }

    public void onUpdateReceived(Update update) {
        if ((update.getMessage().getFrom().getUserName().equals("kuraev_alex"))
                || (update.getMessage().getFrom().getUserName().equals("Recedivist"))) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                if (update.getMessage().getText().equalsIgnoreCase("/botstart")) {
                    try {
                        execute(new SendMessage().setChatId(update.getMessage().getChatId()).setText("Бот запущен"));
                        while (true) {
                            try {
                                responseFromWall = vkApi.getResponseFromWall();
                                if (isFirstStart) {
                                    firstStart(responseFromWall.getItems());
                                }
                                checkPostAlredyExist(responseFromWall);
                                if (idNewPosts.size() != 0) {
                                    for (Integer id : idNewPosts) {
                                        WallPostFull wallPostFull = getPostByID(id);
                                        if (checkRepost(wallPostFull)) {
                                            sendRepostMessage(wallPostFull);
                                        } else {
                                            sendMessageInCase(wallPostFull);
                                        }

                                        Thread.sleep(2000);
                                    }
                                    idNewPosts.clear();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                Thread.sleep(60000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            try {
                execute(new SendMessage().setChatId(update.getMessage().getChatId()).setText("У вас нет прав доступа"));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    public String getBotUsername() {
        return Constants.BOT_USER_NAME;
    }

    public String getBotToken() {
        return Constants.BOT_TOKEN;
    }

    private boolean checkRepost(WallPostFull wallPostFull) {
        if (wallPostFull.getCopyHistory().size() != 0) {
            return true;
        } else return false;
    }

    private void sendMessageInCase(WallPostFull wallPostFull) {
        boolean havePhoto = false;
        StringBuilder sb = new StringBuilder();
        int num1 = -1;
        int num2 = -1;
        int num3 = -1;
        if (wallPostFull.getText() != null) sb.append("T");
        if (wallPostFull.getAttachments() != null) {
            if (wallPostFull.getAttachments().size() == 1) {
                if (wallPostFull.getAttachments().get(0).getLink() != null) {
                    sb.append("L");
                    num1 = 0;
                    System.out.println("В тексте есть линк (0)");
                }
                if (wallPostFull.getAttachments().get(0).getPhoto() != null) {
                    sb.append("P");
                    num1 = 0;
                    System.out.println("В тексте есть фото (0)");
                }
            } else {
                for (int i = 0; i < wallPostFull.getAttachments().size(); i++) {
                    while (!havePhoto) {
                        if (wallPostFull.getAttachments().get(i).getPhoto() != null) {
                            sb.append("P");
                            num2 = i;
                            havePhoto = true;
                            System.out.println("В тексте есть фото " + num2);
                        }
                    }
                    if (wallPostFull.getAttachments().get(i).getLink() != null) {
                        sb.append("L");
                        num3 = i;
                        System.out.println("В тексте есть линк " + num3);
                    }
                }
            }
        }
        send(wallPostFull, sb.toString(), num1, num2, num3);
    }

    private void send(WallPostFull wallPostFull, String desc, int num1, int num2, int num3) {
        SendMessage sendMessage;
        String photoUrl = null;
        String tmpURL = null;
        String textMessage;
        SendPhoto sendPhotoRequest;
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineKeyboardButton;
        System.out.println(desc);
        switch (desc) {
            case "T":
                System.out.println("Вход в блок Т");
                sendMessage = new SendMessage()
                        .setChatId("@testMotoMSK")
                        .setText(wallPostFull.getText());
                try {
                    execute(sendMessage);
                } catch (TelegramApiException ex) {
                    ex.printStackTrace();
                }
                break;
            case "TL":
                System.out.println("Вход в блок ТЛ");
                if (num1 == 0) {
                    tmpURL = wallPostFull.getAttachments().get(0).getLink().getUrl();
                } else if (num3 != -1) {
                    tmpURL = wallPostFull.getAttachments().get(num3).getLink().getUrl();
                }
                sendMessage = new SendMessage()
                        .setChatId("@testMotoMSK")
                        .setText(checkTextHttp(wallPostFull.getText()));
                inlineKeyboardButton = new InlineKeyboardButton()
                        .setText("Перейти по ссылке")
                        .setUrl(tmpURL);
                row.add(inlineKeyboardButton);
                rows.add(row);
                inlineKeyboardMarkup.setKeyboard(rows);
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException ex) {
                    ex.printStackTrace();
                }
                break;
            case "TP":
                System.out.println("Вход в блок ТР");
                if (num1 == 0) {
                    photoUrl = wallPostFull.getAttachments().get(0).getPhoto().getPhoto807();
                } else if (num2 != -1) {
                    photoUrl = wallPostFull.getAttachments().get(0).getPhoto().getPhoto807();
                }
                sendPhotoRequest = new SendPhoto()
                        .setChatId("@testMotoMSK")
                        .setPhoto(photoUrl)
                        .setCaption(checkTextLength(wallPostFull.getText()));
                try {
                    sendPhoto(sendPhotoRequest);
                } catch (TelegramApiException ex) {
                    ex.printStackTrace();
                }
                break;
            case "TLP":
                System.out.println("Вход в блок ТLP");
                photoUrl = wallPostFull.getAttachments().get(num2).getPhoto().getPhoto807();
                textMessage = checkTextHttp(wallPostFull.getText());
                textMessage = checkTextLength(textMessage);
                sendPhotoRequest = new SendPhoto()
                        .setChatId("@testMotoMSK")
                        .setPhoto(photoUrl)
                        .setCaption(textMessage);
                inlineKeyboardButton = new InlineKeyboardButton()
                        .setText("Перейти по ссылке")
                        .setUrl(wallPostFull.getAttachments().get(num3).getLink().getUrl());
                row.add(inlineKeyboardButton);
                rows.add(row);
                inlineKeyboardMarkup.setKeyboard(rows);
                sendPhotoRequest.setReplyMarkup(inlineKeyboardMarkup);
                try {
                    sendPhoto(sendPhotoRequest);
                } catch (TelegramApiException ex) {
                    ex.printStackTrace();
                }
                break;
            case "TPL":
                System.out.println("Вход в блок ТLP");
                photoUrl = wallPostFull.getAttachments().get(num2).getPhoto().getPhoto807();
                textMessage = checkTextHttp(wallPostFull.getText());
                textMessage = checkTextLength(textMessage);
                sendPhotoRequest = new SendPhoto()
                        .setChatId("@testMotoMSK")
                        .setPhoto(photoUrl)
                        .setCaption(textMessage);
                inlineKeyboardButton = new InlineKeyboardButton()
                        .setText("Перейти по ссылке")
                        .setUrl(wallPostFull.getAttachments().get(num3).getLink().getUrl());
                row.add(inlineKeyboardButton);
                rows.add(row);
                inlineKeyboardMarkup.setKeyboard(rows);
                sendPhotoRequest.setReplyMarkup(inlineKeyboardMarkup);
                try {
                    sendPhoto(sendPhotoRequest);
                } catch (TelegramApiException ex) {
                    ex.printStackTrace();
                }
                break;
        }
    }

    private void sendRepostMessage(WallPostFull wallPostFull) {
        SendMessage sendMessage;
        List<WallPost> copyHistory = wallPostFull.getCopyHistory();
        System.out.println("Вход в блок repost");
        if (copyHistory.size() != 0) {
            if (!wallPostFull.getText().equals("")) {
                sendMessage = new SendMessage()
                        .setChatId("@testMotoMSK")
                        .setText(wallPostFull.getText() + "\n" + "\n" + copyHistory.get(0).getText());
            } else {
                sendMessage = new SendMessage()
                        .setChatId("@testMotoMSK")
                        .setText(copyHistory.get(0).getText());
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        }
    }

    private String checkTextLength(String textMessage) {
        String returnString = textMessage;
        if (textMessage.length() > 200) {
            returnString = textMessage.substring(0, 196) + "...";
        }
        return returnString;
    }

    private String checkTextHttp(String textMessage) {
        String returnString = textMessage;
        if (textMessage.contains("http")) {
            String[] parts = textMessage.split("http");
            returnString = parts[0];
        }
        return returnString;
    }

    private WallPostFull getPostByID(int id) {
        for (WallPostFull wallPostFull : responseFromWall.getItems()) {
            if (wallPostFull.getId() == id) {
                return wallPostFull;
            }
        }
        return null;
    }

    private void checkPostAlredyExist(GetResponse responseFromWall) {
        List<WallPostFull> items = responseFromWall.getItems();
        boolean label;
        for (WallPostFull updateId : items) {
            label = true;
            for (Integer oldId : alredyExistNumbers) {
                if (oldId == updateId.getId()) {
                    label = false;
                    break;
                }
            }
            if (label) {
                idNewPosts.add(updateId.getId());
            }
        }
        System.out.println("IdNewPostNum :" + idNewPosts);
        alredyExistNumbers.clear();
        for (WallPostFull item : items) {
            alredyExistNumbers.add(item.getId());
        }
        System.out.println("alredyExistNum: " + alredyExistNumbers);
    }

    private void firstStart(List<WallPostFull> wallPostFull) {
        for (WallPostFull wp : wallPostFull) {
            alredyExistNumbers.add(wp.getId());
        }
        isFirstStart = false;
    }

}
