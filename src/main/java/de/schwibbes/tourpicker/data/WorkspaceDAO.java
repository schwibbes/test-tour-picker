package de.schwibbes.tourpicker.data;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("workspace")
public interface WorkspaceDAO extends CrudRepository<Workspace, Long> {

	@Override
	List<Workspace> findAll();
}
