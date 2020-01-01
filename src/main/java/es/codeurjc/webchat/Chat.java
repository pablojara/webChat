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

	public class threadExecTask
	implements Supplier<String>, Callable<String> {

		public Chat chat;
		public User sender;
		public User receiver;
		public String message;
		
		public threadExecTask(Chat chat, User sender, User receiver, String message) {
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
	
	public Chat(ChatManager chatManager, String name) {
		this.chatManager = chatManager;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addUser(User user) {
		users.putIfAbsent(user.getName(), user);
		for(User u : users.values()){
			if (u != user) {
				u.newUserInChat(this, user);
			}
		}
	}

	public void removeUser(User user) {
		users.remove(user.getName());
		for(User u : users.values()){
			u.userExitedFromChat(this, user);
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
			completionService.submit(new threadExecTask(this, user, u, message));
			u.newMessage(this, user, message);
		}
	}

	public void close() {
		this.chatManager.closeChat(this);
	}
}
