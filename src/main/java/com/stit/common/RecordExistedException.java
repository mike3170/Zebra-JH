/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stit.common;

/**
 *
 * @author User
 */
public class RecordExistedException extends RuntimeException {

	public RecordExistedException() {
		super("資料已存在");
	}

	public RecordExistedException(String message) {
		super(message);
	}

	public RecordExistedException(String message, Throwable cause) {
		super(message, cause);
	}
}
