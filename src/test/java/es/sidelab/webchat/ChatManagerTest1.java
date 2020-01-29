package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class ChatManagerTest1 {

	public class threadExecTask
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
	
	@Test
	public void improvement4ParalelNotify() throws InterruptedException, TimeoutException, ExecutionException {

		ChatManager chatManager = new ChatManager(50);
		
		CountDownLatch latch = new CountDownLatch(4); 
					
		TestUser user0 = new TestUser("User0", latch);
		TestUser user1 = new TestUser("User1", latch);
		TestUser user2 = new TestUser("User2", latch);
		TestUser user3 = new TestUser("User3", latch);

		chatManager.newUser(user0);
		chatManager.newUser(user1);
		chatManager.newUser(user2);
		chatManager.newUser(user3);
		
		String chatName = "TestChat";
		
		Chat testChat = chatManager.newChat(chatName, 5, TimeUnit.SECONDS);
		chatManager.getChat(chatName).addUser(user0);
		chatManager.getChat(chatName).addUser(user1);
		chatManager.getChat(chatName).addUser(user2);
		chatManager.getChat(chatName).addUser(user3);
		
		long start = System.currentTimeMillis();
		
		testChat.sendMessage(user0, "test message");
		
		latch.await();
		
		long end = System.currentTimeMillis();
		long elapsedTime = end - start;
		
		assertTrue("Time to send messages is more than 1000 miliseconds.", elapsedTime < 1100);
		
	}
	
	@Test
	public void improvement4MessageOrder() throws InterruptedException, TimeoutException, ExecutionException {

		ChatManager chatManager = new ChatManager(50);
							
		TestUser user0 = new TestUser("User0");
		TestUser user1 = new TestUser("User1");
		
		chatManager.newUser(user0);
		chatManager.newUser(user1);
		
		String chatName = "TestChat";
		
		Chat testChat = chatManager.newChat(chatName, 5, TimeUnit.SECONDS);
		chatManager.getChat(chatName).addUser(user0);
		chatManager.getChat(chatName).addUser(user1);
		
		for(int i = 0; i < 5; i++) {
			testChat.sendMessage(user0, String.valueOf(i));
		}
		
		LinkedList<String> receivedMessages = user1.getReceivedMessages();
		
		System.out.println(receivedMessages.size() + "sizeeeeeeeeeeeeeee");
		
		for(int i = 0; i < 5; i++) {
			String message = receivedMessages.removeLast();
			assertTrue("Message " + message + " should be " + String.valueOf(i), message.compareTo(String.valueOf(i)) == 0);
		}
	}
	
}
