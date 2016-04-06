package de.schwibbes.tourpicker.data;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartDAO extends CrudRepository<Part, Long> {

	List<Part> findAll();
}
