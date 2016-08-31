package de.schwibbes.tourpicker.data;

public enum WorkingStatus {
	GOOD("Funktionsfähig"), ADAPT("Mit Anpassungen"), BAD("Nicht Funktionsfähig"), UNKNOWN("Unbekannt");

	private String displayName;

	private WorkingStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public WorkingStatus next() {
		return WorkingStatus.values()[(this.ordinal() + 1) % WorkingStatus.values().length];
	}
}
