package de.schwibbes.tourpicker.data;

public enum PartType {
	DATA("Workspace"), FEATURE("Feature"), TOUR("Tour");

	private String displayName;

	private PartType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
