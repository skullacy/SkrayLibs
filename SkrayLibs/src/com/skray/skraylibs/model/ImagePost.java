package com.skray.skraylibs.model;

import com.skray.skraylibs.SkrayLibConst;


public class ImagePost {

	private long id;
	private String title;
	private long imageId;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getImageId() {
		return imageId;
	}

	public void setImageId(long imageId) {
		this.imageId = imageId;
	}
	
	public String getImageURL() {
		return SkrayLibConst.API_URL + "/user-image/thumb/" + this.imageId + "?w=476&h=325&t=crop";
	}

}
