package com.fusefs.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A basic node
 * Every other entity - Directory, file, filehandle and symlink inherit from this node 
 *
 */
public class Node implements Serializable{
	public static int nfiles = 0;
	private String name;
    private int mode;
    private Map<String,byte[]> xattrs = new HashMap<String,byte[]>();

    public Node(String name, int mode, String ... xattrs)
    {
       this.name = name;
       this.mode = mode;

       for (int i = 0; i < xattrs.length - 1; i += 2)
          this.xattrs.put(xattrs[i], xattrs[i + 1].getBytes());

       nfiles++;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public Map<String, byte[]> getXattrs() {
		return xattrs;
	}

	public void setXattrs(Map<String, byte[]> xattrs) {
		this.xattrs = xattrs;
	}


    @Override
	public String toString() {
		return "Node [name=" + name + ", mode=" + mode + ", xattrs=" + xattrs
				+ ", toString()=" + super.toString() + "]";
	}

}
