package com.firstlinecode.chalk.leps.im.subscription;

import com.firstlinecode.basalt.protocol.core.JabberId;

public interface ISubscriptionListener2 {
	void asked(JabberId user, String message);
	void approved(JabberId contact);
	void refused(JabberId contact, String reason);
	void revoked(JabberId user);
	void occurred(SubscriptionError2 error);
}
