package com.fusefs.server;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fusefs.common.FileHandle;
import com.fusefs.common.Node;
import com.fusefs.common.Request;
import com.fusefs.common.Response;
import com.fusefs.common.SocketReader;
import com.fusefs.common.SocketWriter;

import fuse.FuseException;
import fuse.FuseOpen;
import fuse.FuseOpenSetter;

public class ServerThread implements Runnable {
	private static final Log log = LogFactory.getLog(ServerThread.class);

	private Socket socket;

	private Node root;

	private FuseServerFs serverFs;

	public ServerThread(Socket socket, Node fsRoot, FuseServerFs serverFs) {
		this.socket = socket;
		this.root = fsRoot;
		this.serverFs = serverFs;
	}

	@Override
	public void run() {
		if (socket != null) {
			Request request = null;
			//while (true) {
				try {
					log.info("Going to read request from client..");
					// anticipate response from server and start reading from
					// server
					ExecutorService service = Executors
							.newSingleThreadExecutor();
					Future<Object> future = service.submit(new SocketReader(
							socket));

					request = (Request) future.get();

					log.info("Request received from client:" + request);
				} catch (InterruptedException e) {

					e.printStackTrace();
				} catch (ExecutionException e) {

					e.printStackTrace();
				}

				// send response to client
				Response response = processRequest(request);
				response.setData(root);
				Thread responseSender = new Thread(new SocketWriter(socket,
						response));
				log.info("Sending response back to the client..");
				responseSender.start();
			}
		//	}
	}

	private Response processRequest(Request request) {
		Response response = new Response();
		if (request == null) {
			response.setSuccess(false);
			return response;
		}
		switch (request.getRequestType()) {
		case SYNC:
			log.debug("Sending filesystem for SYNC command..");
			response.setData(root);
			response.setSuccess(true);
			break;
		case NEWFILE:
			try {
				log.debug("Creating new file on the server at "
						+ request.getPath());
				serverFs.mknod(request.getPath(), request.getMode(), 0);
				response.setSuccess(true);
			} catch (FuseException e) {
				response.setSuccess(false);
				e.printStackTrace();
			}
			break;
		case NEWDIR:
			try {
				log.debug("Creating new directory on the server at "
						+ request.getPath());
				serverFs.mkdir(request.getPath(), request.getMode());
				response.setSuccess(true);
			} catch (FuseException e) {
				response.setSuccess(false);
				e.printStackTrace();
			}
			break;
		case RMFILE:
			try {
				log.debug("Removing file on the server at " + request.getPath());
				serverFs.unlink(request.getPath());
				response.setSuccess(true);
			} catch (FuseException e) {
				response.setSuccess(false);
				e.printStackTrace();
			}
			break;
		case RMDIR:
			try {
				log.debug("Removing directory on the server at "
						+ request.getPath());
				serverFs.rmdir(request.getPath());
				response.setSuccess(true);
			} catch (FuseException e) {
				response.setSuccess(false);
				e.printStackTrace();
			}
			break;
		case UPDATEFILE:
			try {
				log.debug("UPDATING file on the server at "
						+ request.getPath());				
				log.info("Unlinking file at "+request.getPath());
				serverFs.unlink(request.getPath());
				log.info("Adding file at "+request.getPath());
				serverFs.addToFs(request.getPath(), request.getByteBuffer());
				response.setSuccess(true);
			} catch (FuseException e) {
				response.setSuccess(false);
				e.printStackTrace();
			}
			break;
		
		case MV:
			try {
				log.debug("Moving file/directory on the server from "
						+ request.getPath() +" to "+request.getNewPath());
				serverFs.rename(request.getPath(), request.getNewPath());
				response.setSuccess(true);
			} catch (FuseException e) {
				response.setSuccess(false);
				e.printStackTrace();
			}
			break;
			
		case CHMOD:
			try {
				log.debug("Changing permissions of file located at "
						+ request.getPath() +" to "+request.getMode());
				serverFs.chmod(request.getPath(), request.getMode());
				response.setSuccess(true);
			} catch (FuseException e) {
				response.setSuccess(false);
				e.printStackTrace();
			}
			break;
			

		}
		return response;
	}

}
