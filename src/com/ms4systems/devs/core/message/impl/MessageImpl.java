package com.ms4systems.devs.core.message.impl;

import java.io.Serializable;

import com.ms4systems.devs.core.message.Message;
import com.ms4systems.devs.core.message.Port;
import com.ms4systems.devs.core.model.AtomicModel;
import com.ms4systems.devs.exception.PortTypeException;

public class MessageImpl<T extends Serializable> implements Message<T> {
	private static final long serialVersionUID = 1L;
	private Port<?> port;
	private T data;
	private Message<T> messageTrace;
	
	/***
	 * Create a default empty message
	 */
	public MessageImpl() {
		this(null,null);
	}
	
	/***
	 * Create a message with a given port and data
	 * @param port The port to use
	 * @param data The data of the message
	 */
	public MessageImpl(Port<?> port, T data){
		setPort(port);
		setData(data);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<T> getType() {
		if (getData()==null) return null;
		return (Class<T>) getData().getClass();
	}

	@Override
	public boolean hasData() {
		return getData()!=null;
	}

	@Override
	public boolean isValid() {
		return getData()!=null && getPort()!=null;
	}

	@Override
	public Message<T> replicateOnPort(Port<?> port) {
		if (hasData() && !port.getType().isInstance(getData())) 
			throw new PortTypeException("Data type does not match port type of " + port.getType().getCanonicalName());
		final MessageImpl<T> messageImpl = new MessageImpl<T>(port,getData());
		messageImpl.setMessageTrace(this);
		return messageImpl;
	}

	@Override
	public boolean hasPort() {
		return getPort()!=null;
	}

	@Override
	public boolean isEmpty() {
		return !hasPort() && !hasData();
	}

	@Override
	public AtomicModel getModel() {
		if (!hasPort()) return null;
		return getPort().getModel();
	}

	@Override
	public void setPort(Port<?> port) {
		this.port = port;
	}

	@Override
	public Port<?> getPort() {
		return port;
	}

	@Override
	public void setData(T data) {
		this.data = data;
	}

	@Override
	public T getData() {
		return data;
	}

	@Override
	public void setMessageTrace(Message<T> messageTrace) {
		this.messageTrace = messageTrace;
	}

	@Override
	public Message<T> getMessageTrace() {
		return messageTrace;
	}
	
	@Override
	public String toString() {
		if (isEmpty()) return "";
		if (!hasData()) return getPort().getName();
		return getPort().getName() + ": " + getData().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Message))
			return false;
		Message<?> other = (Message<?>) obj;
		if (data == null) {
			if (other.getData() != null)
				return false;
		} else if (!data.equals(other.getData()))
			return false;
		if (port == null) {
			if (other.getPort() != null)
				return false;
		} else if (!port.equals(other.getPort()))
			return false;
		return true;
	}
	
	
}
