package com.ms4systems.devs.core.message.impl;

import java.io.Serializable;
import java.util.ArrayList;

import com.ms4systems.devs.core.message.Message;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.model.AtomicModel;



public class MessageBagImpl extends MessageBag {
	private static final long serialVersionUID = 1L;
	private double messageTime;

	public MessageBagImpl() { super(); }
	
	public MessageBagImpl(MessageBag otherBag) {
		super(otherBag);
		this.setMessageTime(otherBag.getMessageTime());
	}
	
	@Override
	public MessageBag getMessages(Port<?> port) {
		MessageBag messages = null;
		for (Message<?> msg : this) {
			if (!msg.hasPort()) continue;
			if (msg.getPort().equals(port)) {
				if (messages==null) {
					messages = new MessageBagImpl();
					messages.setMessageTime(getMessageTime());
				}
				messages.add(msg);
			}
		}
		if (messages==null) return MessageBag.EMPTY;
		return messages;
	}


	@Override
	public MessageBag getMessages(AtomicModel model) {
		MessageBag messages = null;
		for (Message<?> msg : this) {
			if (!msg.hasPort()) continue;
			if (msg.getPort().getModel().equals(model)) {
				if (messages==null) {
					messages = new MessageBagImpl();
					messages.setMessageTime(getMessageTime());
				}
				messages.add(msg);
			}
		}
		if (messages==null) return MessageBag.EMPTY;
		return messages;
	}
	
	@Override
	public boolean hasMessages(Port<?> port) {
		for (Message<?> msg : this) {
			if (!msg.hasPort()) continue;
			if (msg.getPort().equals(port)) return true;
		}
		return false;
	}

	@Override
	public boolean hasMessages(AtomicModel model) {
		for (Message<?> msg : this) {
			if (!msg.hasPort()) continue;
			if (msg.getPort().getModel().equals(model)) return true;
		}
		return false;
	}
	
	@Override
	public ArrayList<Port<?>> portsWithMessages() {
		ArrayList<Port<?>> ports = new ArrayList<Port<?>>();
		for (Message<?> msg : this) {
			if (!msg.hasPort()) continue;
			ports.add(msg.getPort());
		}
		return ports;
	}

	/***
	 * Compares MessageBags for their time values
	 */
	@Override
	public int compareTo(MessageBag other) {
		return Double.compare(this.getMessageTime(), other.getMessageTime());
	}

	@Override
	public ArrayList<AtomicModel> getModelsWithMessages() {
		ArrayList<AtomicModel> lst = new ArrayList<AtomicModel>();
		for (Message<?> msg : this) {
			final AtomicModel model = msg.getPort().getModel();
			if (!lst.contains(model)) lst.add(model);
		}
		return lst;
	}

	@Override
	public void setMessageTime(double messageTime) {
		this.messageTime = messageTime;
	}

	@Override
	public double getMessageTime() {
		return messageTime;
	}

	@Override
	public <T extends Serializable> void add(Port<T> port, T data) {
		add(port.createMessage(data));
	}
	
}
