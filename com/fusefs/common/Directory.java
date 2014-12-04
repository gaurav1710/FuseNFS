package com.fusefs.common;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A Directory in filesystem tree  
 *
 */

public class Directory extends Node implements Serializable{
    private Map<String,Node> files = new LinkedHashMap<String,Node>();

	public Directory(String name, int mode, String ... xattrs)
    {
       super(name, mode, xattrs);
    }

    public void add(Node node)
    {
       files.put(node.getName(), node);
    }

    public Map<String, Node> getFiles() {
		return files;
	}

    @Override
	public String toString() {
		return "Directory [files=" + files + ", toString()=" + super.toString()
				+ "]";
	}
}
