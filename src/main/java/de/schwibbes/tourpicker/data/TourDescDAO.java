package de.schwibbes.tourpicker.data;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("tourdesc")
public interface TourDescDAO extends CrudRepository<TourDesc, Long> {

	@Override
	List<TourDesc> findAll();
}
