package com.stit.common;

import java.io.Serializable;

/**
 * envelope.
 
 */
public class ApiResponse implements Serializable {
	public static enum Status {
		OK,
		ERROR
	}

	private Status status;
	private Object data;
	private Error error;

	public ApiResponse(Status status, Object data) {
		this(status, data, null);
	}

	public ApiResponse(Status status, Error error) {
		this(status, -1, error);
	}

	public ApiResponse(Status status) {
		this(status, null, null);
	}

	public ApiResponse(Status status, Object data, Error error) {
		this.status = status;
		this.data = data;
		this.error = error;
	}

	public static ApiResponse ok(Object data) {
		ApiResponse resp = new ApiResponse(Status.OK, data);
		return resp;
	}

	public static ApiResponse error(String mesg) {
		ApiResponse resp = new ApiResponse(Status.ERROR, new Error(-1, mesg));
		return resp;
	}

	public Status getStatus() {
		return status;
	}

	public Object getData() {
		return data;
	}

	public Error getError() {
		return this.error;
	}

	//--------------------------------------------------
	public static final class Error {
		private final int code;
		private final String desc;

		public Error(int code, String desc) {
			this.code = code;
			this.desc = desc;
		}

		public int getCode() {
			return this.code;
		}

		public String getDesc() {
			return this.desc;
		}
	}

}
