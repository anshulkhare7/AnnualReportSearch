package com.anshul.calvinbot;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class CalvinBot extends TelegramLongPollingBot{

	private static final String BOT_TOKEN = "703719936:AAEXo7w1mInMRk2VMZOGk6nz3hTZOzIC698"; 
	private static final String BOT_NAME = "calvinandhobbes_bot";
	private static final String INDEX_NAME = "calvin_and_hobbes";
	private static final Logger log = LoggerFactory.getLogger(CalvinBot.class);
	
	@Override
	public void onUpdateReceived(Update update) {
	
		Long chatId;
		SendMessage message = new SendMessage() ;
		StringBuilder responseText = new StringBuilder("");		
		
		if(update.hasMessage() && update.getMessage().hasText()) {
			String receivedText = update.getMessage().getText().trim().toLowerCase();
			chatId = update.getMessage().getChatId();			
			message.setParseMode(ParseMode.HTML).setChatId(chatId);
			
			if(receivedText.equals("/start")) {
				responseText.append("Welcome to <strong>Calvin & Hobbes Bot</strong>!").append("\n").append("\n");
				responseText.append("This bot is for Calvin & Hobbes fans. Type any <b>search word</b> and I'll find a relevant Calvin & Hobbes comics for you.").append("\n").append("\n");				
				responseText.append("Currently, the search abilities are limited. I can only search single keywords. If you give me multiple search words together, I'll only search for the first word and ignore the rest.").append("\n").append("\n");
				responseText.append("If I find more than one comic for your search word, I'll randomly select one for you. If you want to see more for the same search word, please send me the same word again.").append("\n").append("\n");
				responseText.append("Very soon I'll be upgraded and you'll be allowed to search multiple words.").append("\n").append("\n");
				responseText.append("Please drop me a mail at <b>exponenthash[at]gmail[dot]com</b> to share your feedback.");
			}else {
				String searchWord = receivedText.split(" ")[0];
				log.info("Search Word Received: "+searchWord);
				
				try {									
				    responseText.append("You want me to search: "+searchWord);	
			    	}catch(Exception e) {		    		
			    		log.info("Exception in calling the Calvin Search Engine API."+e.getMessage());
			    	}
			}						
		}
		
		if(responseText.length() < 1) {
			responseText.append("I am sorry. I could not find anything for your search word.").append("\n").append("\n"); 
			responseText.append("It would be great if you could drop me a mail at exponenthash[at]gmail[dot]com explaining what were you trying to search. It will help me improve the bot.");
		}
		
		message.setText(responseText.toString());
		
		try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
	}
	
	@Override
	public String getBotUsername() {
		return BOT_NAME;
	}

	@Override
	public String getBotToken() {
		return BOT_TOKEN;
	}
}

