package de.schwibbes.tourpicker.data;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("user")
public interface UserDAO extends CrudRepository<User, Long> {

	@Override
	List<User> findAll();
}
