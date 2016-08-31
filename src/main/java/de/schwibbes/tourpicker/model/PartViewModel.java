package de.schwibbes.tourpicker.model;

import de.schwibbes.tourpicker.data.Part;
import de.schwibbes.tourpicker.data.PartType;
import de.schwibbes.tourpicker.data.WorkingStatus;

public class PartViewModel {

	private Part part;
	private String description;
	private WorkingStatus workingStatus;

	public PartViewModel(Part part) {
		this.part = part;
		description = null;
		workingStatus = null;
	}

	public Long getId() {
		return this.part.getId();
	}

	public String getDisplayName() {
		if (getPartType() == PartType.TOUR)
			return getName() + ": " + (getDescription() == null ? "" : getDescription());
		if (getPartType() == PartType.DATA)
			return getName() + " [Status: " + getWorkingStatus().getDisplayName() + "]";
		return getName();
	}

	public String getName() {
		return this.part.getName();
	}

	public PartType getPartType() {
		return this.part.getPartType();
	}

	public String getDescription() {
		if (getPartType() != PartType.TOUR)
			throw new IllegalStateException("Description is only valid on PartType.TOUR parts");
		return this.description;
	}

	public void setDescription(String description) {
		if (getPartType() != PartType.TOUR)
			throw new IllegalStateException("Description is only valid on PartType.TOUR parts");
		this.description = description;
	}

	public WorkingStatus getWorkingStatus() {
		if (getPartType() != PartType.DATA)
			throw new IllegalStateException("WorkingStatus is only valid on PartType.DATA parts");
		return this.workingStatus;
	}

	public void setWorkingStatus(WorkingStatus status) {
		if (getPartType() != PartType.DATA)
			throw new IllegalStateException("WorkingStatus is only valid on PartType.DATA parts");
		this.workingStatus = status;
	}

	public Part getPart() {
		return part;
	}

}
