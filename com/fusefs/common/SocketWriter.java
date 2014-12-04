package com.fusefs.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketWriter implements Runnable {

	private static final Log log = LogFactory.getLog(SocketWriter.class);
	private Socket socket;
	private Object requestResponse;

	public SocketWriter(Socket socket, Object requestResponse) {
		this.socket = socket;
		this.requestResponse = requestResponse;
	}

	@Override
	public void run() {
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

			outputStream.writeObject(requestResponse);
			outputStream.flush();
		} catch (IOException e) {
			log.error("Not able to create input/output socket streams..");
			e.printStackTrace();
		}

	}

}
