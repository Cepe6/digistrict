package bg.digistrict.digistrictsecurity.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "users")
public class User {
	
	@Id
	Integer id;
	
	String email;
	String password;
	Boolean active;
}
