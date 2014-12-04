package com.fusefs.server;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fuse.FuseMount;

public class Server {

	private static final Log log = LogFactory.getLog(Server.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			//parse input arguments
			//get directory on the server to be mounted at server side
			
			String fuseArgs[] = new String[args.length - 1];
		    System.arraycopy(args, 0, fuseArgs, 0, fuseArgs.length);
			
		    String dirPath = args[args.length-1];
		    //Mount the remote shared file system
			FuseMount.mount(fuseArgs,
					new FuseServerFs(dirPath), log);
		} catch (IOException e) {
			log.error("Server not able to startup."+e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Server not able to startup."+e.getMessage());
			e.printStackTrace();
		}

	}
}
