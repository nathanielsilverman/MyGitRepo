package edu.yu.cs.com3800.stage5;

public class Response {

	private int code;
	private String body;

	public Response(int code, String body) {
		this.code = code;
		this.body = body;
	}

	public int getCode() {
		return this.code;
	}

	public String getBody() {
		return this.body;
	}

	@Override
	public String toString() {
		return "\nResponse: {\n" +
				"\tCode: " + getCode() + ",\n" +
				"\tBody: \"" + getBody() + "\"" +
				"\n}";
	}
}
