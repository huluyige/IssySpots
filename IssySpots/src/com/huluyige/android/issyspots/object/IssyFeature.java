package com.huluyige.android.issyspots.object;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class IssyFeature implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2386931294358514794L;

	private int uid;
	private String name;

	public IssyFeature(int uid, String name) {
		super();
		this.uid = uid;
		this.name = name;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
