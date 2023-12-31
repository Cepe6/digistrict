package bg.digistrict.digistrictsecurity.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenExpiredException extends RuntimeException {
	
	public TokenExpiredException(String message) {
		super(message);
	}
}
