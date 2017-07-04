package com.byodl.model.api;

import com.google.gson.annotations.SerializedName;

public class VersionResponse{

	@SerializedName("timestamp")
	private String timestamp;

	public String getTimestamp(){
		return timestamp;
	}
}