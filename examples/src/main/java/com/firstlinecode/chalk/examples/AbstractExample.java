package com.firstlinecode.chalk.examples;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.xeps.ibr.IqRegister;
import com.firstlinecode.basalt.xeps.ibr.RegistrationField;
import com.firstlinecode.basalt.xeps.ibr.RegistrationForm;
import com.firstlinecode.chalk.core.IChatClient;
import com.firstlinecode.chalk.core.StandardChatClient;
import com.firstlinecode.chalk.core.stream.StandardStreamConfig;
import com.firstlinecode.chalk.core.stream.StreamConfig;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.ConnectionListenerAdapter;
import com.firstlinecode.chalk.xeps.ibr.IRegistration;
import com.firstlinecode.chalk.xeps.ibr.IRegistrationCallback;
import com.firstlinecode.chalk.xeps.ibr.IbrPlugin;
import com.firstlinecode.chalk.xeps.ibr.RegistrationException;

public abstract class AbstractExample extends ConnectionListenerAdapter implements Example {
	protected Options options;
	
	public AbstractExample() {
		super();
	}

	@Override
	public void init(Options options) {
		this.options = options;
		
		try {
			createUsers();
		} catch (RegistrationException e) {
			throw new RuntimeException("Can't create user.", e);
		}
		
		doInit();
	}

	protected void doInit() {}
	
	protected abstract String[][] getUserNameAndPasswords();

	protected StandardStreamConfig createStreamConfig(String resource) {
		StandardStreamConfig streamConfig = new StandardStreamConfig(options.host, options.port);
		streamConfig.setTlsPreferred(true);
		streamConfig.setResource(resource);
		
		streamConfig.setProperty(StreamConfig.PROPERTY_NAME_CHALK_MESSAGE_FORMAT, options.messageFormat);
		
		return streamConfig;
	}

	protected JabberId getJabberId(String user) {
		return getJabberId(user, null);
	}

	protected JabberId getJabberId(String user, String resource) {
		if (resource == null) {
			return JabberId.parse(String.format("%s@%s", user, options.host));			
		}
		
		return JabberId.parse(String.format("%s@%s/%s", user, options.host, resource));
	}

	protected StandardStreamConfig createStreamConfig() {
		return createStreamConfig("chalk_" + getExampleName() + "_example");
	}

	protected String getExampleName() {
		String className = getClass().getSimpleName();
		if (className.endsWith("Example")) {
			return className.substring(0, className.length() - 7);
		}
		
		throw new IllegalArgumentException("Can't determine example name. You should override getExampleName() method to resolve the problem.");
	}

	protected void createUsers() throws RegistrationException {
		String[][] userNameAndPaswords = getUserNameAndPasswords();
		if (userNameAndPaswords == null || userNameAndPaswords.length == 0)
			return;
		
		IChatClient chatClient = new StandardChatClient(createStreamConfig());
		chatClient.register(IbrPlugin.class);
		
		IRegistration registration = chatClient.createApi(IRegistration.class);
		registration.addConnectionListener(this);
		
		for (final String[] userNameAndPassword : userNameAndPaswords) {
			registration.register(new IRegistrationCallback() {
	
				@Override
				public Object fillOut(IqRegister iqRegister) {
					if (iqRegister.getRegister() instanceof RegistrationForm) {
						RegistrationForm form = new RegistrationForm();
						form.getFields().add(new RegistrationField("username", userNameAndPassword[0]));
						form.getFields().add(new RegistrationField("password", userNameAndPassword[1]));
						
						return form;
					} else {
						throw new RuntimeException("Can't get registration form.");
					}
				}
				
			});
		}
		
		chatClient.close();
	}

	@Override
	public void exceptionOccurred(ConnectionException exception) {}

	@Override
	public void messageReceived(String message) {
		printString("<- " + message);
	}

	@Override
	public void messageSent(String message) {
		printString("-> " + message);
	}

	protected void printString(String string) {
		System.out.println(string);
	}

	protected void printException(Exception e) {
		System.out.println("Exception:");
		e.printStackTrace(System.out);
		System.out.println();
	}

}