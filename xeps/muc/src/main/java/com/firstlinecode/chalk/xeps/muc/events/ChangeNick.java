package com.firstlinecode.chalk.xeps.muc.events;

import com.firstlinecode.basalt.protocol.core.JabberId;
import com.firstlinecode.basalt.xeps.muc.Affiliation;
import com.firstlinecode.basalt.xeps.muc.Role;

public class ChangeNick {
	private String oldNick;
	private String newNick;
	private Affiliation affiliation;
	private Role role;
	private JabberId jid;
	private int oldNickSessions;
	
	public ChangeNick(String oldNick, String newNick, Affiliation affiliation, Role role) {
		this.oldNick = oldNick;
		this.newNick = newNick;
		this.affiliation = affiliation;
		this.role = role;
	}

	public String getOldNick() {
		return oldNick;
	}

	public String getNewNick() {
		return newNick;
	}

	public Affiliation getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(Affiliation affiliation) {
		this.affiliation = affiliation;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public JabberId getJid() {
		return jid;
	}

	public void setJid(JabberId jid) {
		this.jid = jid;
	}

	public void setOldNick(String oldNick) {
		this.oldNick = oldNick;
	}

	public void setNewNick(String newNick) {
		this.newNick = newNick;
	}

	public int getOldNickSessions() {
		return oldNickSessions;
	}

	public void setOldNickSessions(int oldNickSessions) {
		this.oldNickSessions = oldNickSessions;
	}
	
}
