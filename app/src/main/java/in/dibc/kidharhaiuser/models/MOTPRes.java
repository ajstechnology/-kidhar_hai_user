package in.dibc.kidharhaiuser.models;

import com.squareup.moshi.Json;

public class MOTPRes{

	@Json(name = "msg")
	private String msg;

	public String getMsg(){
		return msg;
	}

	@Override
 	public String toString(){
		return 
			"MOTPRes{" + 
			"msg = '" + msg + '\'' + 
			"}";
		}
}