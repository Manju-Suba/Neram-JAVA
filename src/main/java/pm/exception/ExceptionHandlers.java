package pm.exception;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import pm.response.ApiResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.IllegalFormatCodePointException;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class ExceptionHandlers {
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
		HttpStatus statusCode = HttpStatus.NOT_FOUND;

		ApiResponse message = new ApiResponse(false, ex.getMessage());
		return ResponseEntity.status(statusCode).body(message);
	}

	@ExceptionHandler(InsufficientAuthenticationException.class)
	public ResponseEntity<String> handleInsufficientAuthenticationException(InsufficientAuthenticationException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + ex.getMessage());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse> handleRequestMethodNotSupportedException(
			HttpRequestMethodNotSupportedException ex) {
		HttpStatus statusCode = HttpStatus.METHOD_NOT_ALLOWED;
		String message = ex.getMessage();

		ApiResponse response = new ApiResponse(false, message, null);
		return ResponseEntity.status(statusCode).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse> handleException(Exception ex) {
		HttpStatus statusCode = HttpStatus.UNAUTHORIZED;
		String message = ex.getMessage();

		ApiResponse response = new ApiResponse(false, message, null);
		return ResponseEntity.status(statusCode).body(response);
	}

	@ExceptionHandler(ProductNameAlreadyExistsException.class)
	public ResponseEntity<ApiResponse> handleProductNameAlreadyExistsException(ProductNameAlreadyExistsException ex) {
		ApiResponse response = new ApiResponse(false, ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiResponse> handleNotFoundException(NotFoundException ex) {
		ApiResponse response = new ApiResponse(false, "Not Found Exception",
				ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse> resourceNotFoundException(MethodArgumentNotValidException ex, WebRequest request) {
		HttpStatus statusCode = HttpStatus.BAD_REQUEST;

		ApiResponse message = new ApiResponse(false,"MethodArgumentNotValidException ", ex.getMessage());
		return ResponseEntity.status(statusCode).body(message);
	}


	// @ExceptionHandler(UnauthorizedException.class) // Replace with your specific
	// unauthorized exception type
	// public ResponseEntity<ApiResponse>
	// unauthorizedException(UnauthorizedException ex, WebRequest request) {
	// HttpStatus statusCode = HttpStatus.UNAUTHORIZED;

	// ApiResponse errorResponse = new ApiResponse(false, "Unauthorized access",
	// ex.getMessage());
	// return ResponseEntity.status(statusCode).body(errorResponse);
	// }

	// @ExceptionHandler(Exception.class)
	// public ResponseEntity<ApiResponse> globalExceptionHandler(Exception ex,
	// WebRequest request) {
	// String errormessage = ex.getMessage();
	// ApiResponse message = new ApiResponse(false, errormessage);
	// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
	// }

	// @ExceptionHandler(NullPointerException.class)
	// public ResponseEntity<ApiResponse> NullPointerException(NullPointerException
	// ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "Null Pointer Exception",
	// ex.getMessage());
	// return
	// ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	// }

	// @ExceptionHandler(IndexOutOfBoundsException.class)
	// public ResponseEntity<ApiResponse>
	// IndexOutOfBoundsException(IndexOutOfBoundsException ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "Index Out Of Bounds
	// Exception", ex.getMessage());
	// return
	// ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	// }

	// @ExceptionHandler(NumberFormatException.class)
	// public ResponseEntity<ApiResponse>
	// numberFormatException(NumberFormatException ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "NumberFormatException",
	// ex.getMessage());
	// return
	// ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorResponse);
	// }

	// @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
	// public ResponseEntity<ApiResponse>
	// arrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "Array Index Out Of Bounds
	// Exception", ex.getMessage());
	// return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	// }

	// @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	// public ResponseEntity<ApiResponse>
	// handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex)
	// {
	// ApiResponse errorResponse = new ApiResponse(false, "METHOD_NOT_ALLOWED",
	// ex.getMessage());
	// return
	// ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
	// }

	// @ExceptionHandler(IOException.class)
	// public ResponseEntity<ApiResponse> iOException(IOException ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "IOException",
	// ex.getMessage());
	// return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	// }

	// @ExceptionHandler(NoSuchMethodException.class)
	// public ResponseEntity<ApiResponse>
	// noSuchMethodException(NoSuchMethodException ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "NoSuchMethodException",
	// ex.getMessage());
	// return
	// ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
	// }

	// @ExceptionHandler(IllegalArgumentException.class)
	// public ResponseEntity<ApiResponse>
	// illegalArgumentException(IllegalArgumentException ex) {
	// ApiResponse errorResponse = new ApiResponse(false,
	// "IllegalArgumentException", ex.getMessage());
	// return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(errorResponse);
	// }

	// @ExceptionHandler(ClassNotFoundException.class)
	// public ResponseEntity<ApiResponse>
	// classNotFoundException(ClassNotFoundException ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "Class Not Found
	// Exception", ex.getMessage());
	// return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	// }

	// @ExceptionHandler(MethodNotAllowedException.class)
	// public ResponseEntity<ApiResponse>
	// methodNotAllowedException(MethodNotAllowedException ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "METHOD_NOT_ALLOWED",
	// ex.getMessage());
	// return
	// ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
	// }

	// @ExceptionHandler(InterruptedException.class)
	// public ResponseEntity<ApiResponse> interruptedException(InterruptedException
	// ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "Interrupted Exception",
	// ex.getMessage());
	// return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	// }

	// @ExceptionHandler(FileAlreadyExistsException.class)
	// public ResponseEntity<ApiResponse>
	// fileAlreadyExistsException(FileAlreadyExistsException ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "File Already Exists
	// Exception", ex.getMessage());
	// return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	// }

	// @ExceptionHandler(FileNotFoundException.class)
	// public ResponseEntity<ApiResponse>
	// fileNotFoundException(FileNotFoundException ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "FileNotFoundException",
	// ex.getMessage());
	// return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	// }

	// @ExceptionHandler(ArrayStoreException.class)
	// public ResponseEntity<ApiResponse> arrayStoreException(ArrayStoreException
	// ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "Array Store Exception",
	// ex.getMessage());
	// return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	// }

	// @ExceptionHandler(IllegalFormatCodePointException.class)
	// public ResponseEntity<ApiResponse>
	// illegalFormatCodePointException(IllegalFormatCodePointException ex) {
	// ApiResponse errorResponse = new ApiResponse(false, "Illegal Format Code Point
	// Exception", ex.getMessage());
	// return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	// }

	// @ExceptionHandler(ValidationException.class)
	// @ResponseBody
	// public ResponseEntity<ApiResponse>
	// handleValidationException(ValidationException ex) {
	// HttpStatus statusCode = HttpStatus.UNPROCESSABLE_ENTITY;
	// String message = ex.getMessage();

	// ApiResponse response = new ApiResponse(false, message, null);
	// return ResponseEntity.status(statusCode).body(response);
	// }

	// @ExceptionHandler({ DuplicateEntryException.class,
	// SQLIntegrityConstraintViolationException.class })
	// public ResponseEntity<ApiResponse> handleDuplicateEntryException(Exception
	// ex) {
	// HttpStatus statusCode = HttpStatus.CONFLICT;
	// String message = ex.getMessage();

	// ApiResponse response = new ApiResponse(false, message, null);
	// return ResponseEntity.status(statusCode).body(response);
	// }
}
