package bg.digistrict.digistrictsecurity.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import bg.digistrict.digistrictsecurity.dto.LoginRequestDto;
import bg.digistrict.digistrictsecurity.jwt.JwtUtils;
import bg.digistrict.digistrictsecurity.service.DigistrictUserDetailsImpl;
import bg.digistrict.digistrictsecurity.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("digistrict-security")
public class SecurityController {
	
	private final JwtUtils jwtUtils;
	private final RefreshTokenService refreshTokenService;
	private final AuthenticationManager authenticationManager;
	
	@PostMapping("login")
	public void login(@RequestBody LoginRequestDto loginRequest,
			HttpServletResponse response) {
		Authentication authentication = authenticationManager
		        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

	    SecurityContextHolder.getContext().setAuthentication(authentication);
	    List<String> roles = authentication.getAuthorities().stream()
	        .map(item -> item.getAuthority())
	        .collect(Collectors.toList());
	    
	    
	    String refreshToken = refreshTokenService.createRefreshToken(authentication.getName());
	    ResponseCookie jwtRefreshCookie = jwtUtils.generateJwtRefreshCookie(refreshToken);
	    ResponseCookie jwtAccessCookie = jwtUtils.generateJwtAccessCookie(loginRequest.email(), roles);

	    response.addCookie(new Cookie(jwtRefreshCookie.getName(), jwtRefreshCookie.getValue()));
	    response.addCookie(new Cookie(jwtAccessCookie.getName(), jwtAccessCookie.getValue()));
	}
	
	@PostMapping("logout")
	public void logout(HttpServletResponse response) {
		Object principle = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    if (principle.toString() != "anonymousUser") {      
	      Integer userId = ((DigistrictUserDetailsImpl) principle).getId();
	      refreshTokenService.deleteByUserId(userId);
	    }
	    
	    ResponseCookie jwtRefreshCookie = jwtUtils.generateCleanJwtRefreshCookie();
	    ResponseCookie jwtAccessCookie = jwtUtils.generateCleanJwtAccessCookie();

	    response.addCookie(new Cookie(jwtRefreshCookie.getName(), jwtRefreshCookie.getValue()));
	    response.addCookie(new Cookie(jwtAccessCookie.getName(), jwtAccessCookie.getValue()));
	}
	
	@PostMapping("refresh-token")
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = WebUtils.getCookie(request, "refresh_token").getValue();

		if ((refreshToken != null) && (refreshToken.length() > 0)) {			
			Object principle = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		    if (principle.toString() != "anonymousUser") {      
		    	DigistrictUserDetailsImpl userDetails = (DigistrictUserDetailsImpl) principle;
		    	
		    	if(refreshTokenService.verifyExpiration(userDetails.getId())) {
					ResponseCookie jwtRefreshCookie = jwtUtils.generateJwtAccessCookie(userDetails.getEmail(), 
							userDetails.getAuthorities().stream()
								.map(item -> item.getAuthority())
								.collect(Collectors.toList()));
					
				    response.addCookie(new Cookie(jwtRefreshCookie.getName(), jwtRefreshCookie.getValue()));
		    	}
		    }
		}
	}
}
