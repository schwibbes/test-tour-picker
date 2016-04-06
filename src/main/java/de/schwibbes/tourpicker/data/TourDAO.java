package de.schwibbes.tourpicker.data;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourDAO extends CrudRepository<Tour, Long> {

	List<Tour> findAll();
}
