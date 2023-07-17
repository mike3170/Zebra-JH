package com.stit.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.stit.common.RecordExistedException;
import com.stit.common.RecordNotFoundException;

/**
 * global exception handler across multiple controllers
 * @author david
 */
@ControllerAdvice
public class AppWideExceptionHandler {
	private Logger logger = LogManager.getLogger();

	// Exception Handler
	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiResponse> handle(DataAccessException ex) {
		ApiResponse.Error info = new ApiResponse.Error(-1, ex.getMessage());
		ApiResponse apiResponse = new ApiResponse(ApiResponse.Status.ERROR, null, info);
		logger.error(ex);
		return new ResponseEntity<>(apiResponse, HttpStatus.EXPECTATION_FAILED);
	}

	@ExceptionHandler(RecordExistedException.class)
	public ResponseEntity<ApiResponse> handle(RecordExistedException ex) {
		ApiResponse.Error info = new ApiResponse.Error(-1, ex.getMessage());
		ApiResponse apiResponse = new ApiResponse(ApiResponse.Status.ERROR, null, info);
		logger.error(ex);
		return new ResponseEntity<>(apiResponse, HttpStatus.FOUND);
	}

	@ExceptionHandler(RecordNotFoundException.class)
	public ResponseEntity<ApiResponse> handle(RecordNotFoundException ex) {
		ApiResponse.Error info = new ApiResponse.Error(-1, ex.getMessage());
		ApiResponse apiResponse = new ApiResponse(ApiResponse.Status.ERROR, null, info);
		logger.error(ex);
		return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
	}

}
