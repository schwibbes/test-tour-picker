package de.schwibbes.tourpicker.data;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("tour")
public interface TourDAO extends CrudRepository<Tour, Long> {

	@Override
	List<Tour> findAll();
}
