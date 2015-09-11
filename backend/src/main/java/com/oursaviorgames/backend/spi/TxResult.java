package com.oursaviorgames.backend.spi;

import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;

/**
 * A wrapper class that can embrace a generic result or some kind of
 * exception. This class captures exceptions until getResult() is called, at
 * which point it spits out the exception, otherwise returns result. Use
 * this wrapper class for the return type of objectify transaction.
 * 
 * @param <ResultType>
 *            The type of the actual return object.
 */
public class TxResult<ResultType> {

	private ResultType result;

	private Throwable exception;

	public TxResult(ResultType result) {
		this.result = result;
	}

	public TxResult(Throwable exception) {
		if (exception instanceof NotFoundException
				|| exception instanceof ForbiddenException
				|| exception instanceof ConflictException) {
			this.exception = exception;
		} else {
			throw new IllegalArgumentException("Exception not supported.");
		}
	}

	public ResultType getResult() throws NotFoundException,
			ForbiddenException, ConflictException {
		if (exception instanceof NotFoundException) {
			throw (NotFoundException) exception;
		}
		if (exception instanceof ForbiddenException) {
			throw (ForbiddenException) exception;
		}
		if (exception instanceof ConflictException) {
			throw (ConflictException) exception;
		}
		return result;
	}
}
