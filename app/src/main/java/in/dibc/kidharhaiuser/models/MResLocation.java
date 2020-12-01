package in.dibc.kidharhaiuser.models;

import com.squareup.moshi.Json;

public class MResLocation{

	@Json(name = "message")
	private String message;

	public String getMessage(){
		return message;
	}

	@Override
 	public String toString(){
		return 
			"MResLocation{" + 
			"message = '" + message + '\'' + 
			"}";
		}
}