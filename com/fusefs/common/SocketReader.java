package com.fusefs.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketReader implements Callable<Object> {
	private static final Log log = LogFactory.getLog(SocketReader.class);

	private Socket socket;

	public SocketReader(Socket socket) {
		this.socket = socket;
	}

	@Override
	public Object call() throws Exception {
		try {
			ObjectInputStream inputStream = new ObjectInputStream(
					socket.getInputStream());
			log.info("Streams set up..");
			// while(true){
			Object obj = inputStream.readObject();
			//inputStream.close();
			return obj;
			// inputStream.close();
		} catch (IOException e) {
			log.error("Not able to create input/output socket streams..");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
