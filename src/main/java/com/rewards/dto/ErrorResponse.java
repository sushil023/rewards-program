package com.rewards.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ErrorResponse {
	/* HTTP status code. */
	private int status;

	/* Short error type label. */
	private String error;

	/* Detailed error message. */
	private String message;

	/* Timestamp when the error occurred. */
	private LocalDateTime timestamp;

	/*
	 * Constructs an ErrorResponse with all fields.
	 *
	 * @param status HTTP status code
	 * 
	 * @param error error type
	 * 
	 * @param message detailed message
	 */
	public ErrorResponse(int status, String error, String message) {
		this.status = status;
		this.error = error;
		this.message = message;
		this.timestamp = LocalDateTime.now();
	}
}
