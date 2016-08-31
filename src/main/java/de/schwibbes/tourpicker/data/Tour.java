package de.schwibbes.tourpicker.data;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "TOUR")
public class Tour {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	private Part data;
	@OneToOne
	private Part tour;
	@OneToOne
	private Part feature;

	@OneToOne
	private User tester;

	private Timestamp timestamp;

	public Long getId() {
		return id;
	}

	public Part getData() {
		return data;
	}

	public Part getTour() {
		return tour;
	}

	public Part getFeature() {
		return feature;
	}

	public Tour() {
	}

	public Tour(User tester, Timestamp timestamp, Part data, Part tour, Part feature) {
		super();
		this.tester = tester;
		this.timestamp = timestamp;
		this.data = data;
		this.tour = tour;
		this.feature = feature;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public User getTester() {
		return tester;
	}

	public void setTester(User tester) {
		this.tester = tester;
	}

}
