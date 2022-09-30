package com.artifex.mupdf.fitz;

public class TryLaterException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	TryLaterException(String message) {
		super(message);
	}
}
