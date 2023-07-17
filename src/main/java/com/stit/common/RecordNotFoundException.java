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
public class RecordNotFoundException extends RuntimeException {

	public RecordNotFoundException() {
		super("資料不存在");
	}

	public RecordNotFoundException(String message) {
		super(message);
	}

	public RecordNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
