package com.fusefs.client;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fuse.FuseMount;

public class Client {
	private static final Log log = LogFactory.getLog(Client.class);

	/**
	 * @param args
	 */

	   public static void main(String[] args) {
			try {
				//parse input arguments
				//get server ip, port and directory on the server to be accessed
				//example - 127.0.0.1:9000/home/gaurav/Test/
				
				String[] clientArgs = args[args.length-1].split(":");
				String serverIp = clientArgs[0];
				int port = Integer.parseInt(clientArgs[1].split("/")[0]);
				
				String dirPath = "";
				//reconstruct the directory path
				for(int i=1;i<clientArgs[0].split("/").length;i++){
					dirPath += "/" + clientArgs[0].split("/")[i].trim();
				}
				//get the arguments to be passed down to fuse library
				String fuseArgs[] = new String[args.length - 1];
			    System.arraycopy(args, 0, fuseArgs, 0, fuseArgs.length);
				
			    //Mount the remote shared file system
				FuseMount.mount(fuseArgs,
						new FuseClientFs(serverIp, port, dirPath), log);
			} catch (IOException e) {
			//	log.error("FuseFs client cannot be created." + e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
			//	log.error("FuseFs client cannot be created." + e.getMessage());
				e.printStackTrace();
			}
		}

	}
