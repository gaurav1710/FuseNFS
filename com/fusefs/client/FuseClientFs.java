package com.fusefs.client;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fusefs.common.Directory;
import com.fusefs.common.FileHandle;
import com.fusefs.common.Node;
import com.fusefs.common.Request;
import com.fusefs.common.RequestType;
import com.fusefs.common.Response;
import com.fusefs.common.SocketReader;
import com.fusefs.common.SocketWriter;
import com.fusefs.common.SymLink;

import fuse.Errno;
import fuse.Filesystem3;
import fuse.FuseDirFiller;
import fuse.FuseException;
import fuse.FuseFtypeConstants;
import fuse.FuseGetattrSetter;
import fuse.FuseOpenSetter;
import fuse.FuseSizeSetter;
import fuse.FuseStatConstants;
import fuse.FuseStatfsSetter;
import fuse.XattrLister;

public class FuseClientFs implements Filesystem3 {
	private static Log log = LogFactory.getLog(FuseClientFs.class);
	private Socket socket;
	private String serverIp = "127.0.0.1";// default localhost
	private int port = 9000;
	private String initDirPath;
	private static final int BLOCK_SIZE = 1024;
	private static final int NAME_LENGTH = 1024;
	// a root directory
	private Directory root;

	private int defaultMode;

	public FuseClientFs(String serverIp, int port, String dirPath) {
		this.initDirPath = dirPath;
		this.serverIp = serverIp;
		this.port = port;
		startClient();
	}

	private void refreshFs() {
		try {
			
			Request request = new Request();
			request.setRequestType(RequestType.SYNC);
			Response response;
			response = sendRequest(request);
			Object data = response.getData();
			if (data instanceof Directory) {
				// log.info(((Directory) data).files);
				initFs((Directory) data);
			} else {
				log.error("Incorrect data received from the server..");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private void refreshFs2() {
	
		try {
			Request request = new Request();
			request.setRequestType(RequestType.SYNC);
			Response response;
			response = sendRequest(request);
			Object data = response.getData();
			if (data instanceof Directory) {
				// log.info(((Directory) data).files);
				initFs((Directory) data);
			} else {
				log.error("Incorrect data received from the server..");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void startClient() {
		log.info("Attempting to connect to server:" + serverIp + " at port "
				+ port);
		// socket = new Socket(serverIp, port);
		log.info("Connection established..");
		refreshFs2();

	}

	private Response sendRequest(Request request) throws InterruptedException,
			ExecutionException, UnknownHostException, IOException {
		log.info("Sending request to server:" + request);
		Socket socket = new Socket(serverIp, port);
		Thread requestSender = new Thread(new SocketWriter(socket, request));
		// send the request to the server
		requestSender.start();

		// wait for thread to complete before reading response from the server
		requestSender.join();
		log.info("Going to read response from server..");
		// anticipate response from server and start reading from server
		ExecutorService service = Executors.newSingleThreadExecutor();
		Future<Object> future = service.submit(new SocketReader(socket));
		Response response = (Response) future.get();
		
		return response;
	}

	private void initFs(Directory directory) {
		log.info("Adding files and subdirectories from server..");
		root = directory;
	}

	// lookup node
	private Node lookup(String path) {
		log.info("Looking up file at " + getAbsolutePath(path));
		if (path.equals("/"))
			return root;

		File f = new File(path);
		Node parent = lookup(f.getParent());
		Node node = (parent instanceof Directory) ? ((Directory) parent)
				.getFiles().get(f.getName()) : null;

		if (log.isDebugEnabled())
			log.debug("  lookup(\"" + path + "\") returning: " + node);

		return node;
	}

	public int chmod(String path, int mode) throws FuseException {
		log.info("Changing mode for file " + getAbsolutePath(path));
		Node node = lookup(path);

		if (node != null) {
			node.setMode((node.getMode() & FuseStatConstants.TYPE_MASK)
					| (mode & FuseStatConstants.MODE_MASK));
			Request request = new Request();
			request.setRequestType(RequestType.CHMOD);
			request.setPath(path);
			request.setMode(mode);
			try {
				Response response = sendRequest(request);
				log.info("Response received from server :" + response);
				//refresh filesystem - maintain somewhat strong consistency
				refreshFs();

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return 0;
		}

		return Errno.ENOENT;
	}

	public int chown(String path, int uid, int gid) throws FuseException {
		log.info("Chown not supported.");
		return 0;
	}

	// Commands using this method - ls
	public int getattr(String path, FuseGetattrSetter getattrSetter)
			throws FuseException {
		log.info("Getting attributes for the file:" + getAbsolutePath(path));
		Node node = lookup(path);

		int time = (int) (System.currentTimeMillis() / 1000L);
		if (node instanceof Directory) {
			Directory dir = (Directory) node;
			getattrSetter.set(dir.hashCode(),
					FuseFtypeConstants.TYPE_DIR | dir.getMode(), 1, 0, 0, 0,
					dir.getFiles().size() * NAME_LENGTH, (dir.getFiles().size()
							* NAME_LENGTH + BLOCK_SIZE - 1)
							/ BLOCK_SIZE, time, time, time);

			return 0;
		} else if (node instanceof com.fusefs.common.File) {
			com.fusefs.common.File f = (com.fusefs.common.File) node;
			getattrSetter.set(f.hashCode(),
					FuseFtypeConstants.TYPE_FILE | f.getMode(), 1, 0, 0, 0,
					f.getContent().length,
					(f.getContent().length + BLOCK_SIZE - 1) / BLOCK_SIZE,
					time, time, time);

			return 0;
		} else if (node instanceof SymLink) {
			SymLink l = (SymLink) node;
			getattrSetter.set(l.hashCode(),
					FuseFtypeConstants.TYPE_SYMLINK | l.getMode(), 1, 0, 0, 0,
					l.getLink().length(),
					(l.getLink().length() + BLOCK_SIZE - 1) / BLOCK_SIZE, time,
					time, time);

			return 0;
		}
		// File not found
		log.error("File is not found at the location " + path);
		return Errno.ENOENT;
	}

	// return list of files in directory, suply to getattr method
	public int getdir(String path, FuseDirFiller filler) throws FuseException {
		log.info("Getting directory at " + getAbsolutePath(path));
		Node n = lookup(path);

		if (n instanceof Directory) {
			for (Node child : ((Directory) n).getFiles().values()) {
				int ftype = (child instanceof Directory) ? FuseFtypeConstants.TYPE_DIR
						: ((child instanceof com.fusefs.common.File) ? FuseFtypeConstants.TYPE_FILE
								: ((child instanceof SymLink) ? FuseFtypeConstants.TYPE_SYMLINK
										: 0));
				if (ftype > 0)
					filler.add(child.getName(), child.hashCode(),
							ftype | child.getMode());
			}

			return 0;
		}
		log.error("Not a directory " + path);
		return Errno.ENOTDIR;
	}

	public int link(String from, String to) throws FuseException {
		return Errno.EROFS;
	}

	// Commands using this method - mkdir - creating a new directory
	public int mkdir(String path, int mode) throws FuseException {
		log.info("Making a new directory in filesystem at "
				+ getAbsolutePath(path));
		Directory parentDir = (Directory) lookupDir(path);
		parentDir.add(createNewDirectory(new File(path).getName(), mode));
		Request request = new Request();
		request.setRequestType(RequestType.NEWDIR);
		request.setPath(path);
		request.setMode(mode);
		try {
			Response response = sendRequest(request);
			log.info("Response received from server :" + response);
			//refresh filesystem - maintain somewhat strong consistency
			refreshFs();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	private Node lookupDir(String path) {
		File f = new File(path);
		Node parentDir = lookup(f.getParent());
		return parentDir;
	}

	// Commands using this method - touch command - creating a new file
	public int mknod(String path, int mode, int rdev) throws FuseException {
		log.info("Making a new node in filesystem at " + getAbsolutePath(path));
		Directory parentDir = (Directory) lookupDir(path);
		parentDir.add(createNewEmptyFile(new File(path).getName(), mode));
		Request request = new Request();
		request.setRequestType(RequestType.NEWFILE);
		request.setPath(path);
		request.setMode(mode);
		try {
			if (!path.contains(".swp")) {//ignore vi swap temporary files
				Response response = sendRequest(request);
				log.info("Response received from server :" + response);
				//refresh filesystem - maintain somewhat strong consistency
				refreshFs();

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	private com.fusefs.common.File createNewEmptyFile(String name, int mode) {
		return new com.fusefs.common.File(name, mode, "");
	}

	private Directory createNewDirectory(String name, int mode) {
		return new Directory(name, mode, "");
	}

	// Commands using this method - mv
	public int rename(String from, String to) throws FuseException {
		Directory fromDir = (Directory) lookupDir(from);
		Directory toDir = (Directory) lookupDir(to);
		Node nodeToMove = fromDir.getFiles().get(new File(from).getName());
		fromDir.getFiles().remove(new File(from).getName());

		// add the node to the new parent directory
		nodeToMove.setName(new File(to).getName());
		toDir.getFiles().put(new File(to).getName(), nodeToMove);


		Request request = new Request();
		request.setRequestType(RequestType.MV);
		request.setPath(from);
		request.setNewPath(to);
		try {
			Response response = sendRequest(request);
			//refresh filesystem - maintain somewhat strong consistency
			refreshFs();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
		// return Errno.EROFS;
	}

	// Commands using this method - rmdir
	public int rmdir(String path) throws FuseException {
		log.info("Removing file from " + getAbsolutePath(path));
		return unlink(path);
	}

	public int statfs(FuseStatfsSetter statfsSetter) throws FuseException {
		statfsSetter.set(BLOCK_SIZE, 1000, 200, 180, Node.nfiles, 0,
				NAME_LENGTH);

		return 0;
	}

	public int symlink(String from, String to) throws FuseException {
		return Errno.EROFS;
	}

	// Commands using this method - cp
	public int truncate(String path, long size) throws FuseException {
		log.info("Truncating file at " + path);
		Node node = lookup(path);
		if (node instanceof Directory) {
			return Errno.EISDIR;
		}
		((com.fusefs.common.File) node).setContent(new byte[0]);
		return 0;
		// return Errno.EROFS;
	}

	// Commands using this method - rm - removing a file
	public int unlink(String path) throws FuseException {
		Directory parentDir = (Directory) lookupDir(path);
		// remove the file from file collection in the parent directory..
		Node node = parentDir.getFiles().get(new File(path).getName());
		parentDir.getFiles().remove(new File(path).getName());
		Request request = new Request();
		if (node instanceof com.fusefs.common.File)
			request.setRequestType(RequestType.RMFILE);
		else
			request.setRequestType(RequestType.RMDIR);
		request.setPath(path);
		try {
			Response response = sendRequest(request);
			log.info("Response received from server :" + response);
			//refresh filesystem - maintain somewhat strong consistency
			refreshFs();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public int utime(String path, int atime, int mtime) throws FuseException {
		return 0;
	}

	public int readlink(String path, CharBuffer link) throws FuseException {
		log.info("Reading link from " + path);
		Node n = lookup(path);

		if (n instanceof SymLink) {
			link.append(((SymLink) n).getLink());
			return 0;
		}

		return Errno.ENOENT;
	}

	public int open(String path, int flags, FuseOpenSetter openSetter)
			throws FuseException {
		refreshFs();
		log.info("Opening file at " + getAbsolutePath(path));
		Node n = lookup(path);

		if (n != null) {
			openSetter.setFh(new FileHandle(n));
			return 0;
		}

		return Errno.ENOENT;
	}

	// Commands using this method - cp, cat
	public int write(String path, Object fh, boolean isWritepage,
			ByteBuffer buf, long offset) throws FuseException {
		log.info("Creating/Updating file at " + getAbsolutePath(path));
		// check if the file already exists or needed to be created..
		Node file = lookup(path);
		if (file == null) {
			// create new file
			mknod(path, defaultMode, 0);
			file = lookup(path);
		}
		if (file instanceof Directory) {
			return Errno.EISDIR;
		}
		// get current contents pointer
		byte[] currentContents = ((com.fusefs.common.File) file).getContent();
		List<Byte> bytesList = new ArrayList<Byte>();
		while (buf.hasRemaining()) {
			bytesList.add(buf.get());
		}
		byte[] bytesToBeAppended = new byte[bytesList.size()];
		// bytesList.toArray(bytesToBeAppended);
		int i = 0;
		for (Byte nextByte : bytesList) {
			bytesToBeAppended[i] = nextByte;
			i++;
		}
		byte[] newContents = new byte[currentContents.length
				+ bytesToBeAppended.length];
		System.arraycopy(currentContents, 0, newContents, 0,
				currentContents.length);
		System.arraycopy(bytesToBeAppended, 0, newContents,
				currentContents.length, bytesToBeAppended.length);
		((com.fusefs.common.File) file).setContent(newContents);

		// Inform the server about this - echo
		Request request = new Request();
		request.setRequestType(RequestType.UPDATEFILE);
		request.setPath(path);
		request.setByteBuffer(newContents);
		try {
			Response response = sendRequest(request);
			log.info("Response received from server :" + response);
			//refresh filesystem - maintain somewhat strong consistency
			refreshFs();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;

		// return Errno.EROFS;
	}

	// Commands using this method - cp , cat, less
	public int read(String path, Object fh, ByteBuffer buf, long offset)
			throws FuseException {
		log.info("Reading file at " + getAbsolutePath(path));
		if (fh instanceof FileHandle) {
			com.fusefs.common.File f = (com.fusefs.common.File) ((FileHandle) fh)
					.getNode();
			buf.put(f.getContent(),
					(int) offset,
					Math.min(buf.remaining(), f.getContent().length
							- (int) offset));

			return 0;
		}

		return Errno.EBADF;
	}

	public int flush(String path, Object fh) throws FuseException {
		if (fh instanceof FileHandle){
			Request request = new Request();
			request.setRequestType(RequestType.FLUSH);
			request.setPath(path);
			try {
				Response response = sendRequest(request);
				log.info("Response received from server :" + response);
				//refresh filesystem - maintain somewhat strong consistency
				refreshFs();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return 0;
		}

		return Errno.EBADF;
	}

	public int fsync(String path, Object fh, boolean isDatasync)
			throws FuseException {
		if (fh instanceof FileHandle)
			return 0;

		return Errno.EBADF;
	}

	public int release(String path, Object fh, int flags) throws FuseException {
		if (fh instanceof FileHandle) {
			((FileHandle) fh).release();
			System.runFinalization();
			Request request = new Request();
			request.setRequestType(RequestType.RELEASE);
			request.setPath(path);
			try {
				Response response = sendRequest(request);
				log.info("Response received from server :" + response);
				//refresh filesystem - maintain somewhat strong consistency
				refreshFs();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return 0;
		}

		return Errno.EBADF;
	}

	public int getxattr(String path, String name, ByteBuffer dst)
			throws FuseException, BufferOverflowException {
		Node n = lookup(path);

		if (n == null)
			return Errno.ENOENT;

		byte[] value = n.getXattrs().get(name);

		if (value == null)
			return Errno.ENOATTR;

		dst.put(value);

		return 0;
	}

	public int getxattrsize(String path, String name, FuseSizeSetter sizeSetter)
			throws FuseException {
		Node n = lookup(path);

		if (n == null)
			return Errno.ENOENT;

		byte[] value = n.getXattrs().get(name);

		if (value == null)
			return Errno.ENOATTR;

		sizeSetter.setSize(value.length);

		return 0;
	}

	public int listxattr(String path, XattrLister lister) throws FuseException {
		Node n = lookup(path);

		if (n == null)
			return Errno.ENOENT;

		for (String xattrName : n.getXattrs().keySet())
			lister.add(xattrName);

		return 0;
	}

	public int removexattr(String path, String name) throws FuseException {
		return Errno.EROFS;
	}

	public int setxattr(String path, String name, ByteBuffer value, int flags)
			throws FuseException {
		return Errno.EROFS;
	}

	private String getAbsolutePath(String relPath) {
		return initDirPath + relPath;
	}

}