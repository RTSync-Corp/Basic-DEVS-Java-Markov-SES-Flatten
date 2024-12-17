package com.ms4systems.devs.core.message.impl;

import java.io.Serializable;
import java.util.ArrayList;

import com.ms4systems.devs.core.message.Message;
import com.ms4systems.devs.core.message.MessageBag;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.exception.PortTypeException;

public class PortImpl<T extends Serializable> implements Port<T> {
	private static final long serialVersionUID = 1L;
	protected Class<T> type;
	protected String name;
	protected Direction direction;
	protected AtomicModel model;
	
	public PortImpl(AtomicModel model, String name, Class<T> type, Direction direction) {
		setType(type);
		setName(name);
		setDirection(direction);
		setModel(model);
	}
	
	@Override
	public String toString() {
		return getName() + " (" + type.getSimpleName() + " " + (direction==Port.Direction.INPUT ? "input" : "output") + ")"; 
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Message<T> createMessage(T data) {
		return new MessageImpl<T>(this,data);
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	
	@Override
	public AtomicModel getModel() {
		return model;
	}

	@Override
	public void setModel(AtomicModel model) {
		this.model = model;
	}

	@Override
	public Direction getDirection() {
		return direction;
	}

	@Override
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	@SuppressWarnings("unchecked")
	@Override
	public MessageBag replicateOnPort(MessageBag input) {
		if (input==null || input.isEmpty()) return MessageBag.EMPTY;
		MessageBag bag = new MessageBagImpl();
		for (Message<?> m : input) {
			if (m==null) continue;
			
			// Check message data to make sure it can belong on this port
			if (m.hasData() && !type.isInstance(m.getData())) 
				throw new PortTypeException("Message type " + m.getData().getClass() +
						" incompatible with port type " + type);
			final Message<T> msg = (Message<T>) m;
			bag.add(msg.replicateOnPort(this));
		}
		return bag;
	}
	
	protected void setType(Class<T> type) {
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Message<T>> getMessages(MessageBag input) {
		ArrayList<Message<T>> messages = new ArrayList<Message<T>>();
		for (Message<? extends Serializable> m : input.getMessages(this))
			messages.add((Message<T>) m);
		return messages;
	}
	
	@Override
	public ArrayList<T> getData(MessageBag input) {
		ArrayList<Message<T>> messageList = getMessages(input);
		ArrayList<T> dataList = new ArrayList<T>(messageList.size());
		for (Message<T> message : messageList) {
			dataList.add(message.getData());
		}
		return dataList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Port))
			return false;
		Port<?> other = (Port<?>) obj;
		if (direction != other.getDirection())
			return false;
		if (model == null) {
			if (other.getModel() != null)
				return false;
		} else if (!model.equals(other.getModel()))
			return false;
		if (name == null) {
			if (other.getName() != null)
				return false;
		} else if (!name.equals(other.getName()))
			return false;
		if (type == null) {
			if (other.getType() != null)
				return false;
		} else if (!type.equals(other.getType()))
			return false;
		return true;
	}
	
	
	
}
