package in.dibc.kidharhaiuser.models;

import com.squareup.moshi.Json;

public class MResVerify{

	@Json(name = "msg")
	private String msg;

	@Json(name = "authkey")
	private String authkey;

	@Json(name = "clientid")
	private String clientid;

	@Json(name = "secret")
	private String secret;

	public String getMsg(){
		return msg;
	}

	public String getAuthkey(){
		return authkey;
	}

	public String getClientid(){
		return clientid;
	}

	public String getSecret() {
		return secret;
	}

	@Override
 	public String toString(){
		return 
			"MResVerify{" + 
			"msg = '" + msg + '\'' + 
			",authkey = '" + authkey + '\'' + 
			",clientid = '" + clientid + '\'' + 
			"}";
		}
}