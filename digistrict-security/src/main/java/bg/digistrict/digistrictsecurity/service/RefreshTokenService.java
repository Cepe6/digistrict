package bg.digistrict.digistrictsecurity.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import bg.digistrict.digistrictsecurity.error.TokenExpiredException;
import bg.digistrict.digistrictsecurity.model.RefreshToken;
import bg.digistrict.digistrictsecurity.model.User;
import bg.digistrict.digistrictsecurity.repository.RefreshTokenRepository;
import bg.digistrict.digistrictsecurity.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	@Value("${jwt.refresh_token_validity_min}")
	private int refreshTokenValidityMin;

	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;

	public Optional<RefreshToken> findByToken(String token) {
		return refreshTokenRepository.findByToken(token);
	}

	public String createRefreshToken(String email) {
		User user = userRepository.getByEmail(email);
		Instant expireTime = Instant.now().plusMillis(TimeUnit.MINUTES.toMillis(refreshTokenValidityMin));
		String token = UUID.randomUUID().toString();
		
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUserId(user.getId());
		refreshToken.setUser(user);
		refreshToken.setToken(token);
		refreshToken.setExpireTime(LocalDateTime.ofInstant(expireTime, ZoneId.systemDefault()));
		
		return refreshTokenRepository.save(refreshToken).getToken();
	}

	public boolean verifyExpiration(Integer userId) {
		Optional<RefreshToken> refreshToken = refreshTokenRepository.findById(userId);
		if (refreshToken.map(token -> token.getExpireTime().isBefore(LocalDateTime.now())).orElse(true)) {
			refreshTokenRepository.delete(refreshToken.get());
			throw new TokenExpiredException("Refresh token has expired. Please make a new login request");
		}

		return true;
	}

	public void deleteByUserId(Integer userId) {
		Optional<User> user = userRepository.findById(userId);
		if(user.isEmpty()) {
			throw new EntityNotFoundException("No user with id %d".formatted(userId));
		}
		
		refreshTokenRepository.deleteById(user.get().getId());
	}
}
