package org.tch.forecast.validator.login;

/*
 * Currently authentication is based on user name only.
 * 
 * this class to be used as an extension in future based on password etc.
 * */
public class UserAuthenticationInfoModel {

	public String username;

	public UserAuthenticationInfoModel(String username) {
		this.username = username;
	}
}
