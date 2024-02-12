package com.softserve.dis.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserve.dis.model.Issue;
import com.softserve.dis.model.TelegramUser;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import org.springframework.web.client.RestTemplate;
@Setter
@Getter
@AllArgsConstructor
class UserData {
    private boolean LoginStatus;
    private String role;
    private String inputData;
    private Issue issue;

    UserData() {
        this.LoginStatus = false;
        this.role = "none";
        this.inputData = null;
        this.issue = new Issue();
    }
}

public class TelegramBot extends TelegramLongPollingBot {
    private Map<Long, UserData> userStatus = new HashMap<>();
    private Map<Long, Issue> issues = new HashMap<>();

    private final String HOST = "http://localhost:8080/";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public TelegramBot() {
        for (TelegramUser user: restTemplate.getForEntity(HOST + "telegramUsers", TelegramUser[].class).getBody()) {
            userStatus.put(user.getId(), new UserData());
            userStatus.get(user.getId()).setLoginStatus(user.isActivated());
            userStatus.get(user.getId()).setRole(user.getRole());
        }
        setAllIssues();
    }


    @Override
    public String getBotUsername() {
        return "BOT NAME";
    }

    public String getBotToken() {return "BOT TOKEN";}

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = getChatId(update);
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            SendMessage message;

            switch (messageText) {
                case "/start":
                    if (!userStatus.containsKey(chatId)) userStatus.put(chatId, new UserData());
                    message = createMessage("Вітаю в системі простежування дефектів в готелях!");
                    message.setChatId(chatId);
                    sendApiMethodAsync(message);
                    LoginMessage(chatId);
                    break;

                case "Вхід":
                    if (!userStatus.containsKey(chatId)) userStatus.put(chatId, new UserData());
                    ResponseEntity<TelegramUser> responseEntity = restTemplate.getForEntity(HOST + "telegramUsers/" + chatId, TelegramUser.class);
                    TelegramUser user = responseEntity.getBody();
                    if (userStatus.getOrDefault(chatId, new UserData()).isLoginStatus()) {
                        userStatus.get(chatId).setRole(user.getRole());
                        chooseOptionMessage(chatId);
                    } else {
                        if (user != null) {
                            message = createMessage("Запит на реєстрацію ще не одобрений");
                        } else {
                            message = createMessage("Ви ще не зареєстровані");
                        }
                        message.setChatId(chatId);
                        sendApiMethodAsync(message);
                    }
                    break;
                case "Реєстрація":
                    if (!userStatus.containsKey(chatId)) userStatus.put(chatId, new UserData());
                    if (restTemplate.getForEntity(HOST + "telegramUsers/" + chatId, TelegramUser.class).getBody() != null) {
                        message = createMessage("Запит на реєстрацію вже відправлений");
                    } else {
                        try {
                            TelegramUser newUser = new TelegramUser();
                            newUser.setId(chatId);
                            newUser.setFirstName(update.getMessage().getFrom().getFirstName());
                            newUser.setLastName(update.getMessage().getFrom().getLastName());
                            String jsonPayload = objectMapper.writeValueAsString(newUser);
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);

                            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);
                            restTemplate.postForEntity(HOST + "telegramUsers", requestEntity, String.class);
                            message = createMessage("Запит успішно вислано, очікуйте підтвердження");
                        } catch (Exception e) {
                            System.out.println("Error: " + e);
                            message = createMessage("Виникла неочікувана помилка, спробуйте ще раз або зв'яжіться з власником.");
                        }
                    }
                    message.setChatId(chatId);
                    sendApiMethodAsync(message);
                    break;
                case "Додати дефект":
                    if (userStatus.getOrDefault(chatId, new UserData()).getRole().equals("technical")) {
                        message = createMessage("Введіть номер кімнати");
                        KeyboardRow row = new KeyboardRow();
                        row.add("Скасувати");
                        message.setReplyMarkup(new ReplyKeyboardMarkup(List.of(new KeyboardRow(row))));
                        message.setChatId(chatId);
                        sendApiMethodAsync(message);
                        userStatus.get(chatId).setInputData("roomNumber");
                        userStatus.get(chatId).setIssue(new Issue());
                        ResponseEntity<TelegramUser> responseEntityAddDef = restTemplate.getForEntity(HOST + "telegramUsers/" + chatId, TelegramUser.class);
                        userStatus.get(chatId).getIssue().setTelegramUser(responseEntityAddDef.getBody());
                    }
                        else {
                        message = createMessage("Вам недоступна ця функція");
                        message.setChatId(chatId);
                        sendApiMethodAsync(message);
                    }
                    break;
                case "Переглянути дефекти":
                    setAllIssues();
                    Map<String, String> buttons = new HashMap<>();

                    if(userStatus.getOrDefault(chatId, new UserData()).getRole().equals("repair")) {
                        for (Long i: issues.keySet()) {
                            buttons.put(issues.get(i).getRoom() + ": " + issues.get(i).getDescription(), "sendIssue/" + i);
                        }

                    } else if(userStatus.getOrDefault(chatId, new UserData()).getRole().equals("technical")) {
                        for (Long i: issues.keySet()) {
                            if(issues.get(i).getTelegramUser().getId().equals(chatId)) {
                                buttons.put(issues.get(i).getRoom() + ": " + issues.get(i).getDescription(), "sendIssue/" + i);
                            }
                        }

                    } else {
                        message = createMessage("Вам недоступна ця функція");
                        message.setChatId(chatId);
                        sendApiMethodAsync(message);
                        break;
                    }

                    if(buttons.isEmpty()) message = createMessage("Список дефектів пустий");
                    else {
                        message = createMessage("Оберіть дефект");
                        attachButtons(message, buttons);
                    }
                    message.setChatId(chatId);
                    sendApiMethodAsync(message);
                    break;
                default:
                    String input = userStatus.getOrDefault(chatId, new UserData()).getInputData();
                    if (input != null) {
                        switch (input) {
                            case "roomNumber":
                                if (messageText.equals("Скасувати")) {
                                    userStatus.get(chatId).setInputData(null);
                                    chooseOptionMessage(chatId);
                                } else {
                                    message = createMessage("Введіть короткий опис");
                                    message.setChatId(chatId);
                                    sendApiMethodAsync(message);
                                    userStatus.get(chatId).setInputData("shortDesc");
                                    userStatus.get(chatId).getIssue().setRoom(messageText);
                                }
                                break;
                            case "shortDesc":
                                if (messageText.equals("Скасувати")) {
                                    userStatus.get(chatId).setInputData(null);
                                    chooseOptionMessage(chatId);
                                } else {
                                    message = createMessage("Відправте фото (опціонально)");
                                    message.setChatId(chatId);
                                    KeyboardRow row = new KeyboardRow();
                                    row.add("Скасувати");
                                    row.add("Пропустити");
                                    message.setReplyMarkup(new ReplyKeyboardMarkup(List.of(new KeyboardRow(row))));
                                    sendApiMethodAsync(message);
                                    userStatus.get(chatId).setInputData("photo");
                                    userStatus.get(chatId).getIssue().setDescription(messageText);
                                }
                                break;
                            case "photo":
                                if (messageText.equals("Скасувати")) {
                                    chooseOptionMessage(chatId);
                                }
                                if (messageText.equals("Пропустити")) {
                                    userStatus.get(chatId).getIssue().setPhoto(null);
                                    addNewDefect(chatId, userStatus.get(chatId).getIssue());
                                }
                                userStatus.get(chatId).setInputData(null);
                                break;
                            case "changeRoom":
                                if (messageText.equals("Скасувати")) {
                                    chooseOptionMessage(chatId);
                                } else {
                                    userStatus.get(chatId).getIssue().setRoom(messageText);
                                    SendIssue(chatId, userStatus.get(chatId).getIssue(), null);
                                    message = createMessage("Зберегти зміни?");
                                    attachButtons(message, Map.of("Так", "saveIssue", "Ні", "cancelChanges"));
                                    message.setChatId(chatId);
                                    sendApiMethodAsync(message);
                                }
                                userStatus.get(chatId).setInputData(null);
                                break;
                            case "changeDesc":
                                if (messageText.equals("Скасувати")) {
                                    chooseOptionMessage(chatId);
                                } else {
                                    userStatus.get(chatId).getIssue().setDescription(messageText);
                                    SendIssue(chatId, userStatus.get(chatId).getIssue(), null);
                                    message = createMessage("Зберегти зміни?");
                                    attachButtons(message, Map.of("Так", "saveIssue", "Ні", "cancelChanges"));
                                    message.setChatId(chatId);
                                    sendApiMethodAsync(message);
                                }
                                userStatus.get(chatId).setInputData(null);
                                break;
                            case "changePhoto":
                                if (messageText.equals("Скасувати")) {
                                    chooseOptionMessage(chatId);
                                } else {
                                    userStatus.get(chatId).getIssue().setPhoto(null);
                                    SendIssue(chatId, userStatus.get(chatId).getIssue(), null);
                                    message = createMessage("Зберегти зміни?");
                                    attachButtons(message, Map.of("Так", "saveIssue", "Ні", "cancelChanges"));
                                    message.setChatId(chatId);
                                    sendApiMethodAsync(message);
                                }
                                userStatus.get(chatId).setInputData(null);
                                break;
                        }
                    }
            }
        }

        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            if (userStatus.getOrDefault(chatId, new UserData()).getInputData().equals("photo")) {
                List<PhotoSize> photos = update.getMessage().getPhoto();
                String fileId = photos.stream()
                        .max(Comparator.comparing(PhotoSize::getFileSize))
                        .orElseThrow().getFileId();
                GetFile getFileRequest = new GetFile();
                getFileRequest.setFileId(fileId);
                try {
                    String filePath = execute(getFileRequest).getFilePath();
                    File file = downloadFile(filePath);
                    String fileName = file.getName() + ".jpg";
                    Path destinationPath = new File("src/main/resources/static/images", fileName).toPath();
                    Files.move(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    String imageUrl = "src/main/resources/static/images/" + fileName;
                    userStatus.get(chatId).getIssue().setPhoto(imageUrl);
                    addNewDefect(chatId, userStatus.get(chatId).getIssue());
                } catch (TelegramApiException | IOException e) {
                    e.printStackTrace();
                }
            }
            if (userStatus.getOrDefault(chatId, new UserData()).getInputData().equals("changePhoto")) {
                List<PhotoSize> photos = update.getMessage().getPhoto();
                String fileId = photos.stream()
                        .max(Comparator.comparing(PhotoSize::getFileSize))
                        .orElseThrow().getFileId();
                GetFile getFileRequest = new GetFile();
                getFileRequest.setFileId(fileId);
                try {
                    String filePath = execute(getFileRequest).getFilePath();
                    File file = downloadFile(filePath);
                    String fileName = file.getName()  + ".jpg";
                    Path destinationPath = new File("src/main/resources/static/images", fileName).toPath();
                    Files.move(file.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    String imageUrl = "src/main/resources/static/images/" + fileName;
                    userStatus.get(chatId).getIssue().setPhoto(imageUrl);
                    SendIssue(chatId, userStatus.get(chatId).getIssue(), null);
                    SendMessage message = createMessage("Зберегти зміни?");
                    attachButtons(message, Map.of("Так", "saveIssue", "Ні", "cancelChanges"));
                    message.setChatId(chatId);
                    sendApiMethodAsync(message);
                } catch (TelegramApiException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(update.hasCallbackQuery()) {
            if(update.getCallbackQuery().getData().startsWith("sendIssue/")) {

                String index = update.getCallbackQuery().getData().substring("sendIssue/".length());
                SendIssue(chatId, issues.get(Long.parseLong(index)), index);
            }
            if(update.getCallbackQuery().getData().startsWith("editIssue/")) {
                String index = update.getCallbackQuery().getData().substring("editIssue/".length());
                SendMessage message = createMessage("Оберіть, що ви хочете змінити");
                message.setChatId(chatId);
                attachButtons(message, Map.of("Номер кімнати", "changeRoom/" + index, "Короткий опис", "changeDesc/" + index, "Фото", "changePhoto/" + index));
                sendApiMethodAsync(message);
            }
            if(update.getCallbackQuery().getData().startsWith("deleteIssue/")) {
                Long index = Long.parseLong(update.getCallbackQuery().getData().substring("deleteIssue/".length()));
                String room = issues.get(index).getRoom();
                restTemplate.delete(HOST + "issues/" + issues.get(index).getId());
                SendMessage message = createMessage("Дефект " + room + " успішно видалено.");
                message.setChatId(chatId);
                sendApiMethodAsync(message);
                chooseOptionMessage(chatId);
            }
            if(update.getCallbackQuery().getData().startsWith("changeRoom/")) {
                Long index = Long.parseLong(update.getCallbackQuery().getData().substring("changeRoom/".length()));
                userStatus.get(chatId).setIssue(issues.get(index));
                userStatus.get(chatId).setInputData("changeRoom");
                SendMessage message = createMessage("Введіть номер кімнати (було - " + userStatus.get(chatId).getIssue().getRoom() + "): ");
                message.setChatId(chatId);
                sendApiMethodAsync(message);
            }
            if(update.getCallbackQuery().getData().startsWith("changeDesc/")) {
                Long index = Long.parseLong(update.getCallbackQuery().getData().substring("changeDesc/".length()));
                userStatus.get(chatId).setIssue(issues.get(index));
                userStatus.get(chatId).setInputData("changeDesc");
                SendMessage message = createMessage("Введіть короткий опис (було - " + userStatus.get(chatId).getIssue().getDescription() + "): ");
                message.setChatId(chatId);
                sendApiMethodAsync(message);
            }
            if(update.getCallbackQuery().getData().startsWith("changePhoto/")) {
                Long index = Long.parseLong(update.getCallbackQuery().getData().substring("changePhoto/".length()));
                userStatus.get(chatId).setIssue(issues.get(index));
                userStatus.get(chatId).setInputData("changePhoto");
                SendMessage message = createMessage("Відправте нове фото (Відправте тектове повідомлення, щоб видалити фото): ");
                message.setChatId(chatId);
                sendApiMethodAsync(message);
            }
            if(update.getCallbackQuery().getData().equals("saveIssue")) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    String jsonPayload = objectMapper.writeValueAsString(userStatus.get(chatId).getIssue());
                    HttpEntity<String> httpEntity = new HttpEntity<>(jsonPayload, headers);
                    restTemplate.put(HOST + "issues", httpEntity);
                    chooseOptionMessage(chatId);
                    setAllIssues();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            if(update.getCallbackQuery().getData().equals("cancelChanges")) {
                chooseOptionMessage(chatId);
                setAllIssues();
            }
        }
    }

    public void removeIssue(Long id) {
        new File(issues.get(id).getPhoto()).delete();
        issues.remove(id);
    }

    public void updateUser(TelegramUser user) {
        userStatus.get(user.getId()).setLoginStatus(user.isActivated());
        userStatus.get(user.getId()).setRole(user.getRole());
    }

    public void deleteUser(Long chatId) {
        userStatus.remove(chatId);
        SendMessage message = createMessage("Ваш акаунт було видалено. Якщо сталася помилка - сповістіть про це власника.");
        message.setChatId(chatId);
        message.setReplyMarkup(getStartKeyboard());
        sendApiMethodAsync(message);
    }

    public void setAllIssues() {
        this.issues = new HashMap<>();
        ResponseEntity<Issue[]> issuesEntity = restTemplate.getForEntity(HOST + "issues", Issue[].class);
        for (Issue issue: issuesEntity.getBody()) {
            this.issues.put(issue.getId(), issue);
        }
    }

    public void SendIssue(Long chatId, Issue issue, String index) {
        if(issue.getPhoto() == null) {
            SendMessage message = createMessage(issue.getRoom() +  " - " + issue.getDescription());
            if(index != null) attachButtons(message, Map.of("Edit", "editIssue/" + index, "Delete", "deleteIssue/" + index));
            message.setChatId(chatId);
            sendApiMethodAsync(message);
        } else {
            SendPhoto photo = new SendPhoto();
            InputFile inputFile = new InputFile();
            inputFile.setMedia(new File(issue.getPhoto()));

            photo.setPhoto(inputFile);
            photo.setCaption(issue.getRoom() +  " - " + issue.getDescription());
            if(index != null) photo.setReplyMarkup(attachButtons(Map.of("Edit", "editIssue/" + index, "Delete", "deleteIssue/" + index)));
            photo.setChatId(chatId);

            executeAsync(photo);
        }
    }



    public void addNewDefect(Long chatId, Issue issue) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            String jsonPayload = objectMapper.writeValueAsString(issue);
            HttpEntity<String> httpEntity = new HttpEntity<>(jsonPayload, headers);
            ResponseEntity<String> responseEntity1 = restTemplate.postForEntity(HOST + "issues", httpEntity, String.class);
            issues.put(issue.getId(), issue);
            SendMessage message = createMessage("Дефект успішно добавлено, ремонтніки скоро все полагодять! ");
            message.setChatId(chatId);
            sendApiMethodAsync(message);
            chooseOptionMessage(chatId);
            message = createMessage("Повідомлення про дефект в кімнаті " + issue.getRoom() + ": " + issue.getDescription());
            ResponseEntity<TelegramUser[]> responseEntity = restTemplate.getForEntity(HOST + "telegramUsers", TelegramUser[].class);
            TelegramUser[] users = responseEntity.getBody();
            for (TelegramUser user: users) {
                if(user.getRole().equals("repair")) {
                    message.setChatId(user.getId());
                    sendApiMethodAsync(message);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

        public void approveRegistration(Long chatId, String role) {
            userStatus.get(chatId).setRole(role);
            userStatus.get(chatId).setLoginStatus(true);
            SendMessage message = createMessage("Вітаю, ваш запит на реєстрацію успішно одобрений!");
            message.setChatId(chatId);
            sendApiMethodAsync(message);
        }
    private void chooseOptionMessage(Long chatId) {
        SendMessage message = createMessage("Оберіть дію");
        message.setChatId(chatId);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        switch (userStatus.getOrDefault(chatId, new UserData()).getRole()) {
            case "technical":
                row.add("Додати дефект");
                row.add("Переглянути дефекти");
                break;
            case "repair":
                row.add("Переглянути дефекти");
                break;
        }
        keyboard.add(row);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(replyKeyboardMarkup);

        sendApiMethodAsync(message);
    }

    private void LoginMessage(Long chatId) {
        SendMessage message = createMessage("Оберіть дію");
        message.setChatId(chatId);
        message.setReplyMarkup(getStartKeyboard());
        sendApiMethodAsync(message);
    }

    private ReplyKeyboardMarkup getStartKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Вхід");
        row.add("Реєстрація");
        keyboard.add(row);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

    public Long getChatId(Update update) {
        if(update.hasMessage()) return update.getMessage().getFrom().getId();
        if(update.hasCallbackQuery()) return update.getCallbackQuery().getFrom().getId();
        return null;
    }

    public static SendMessage createMessage(String text) {
        SendMessage message = new SendMessage();
        message.setText(new String(text.getBytes(), StandardCharsets.UTF_8));
        message.setParseMode("markdown");
        return message;
    }

    public void attachButtons(SendMessage message, Map<String, String> buttons) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList();
        for (String buttonName : buttons.keySet()) {
            String buttonValue = buttons.get(buttonName);

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(new String(buttonName.getBytes(), StandardCharsets.UTF_8));
            button.setCallbackData(buttonValue);
            keyboard.add(Arrays.asList(button));
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
    }

    public InlineKeyboardMarkup attachButtons(Map<String, String> buttons) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList();
        for (String buttonName : buttons.keySet()) {
            String buttonValue = buttons.get(buttonName);

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(new String(buttonName.getBytes(), StandardCharsets.UTF_8));
            button.setCallbackData(buttonValue);
            keyboard.add(Arrays.asList(button));
        }

        markup.setKeyboard(keyboard);
        return markup;
    }

    public void sendImage(String path, Long chatId, String description) {
        SendPhoto photo = new SendPhoto();
        InputFile inputFile = new InputFile();
        inputFile.setMedia(new File(path));

        photo.setPhoto(inputFile);
        photo.setChatId(chatId);

        if(description != null) {
            photo.setCaption(description);
        }

        executeAsync(photo);
    }

}
