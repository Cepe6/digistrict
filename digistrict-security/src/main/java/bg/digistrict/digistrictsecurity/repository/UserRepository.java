package bg.digistrict.digistrictsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import bg.digistrict.digistrictsecurity.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	
	User getByEmail(String email);
}
