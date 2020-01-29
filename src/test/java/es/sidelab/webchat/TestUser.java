package es.sidelab.webchat;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.User;

public class TestUser implements User {

	public String name;
	public CountDownLatch latch;
	public LinkedList<String> receivedMessages = new LinkedList<String>();
	public boolean sleepMessage;

	public TestUser(String name) {
		this.name = name;
		this.latch = new CountDownLatch(8); 
		sleepMessage = false;
	}
	
	public TestUser(String name, CountDownLatch latch) {
		this.name = name;
		this.latch = latch;
		sleepMessage = true;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public String getColor(){
		return "007AFF";
	}

	@Override
	public void newChat(Chat chat) {
		System.out.println("New chat " + chat.getName());
	}

	@Override
	public void chatClosed(Chat chat) {
		System.out.println("Chat " + chat.getName() + " closed ");
	}

	@Override
	public void newUserInChat(Chat chat, User user) {
		System.out.println("New user " + user.getName() + " in chat " + chat.getName());
	}

	@Override
	public void userExitedFromChat(Chat chat, User user) {
		System.out.println("User " + user.getName() + " exited from chat " + chat.getName());
	}

	@Override
	public void newMessage(Chat chat, User user, String message) {
		System.out.println("New message '" + message + "' from user " + user.getName()
				+ " in chat " + chat.getName());
		if(sleepMessage) {
		
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.latch.countDown();
		receivedMessages.addFirst(message);
	}

	@Override
	public String toString() {
		return "User[" + name + "]";
	}	
	
	public LinkedList<String> getReceivedMessages(){
		return receivedMessages;
	}
}
