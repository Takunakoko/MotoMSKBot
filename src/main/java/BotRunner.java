import com.vk.api.sdk.objects.wall.WallPost;
import com.vk.api.sdk.objects.wall.WallPostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class BotRunner implements Runnable {
    private boolean isFirstStart = true;
    private ArrayList<Integer> alredyExistNumbers = new ArrayList<Integer>();
    private static GetResponse responseFromWall;
    private ArrayList<Integer> idNewPosts = new ArrayList<Integer>();
    private MotoMSKBot mskBot;

    BotRunner(MotoMSKBot motoMSKBot) {
        this.mskBot = motoMSKBot;
    }

    @Override
    public void run() {
        VkApi vkApi = new VkApi();
        vkApi.initApi();
        while (true) {
            try {
                responseFromWall = vkApi.getResponseFromWall();
                if (isFirstStart) {
                    firstStart(responseFromWall.getItems());
                }else {
                    checkPostAlredyExist(responseFromWall);
                    if (idNewPosts.size() != 0) {
                        for (Integer id : idNewPosts) {
                            WallPostFull wallPostFull = getPostByID(id);
                            if (checkRepost(wallPostFull)) {
                                sendRepostMessage(wallPostFull);
                            } else {
                                sendMessageInCase(wallPostFull);
                                Thread.currentThread().sleep(1000);
                            }
                        }
                        idNewPosts.clear();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.currentThread().sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean checkRepost(WallPostFull wallPostFull) {
        if (wallPostFull.getCopyHistory().size() != 0) return true;
        else return false;
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
                sendMessage = new SendMessage()
                        .setChatId("@motomskdtp")
                        .setText(wallPostFull.getText());
                mskBot.sendMsg(sendMessage);
                break;
            case "TL":
                if (num1 == 0) {
                    tmpURL = wallPostFull.getAttachments().get(0).getLink().getUrl();
                } else if (num3 != -1) {
                    tmpURL = wallPostFull.getAttachments().get(num3).getLink().getUrl();
                }
                sendMessage = new SendMessage()
                        .setChatId("@motomskdtp")
                        .setText(checkTextHttp(wallPostFull.getText()));
                inlineKeyboardButton = new InlineKeyboardButton()
                        .setText("Перейти по ссылке")
                        .setUrl(tmpURL);
                row.add(inlineKeyboardButton);
                rows.add(row);
                inlineKeyboardMarkup.setKeyboard(rows);
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                mskBot.sendMsg(sendMessage);
                break;
            case "TP":
                if (num1 == 0) {
                    photoUrl = wallPostFull.getAttachments().get(0).getPhoto().getPhoto807();
                } else if (num2 != -1) {
                    photoUrl = wallPostFull.getAttachments().get(0).getPhoto().getPhoto807();
                }
                sendPhotoRequest = new SendPhoto()
                        .setChatId("@motomskdtp")
                        .setPhoto(photoUrl)
                        .setCaption(checkTextLength(wallPostFull.getText()));
                mskBot.sendMsg(sendPhotoRequest);
                break;
            case "TLP":
                photoUrl = wallPostFull.getAttachments().get(num2).getPhoto().getPhoto807();
                textMessage = checkTextHttp(wallPostFull.getText());
                textMessage = checkTextLength(textMessage);
                sendPhotoRequest = new SendPhoto()
                        .setChatId("@motomskdtp")
                        .setPhoto(photoUrl)
                        .setCaption(textMessage);
                inlineKeyboardButton = new InlineKeyboardButton()
                        .setText("Перейти по ссылке")
                        .setUrl(wallPostFull.getAttachments().get(num3).getLink().getUrl());
                row.add(inlineKeyboardButton);
                rows.add(row);
                inlineKeyboardMarkup.setKeyboard(rows);
                sendPhotoRequest.setReplyMarkup(inlineKeyboardMarkup);
                mskBot.sendMsg(sendPhotoRequest);
                break;
            case "TPL":
                photoUrl = wallPostFull.getAttachments().get(num2).getPhoto().getPhoto807();
                textMessage = checkTextHttp(wallPostFull.getText());
                textMessage = checkTextLength(textMessage);
                sendPhotoRequest = new SendPhoto()
                        .setChatId("@motomskdtp")
                        .setPhoto(photoUrl)
                        .setCaption(textMessage);
                inlineKeyboardButton = new InlineKeyboardButton()
                        .setText("Перейти по ссылке")
                        .setUrl(wallPostFull.getAttachments().get(num3).getLink().getUrl());
                row.add(inlineKeyboardButton);
                rows.add(row);
                inlineKeyboardMarkup.setKeyboard(rows);
                sendPhotoRequest.setReplyMarkup(inlineKeyboardMarkup);
                mskBot.sendMsg(sendPhotoRequest);
                break;
        }
    }

    private void sendRepostMessage(WallPostFull wallPostFull) {
        SendMessage sendMessage;
        List<WallPost> copyHistory = wallPostFull.getCopyHistory();
        if (copyHistory.size() == 1) {
            if (!wallPostFull.getText().equals("")) {
                sendMessage = new SendMessage()
                        .setChatId("@motomskdtp")
                        .setText(wallPostFull.getText() + "\n" + "----------------" + "\n" + copyHistory.get(0).getText());
            } else {
                sendMessage = new SendMessage()
                        .setChatId("@motomskdtp")
                        .setText(copyHistory.get(0).getText());
            }
            mskBot.sendMsg(sendMessage);
        } else if (copyHistory.size() == 2) {
            if (!wallPostFull.getText().equals("")) {
                sendMessage = new SendMessage()
                        .setChatId("@motomskdtp")
                        .setText(wallPostFull.getText() + "\n" + "----------------" +"\n" + copyHistory.get(0).getText() +
                                "\n" + "----------------" + "\n" + copyHistory.get(1).getText());
            } else {
                sendMessage = new SendMessage()
                        .setChatId("@motomskdtp")
                        .setText(copyHistory.get(0).getText());
            }
            mskBot.sendMsg(sendMessage);
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
        System.out.println(alredyExistNumbers);
        boolean label;
        for (WallPostFull updateId : items) {
            label = true;
            for (Integer oldId : alredyExistNumbers) {
                if (oldId == updateId.getId().intValue()) {
                    label = false;
                    break;
                }
            }
            if (label) {
                idNewPosts.add(updateId.getId());
            }
        }
        alredyExistNumbers.clear();
        for (WallPostFull item : items) {
            alredyExistNumbers.add(item.getId());
        }
        System.out.println(idNewPosts);
    }

    private void firstStart(List<WallPostFull> wallPostFull) {
        for (WallPostFull wp : wallPostFull) {
            alredyExistNumbers.add(wp.getId());
        }
        isFirstStart = false;
    }
}
