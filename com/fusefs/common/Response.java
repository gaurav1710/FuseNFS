package com.fusefs.common;

import java.io.Serializable;

/**
 * Response from server to client
 * @author gaurav
 *
 */
public class Response implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Node data;
	private String error;
	private boolean success;
	
	public Node getData() {
		return data;
	}
	public void setData(Node data) {
		this.data = data;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	@Override
	public String toString() {
		return "Response [data=" + data + ", error=" + error + ", success="
				+ success + "]";
	}
	
}
