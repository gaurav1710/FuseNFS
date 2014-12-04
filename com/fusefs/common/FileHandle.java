package com.fusefs.common;

import java.io.Serializable;

/**
 * A file handle
 * 
 */
public class FileHandle implements Serializable {
	private Node node;

	public FileHandle(Node node) {
		this.node = node;
		// log.debug("  " + this + " created");
	}

	public void release() {
		// log.debug("  " + this + " released");
	}

	protected void finalize() {
		// log.debug("  " + this + " finalized");
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	@Override
	public String toString() {
		return "FileHandle [node=" + node + ", toString()=" + super.toString()
				+ "]";
	}

}
