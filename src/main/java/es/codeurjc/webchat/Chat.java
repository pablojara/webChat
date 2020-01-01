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

public class Chat {

	private String name;
	private Map<String, User> users = new ConcurrentHashMap<>();

	private ChatManager chatManager;

	public class sendMessageTask
	implements Supplier<String>, Callable<String> {

		public Chat chat;
		public User sender;
		public User receiver;
		public String message;
		
		public sendMessageTask(Chat chat, User sender, User receiver, String message) {
			this.chat = chat;
			this.sender = sender;
			this.receiver = receiver;
			this.message = message;
		}
		
		@Override
		public String call()  throws InterruptedException, TimeoutException {
			receiver.newMessage(chat, sender, message);
			return null;
		}

		@Override
		public String get() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public class newUserInChatTask
	implements Supplier<String>, Callable<String> {

		public Chat chat;
		public User newUser;
		public User receiver;
		
		public newUserInChatTask(Chat chat, User newUser, User receiver) {
			this.chat = chat;
			this.newUser = newUser;
			this.receiver = receiver;
		}
		
		@Override
		public String call()  throws InterruptedException, TimeoutException {
			receiver.newUserInChat(chat, newUser);
			return null;
		}

		@Override
		public String get() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public class userExitedFromChatTask
	implements Supplier<String>, Callable<String> {

		public Chat chat;
		public User exitedUser;
		public User receiver;
		
		public userExitedFromChatTask(Chat chat, User exitedUser, User receiver) {
			this.chat = chat;
			this.exitedUser = exitedUser;
			this.receiver = receiver;
		}
		
		@Override
		public String call()  throws InterruptedException, TimeoutException {
			receiver.userExitedFromChat(chat, exitedUser);
			return null;
		}

		@Override
		public String get() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public Chat(ChatManager chatManager, String name) {
		this.chatManager = chatManager;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addUser(User user) {
		users.putIfAbsent(user.getName(), user);
		
		ExecutorService executor = Executors.newFixedThreadPool(10);
		CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
		for(User u : users.values()){
			if (u != user) {
			completionService.submit(new newUserInChatTask(this, user, u));
			}
		}
	}

	public void removeUser(User user) {
		users.remove(user.getName());
		ExecutorService executor = Executors.newFixedThreadPool(10);
		CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
		for(User u : users.values()){
			completionService.submit(new userExitedFromChatTask(this, user, u));
		}
	}

	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	public User getUser(String name) {
		return users.get(name);
	}

	public void sendMessage(User user, String message) {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
		for(User u : users.values()){
			completionService.submit(new sendMessageTask(this, user, u, message));
		}
	}

	public void close() {
		this.chatManager.closeChat(this);
	}
}
