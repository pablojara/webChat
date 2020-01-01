package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class ChatManagerTest1 {

	
	
	public static class threadExecTask
	implements Supplier<String>, Callable<String> {

		public ChatManager chatManager;
		public int userNumber;
		
		public threadExecTask(int userNumber, ChatManager chatManager) {
			this.userNumber = userNumber;
			this.chatManager = chatManager;
		}
		
		@Override
		public String call()  throws InterruptedException, TimeoutException {
			TestUser user = new TestUser("user"+userNumber);
			chatManager.newUser(user);
			for(int i = 0; i < 5; i++) {
				System.out.println("Thread");
				String chatIteration = "chat" + i;
				chatManager.newChat(chatIteration, 5, TimeUnit.SECONDS);
				chatManager.getChat(chatIteration).addUser(user);
				Collection<User> userList = chatManager.getChat(chatIteration).getUsers();
				System.out.println(userList.toString());
			}
			return null;
		}

		@Override
		public String get() {
			// TODO Auto-generated method stub
			return null;
		}
}
	
	@Test
	public void improvement() throws InterruptedException, TimeoutException, ExecutionException {

		// Crear el chat Manager
		ChatManager chatManager = new ChatManager(50);
		
		ExecutorService executor = Executors.newFixedThreadPool(10);
		
		CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
		
		Future<String> future1 = completionService.submit(new threadExecTask(1, chatManager));
		Future<String> future2 = completionService.submit(new threadExecTask(2, chatManager));
		Future<String> future3 = completionService.submit(new threadExecTask(3, chatManager));
		Future<String> future4 = completionService.submit(new threadExecTask(4, chatManager));

		try {
			   future1.get();
			} catch (InterruptedException | ConcurrentModificationException ex) {
			   ex.getCause().printStackTrace();
		}
		try {
			   future2.get();
			} catch (InterruptedException | ConcurrentModificationException ex) {
			   ex.getCause().printStackTrace();
		}
		try {
			   future3.get();
			} catch (InterruptedException | ConcurrentModificationException ex) {
			   ex.getCause().printStackTrace();
		}
		try {
			   future4.get();
			} catch (InterruptedException | ConcurrentModificationException ex) {
			   ex.getCause().printStackTrace();
		}
		
		for (Chat chat : chatManager.getChats()) {
			assertTrue("Missing some of the 4 users in chat " + chat.getName(),
					Objects.equals(chat.getUsers().size(), 4));
		}


	}
	
}
