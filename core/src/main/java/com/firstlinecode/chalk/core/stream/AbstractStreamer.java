package com.firstlinecode.chalk.core.stream;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firstlinecode.basalt.oxm.OxmService;
import com.firstlinecode.basalt.oxm.annotation.AnnotatedParserFactory;
import com.firstlinecode.basalt.oxm.parsers.core.stream.StreamParser;
import com.firstlinecode.basalt.oxm.parsing.IParsingFactory;
import com.firstlinecode.basalt.protocol.Constants;
import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.ProtocolChain;
import com.firstlinecode.chalk.core.stream.negotiants.AbstractStreamNegotiant;
import com.firstlinecode.chalk.network.ConnectionException;
import com.firstlinecode.chalk.network.ConnectionListenerAdapter;
import com.firstlinecode.chalk.network.IConnection;
import com.firstlinecode.chalk.network.IConnectionListener;
import com.firstlinecode.chalk.network.SocketConnection;

public abstract class AbstractStreamer extends ConnectionListenerAdapter implements IStreamer {
	private static final Logger logger = LoggerFactory.getLogger(AbstractStreamer.class);
	
	private static final String PROPERTY_NAME_CHALK_NEGOTIATION_READ_RESPONSE_TIMEOUT = "chalk.negotiation.read.response.timeout";
	private static final int DEFAULT_CONNECT_TIMEOUT = 10 * 1000;
	
	public static final Object NEGOTIATION_KEY_FEATURES = new Object();
	public static final Object NEGOTIATION_KEY_BINDED_CHAT_ID = new Object();
	
	protected StreamConfig streamConfig;
	
	protected INegotiationListener negotiationListener;
	protected IConnectionListener connectionListener;
	protected IConnection connection;
	
	protected int connectTimeout;
	
	private volatile boolean done;
	
	protected IParsingFactory parsingFactory;
	
	protected IAuthenticationToken authToken;
	
	protected IAuthenticationCallback authenticationCallback;
	
	public AbstractStreamer(StreamConfig streamConfig) {
		this(streamConfig, null);
	}
	
	public AbstractStreamer(StreamConfig streamConfig, IConnection connection) {
		if (streamConfig == null)
			throw new IllegalArgumentException("Null stream config.");
		
		this.streamConfig = streamConfig;
		System.setProperty(StreamConfig.PROPERTY_NAME_CHALK_MESSAGE_FORMAT, streamConfig.getProperty(StreamConfig.PROPERTY_NAME_CHALK_MESSAGE_FORMAT, Constants.MESSAGE_FORMAT_XML));
		if (connection != null) {
			this.connection = connection;
		} else {
			this.connection = createConnection(streamConfig);
		}
		
		connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		
		parsingFactory = OxmService.createParsingFactory();
		parsingFactory.register(ProtocolChain.first(com.firstlinecode.basalt.protocol.core.stream.Stream.PROTOCOL),
			new AnnotatedParserFactory<>(StreamParser.class));
		
		done = false;
	}

	protected IConnection createConnection(StreamConfig streamConfig) {
		String messageFormat = streamConfig.getProperty(StreamConfig.PROPERTY_NAME_CHALK_MESSAGE_FORMAT, "xml");
		return new SocketConnection(messageFormat);
	}
	
	@Override
	public void negotiate(IAuthenticationToken authToken) {
		if (done) {
			return;
		}
		
		connection.addListener(this);
		
		this.authToken = authToken;
		new Thread(new NegotiationThread()).start();
	}
	
	@Override
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	@Override
	public int getConnectTimeout() {
		return connectTimeout;
	}
	
	@Override
	public IConnection getConnection() {
		return connection;
	}
	
	private class NegotiationThread implements Runnable {

		@Override
		public void run() {
			INegotiationContext context = new NegotiationContext(connection);
			try {
				context.connect(streamConfig.getHost(), streamConfig.getPort(), connectTimeout);
				
				List<IStreamNegotiant> negotiants = createNegotiants();
				for (IStreamNegotiant negotiant : negotiants) {
					negotiationListener.before(negotiant);
					context.addListener(negotiant);
					negotiant.negotiate(context);
					context.removeListener(negotiant);
					negotiationListener.after(negotiant);
					
					if (context.isClosed())
						return;
				}
			} catch (NegotiationException e) {
				if (!context.isClosed())
					context.close();
				
				if (logger.isDebugEnabled())
					logger.debug("Negotiation exception.", e);
				
				if (negotiationListener != null) {
					negotiationListener.occurred(e);
				}
				
				return;
			} catch (ConnectionException e) {
				if (logger.isDebugEnabled())
					logger.debug("Connection exception.", e);
				
				if (!context.isClosed())
					context.close();
				
				if (connectionListener != null) {
					connectionListener.exceptionOccurred(e);
				}
				
				return;
			}
			
			done = true;
			if (negotiationListener != null) {
				JabberId jid = (JabberId)context.getAttribute(StandardStreamer.NEGOTIATION_KEY_BINDED_CHAT_ID);
				negotiationListener.done(new Stream(jid, streamConfig, connection));
			}
		}
		
	}

	protected void setNegotiationReadResponseTimeout(List<IStreamNegotiant> negotiants) {
		String sTimeout = System.getProperty(PROPERTY_NAME_CHALK_NEGOTIATION_READ_RESPONSE_TIMEOUT);
		
		if (sTimeout == null)
			return;
		
		long timeout;
		try {
			timeout = Long.parseLong(sTimeout);
		} catch (NumberFormatException e) {
			return;
		}
		
		for (IStreamNegotiant negotiant : negotiants) {
			if (negotiant instanceof AbstractStreamNegotiant) {
				((AbstractStreamNegotiant)negotiant).setReadResponseTimeout(timeout);
			}
		}
	}

	@Override
	public void setNegotiationListener(INegotiationListener negotiationListener) {
		this.negotiationListener = negotiationListener;
	}
	
	@Override
	public INegotiationListener getNegotiationListener() {
		return negotiationListener;
	}

	@Override
	public void setConnectionListener(IConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
	}

	@Override
	public IConnectionListener getConnectionListener() {
		return connectionListener;
	}

	@Override
	public void exceptionOccurred(ConnectionException exception) {
		// Ignore. Connection exception is processed by negotiant
	}

	@Override
	public void messageReceived(String message) {
		if (!done && connectionListener != null) {
			connectionListener.messageReceived(message);
		}
	}

	@Override
	public void messageSent(String message) {
		if (!done && connectionListener != null) {
			connectionListener.messageSent(message);
		}
	}
	
	public StreamConfig getStreamConfig() {
		return streamConfig;
	}
	
	@Override
	public void setAuthenticationCallback(IAuthenticationCallback authenticationCallback) {
		this.authenticationCallback = authenticationCallback;
	}

	@Override
	public IAuthenticationCallback getAuthenticationCallback() {
		return authenticationCallback;
	}
	
	protected abstract List<IStreamNegotiant> createNegotiants();
	
}
