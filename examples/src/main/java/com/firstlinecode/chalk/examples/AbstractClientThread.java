package com.firstlinecode.chalk.examples;

import com.firstlinecode.chalk.core.StandardChatClient;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.ConnectionListenerAdapter;

public abstract class AbstractClientThread extends ConnectionListenerAdapter implements Runnable {
	protected MultiClientsExample example;
	protected StandardChatClient chatClient;
	
	public AbstractClientThread(StandardChatClient chatClient, MultiClientsExample example) {
		this.chatClient = chatClient;
		this.example = example;
	}
	
	@Override
	public void run() {
		chatClient.getStream().addConnectionListener(this);
		
		try {
			String[] userNameAndPassword = getUserNameAndPassword();
			chatClient.connect(userNameAndPassword[0], userNameAndPassword[1]);
			
			doRun();
		} catch (Exception e) {
			example.printException(e);
		}
	}
	
	@Override
	public void messageSent(String message) {
		example.printString(getClass().getSimpleName() + " -> " + message);
	}
	
	@Override
	public void messageReceived(String message) {
		example.printString(getClass().getSimpleName() + " <- " + message);
	}
	
	@Override
	public void exceptionOccurred(ConnectionException exception) {}
	
	protected abstract String[] getUserNameAndPassword();
	protected abstract void doRun() throws Exception;
	protected abstract String getResourceName();
	
}