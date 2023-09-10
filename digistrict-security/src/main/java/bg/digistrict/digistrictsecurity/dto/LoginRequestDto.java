package bg.digistrict.digistrictsecurity.dto;

public record LoginRequestDto (
	String email,
	String password
) {}