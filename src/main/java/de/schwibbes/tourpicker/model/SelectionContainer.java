package de.schwibbes.tourpicker.model;

import com.vaadin.data.util.BeanContainer;

public class SelectionContainer extends BeanContainer<String, PartViewModel> {

	private static final long serialVersionUID = 6665180310015324985L;

	public SelectionContainer() {
		super(PartViewModel.class);
		setBeanIdProperty("id");
	}

}
