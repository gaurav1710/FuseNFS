package com.fusefs.common;

import java.io.Serializable;

public class SymLink extends Node implements Serializable{
    private String link;

	public SymLink(String name, int mode, String link, String ... xattrs)
    {
       super(name, mode, xattrs);

       this.link = link;
    }
	
    public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

}
