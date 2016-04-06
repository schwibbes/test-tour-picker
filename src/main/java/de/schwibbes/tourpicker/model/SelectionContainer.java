package de.schwibbes.tourpicker.model;

import com.vaadin.data.util.BeanContainer;

import de.schwibbes.tourpicker.data.Part;

public class SelectionContainer extends BeanContainer<String, Part> {

	private static final long serialVersionUID = 6665180310015324985L;

	public SelectionContainer() {
		super(Part.class);
		setBeanIdProperty("id");
	}

}
