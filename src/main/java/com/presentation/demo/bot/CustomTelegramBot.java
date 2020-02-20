package com.presentation.demo.bot;

import com.presentation.demo.bot.config.TelegramBotConfig;
import com.presentation.demo.service.user.UserService;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class CustomTelegramBot extends TelegramLongPollingBot {

    private static Logger TELEGRAM_LOGGER = LoggerFactory.getLogger(CustomTelegramBot.class);


    @Autowired
    private TelegramBotConfig telegramBotConfig;

    @Autowired
    private TelegramBotsApi telegramBotsApi;

    private DefaultBotOptions botOptions;

    DefaultBotOptions getBotOptions(){
        return botOptions;
    }

    void setBotOptions(DefaultBotOptions botOptions){//no way to put it into superclass
        this.botOptions = botOptions;
    }

    @PostConstruct
    @Transactional
    public void register(){
        try{
            DefaultBotOptions myBotOptions = ApiContext.getInstance(DefaultBotOptions.class);
            if (telegramBotConfig.getProxy()) {
                myBotOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
                HttpHost httpHost = new HttpHost(telegramBotConfig.getProxyHost(), telegramBotConfig.getProxyPort());
                RequestConfig myConfig = RequestConfig.custom().setProxy(httpHost).build();
                myBotOptions.setRequestConfig(myConfig);
            }
            this.setBotOptions(myBotOptions);
            telegramBotsApi.registerBot(this);
        }
        catch (TelegramApiException apiExc){
            apiExc.printStackTrace();
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
        Message inputMessage = update.getMessage();
        if (Objects.nonNull(inputMessage) && inputMessage.hasText()) {
            SendMessage sendMessage = commandHandler(inputMessage.getText(),inputMessage.getChatId(),inputMessage.getForwardFrom());
            TELEGRAM_LOGGER.info("Get message: " + inputMessage + ". From: " + inputMessage.getAuthorSignature());
            try {
                this.execute(sendMessage);
            } catch (TelegramApiException e) {
                TELEGRAM_LOGGER.info(e.getLocalizedMessage());
            }
        }
    }

    public SendMessage commandHandler(String command, Long chatId, User botUser){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        TELEGRAM_LOGGER.info(command);
        if (command.equals("/start")){
            String welcomeOutputText = "Welcome to NCBestBankBot, \n I will help you to know everything about your bank account!";
            message.setText(welcomeOutputText);
        }
        else{
            message.setText("This is the end of work activity");
        }
        return message;

    }

    @Override
    public String getBotUsername() {
        return "NcBestBankBot";
    }

    @Override
    public String getBotToken() {
        return "853292773:AAGFEDW4EPL1W8WiGEALuFf-bolauMPfjoQ";
    }
}