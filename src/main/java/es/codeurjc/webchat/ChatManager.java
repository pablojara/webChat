package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import es.codeurjc.webchat.Chat.sendMessageTask;

public class ChatManager {

	private Map<String, Chat> chats = new ConcurrentHashMap<>();
	private Map<String, User> users = new ConcurrentHashMap<>();
	private int maxChats;

	public class newChatTask
	implements Supplier<String>, Callable<String> {

		public Chat chat;
		public User user;
		
		public newChatTask(Chat chat, User user) {
			this.chat = chat;
			this.user = user;
		}
		
		@Override
		public String call()  throws InterruptedException, TimeoutException {
			user.newChat(chat);
			return null;
		}

		@Override
		public String get() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public class closedChatTask
	implements Supplier<String>, Callable<String> {

		public Chat chat;
		public User user;
		
		public closedChatTask(Chat chat, User user) {
			this.chat = chat;
			this.user = user;
		}
		
		@Override
		public String call()  throws InterruptedException, TimeoutException {
			user.chatClosed(chat);
			return null;
		}

		@Override
		public String get() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public ChatManager(int maxChats) {
		this.maxChats = maxChats;
	}

	public void newUser(User user) {
		
		if(users.containsKey(user.getName())){
			throw new IllegalArgumentException("There is already a user with name \'"
					+ user.getName() + "\'");
		} else {
			users.putIfAbsent(user.getName(), user);
		}
	}

	public Chat newChat(String name, long timeout, TimeUnit unit) throws InterruptedException,
			TimeoutException {

		if (chats.size() == maxChats) {
			throw new TimeoutException("There is no enought capacity to create a new chat");
		}

		if(chats.containsKey(name)){
			return chats.get(name);
		} else {
			Chat newChat = new Chat(this, name);
			chats.putIfAbsent(name, newChat);
			
			ExecutorService executor = Executors.newFixedThreadPool(10);
			CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
			for(User user : users.values()){
				completionService.submit(new newChatTask(newChat, user));
			}

			return newChat;
		}
	}

	public void closeChat(Chat chat) {
		Chat removedChat = chats.remove(chat.getName());
		if (removedChat == null) {
			throw new IllegalArgumentException("Trying to remove an unknown chat with name \'"
					+ chat.getName() + "\'");
		}

		ExecutorService executor = Executors.newFixedThreadPool(10);
		CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
		for(User user : users.values()){
			completionService.submit(new closedChatTask(removedChat, user));
		}
	}

	public Collection<Chat> getChats() {
		return Collections.unmodifiableCollection(chats.values());
	}

	public Chat getChat(String chatName) {
		return chats.get(chatName);
	}

	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	public User getUser(String userName) {
		return users.get(userName);
	}

	public void close() {}
}
