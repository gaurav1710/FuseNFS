package com.fusefs.common;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A file in the file system tree
 * 
 */
public class File extends Node implements Serializable {
	private byte[] content;

	public File(String name, int mode, String content, String... xattrs) {
		super(name, mode, xattrs);
		this.content = content.getBytes();
	}
	public File(String name, int mode, byte[] contents, String... xattrs){
		super(name, mode, xattrs);
		this.content = contents;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

//	@Override
//	public String toString() {
//		return "File [content=" + Arrays.toString(content) + ", toString()="
//				+ super.toString() + "]";
//	}
	@Override
	public String toString() {
		return "File [toString()="
				+ super.toString() + "]";
	}

}
