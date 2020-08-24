package com.firstlinecode.chalk.core.stream;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.protocol.core.stanza.Stanza;
import com.firstlinecode.basalt.oxm.IOxmFactory;
import com.firstlinecode.chalk.IParsingListener;
import com.firstlinecode.chalk.core.IErrorListener;
import com.firstlinecode.chalk.core.stanza.IStanzaListener;
import com.firstlinecode.chalk.network.IConnection;
import com.firstlinecode.chalk.network.IConnectionListener;

public interface IStream {
	JabberId getJid();
	StreamConfig getStreamConfig();
	
	void send(Stanza stanza);
	
	boolean isConnected();
	
	void close();
	boolean isClosed();
	
	void addStanzaListener(IStanzaListener stanzaListener);
	void removeStanzaListener(IStanzaListener stanzaListener);
	IStanzaListener[] getStanzaListeners();
	
	void addErrorListener(IErrorListener errorListener);
	void removeErrorListener(IErrorListener errorListener);
	IErrorListener[] getErrorListeners();
	
	void addConnectionListener(IConnectionListener connectionListener);
	void removeConnectionListener(IConnectionListener connectionListener);
	void setParsingListener(IParsingListener listener);
	IConnectionListener[] getConnectionListeners();
	
	void addStanzaWatcher(IStanzaWatcher stanzaWatcher);
	void removeStanzaWatcher(IStanzaWatcher stanzaWatcher);
	IStanzaWatcher[] getStanzaWatchers();
	
	IOxmFactory getOxmFactory();
	void setOxmFactory(IOxmFactory oxmFactory);
	
	IConnection getConnection();
}
