package com.fusefs.common;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Request to server to create file/dire, remove file/directory, synchronising
 * co ntents, modify file contents
 * 
 * @author gaurav
 * 
 */
public class Request implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RequestType requestType;
	private Node data;
	private String path;
	private String newPath;// for moves
	private byte[] byteBuffer;

	private Object fileHandle;
	private int mode;

	public Node getData() {
		return data;
	}

	public void setData(Node data) {
		this.data = data;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setByteBuffer(byte[] byteBuffer) {
		this.byteBuffer = byteBuffer;
	}

	public byte[] getByteBuffer() {
		return byteBuffer;
	}

	public Object getFileHandle() {
		return fileHandle;
	}

	public void setFileHandle(Object fileHandle) {
		this.fileHandle = fileHandle;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public String getNewPath() {
		return newPath;
	}

	public void setNewPath(String newPath) {
		this.newPath = newPath;
	}

	@Override
	public String toString() {
		return "Request [requestType=" + requestType + ", data=" + data
				+ ", path=" + path + ", newPath=" + newPath + ", byteBuffer="
				+ Arrays.toString(byteBuffer) + ", fileHandle=" + fileHandle
				+ ", mode=" + mode + "]";
	}

}
