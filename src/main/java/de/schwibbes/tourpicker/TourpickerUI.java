package de.schwibbes.tourpicker;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.UI;

@SpringUI
public class TourpickerUI extends UI {
	private static final long serialVersionUID = 1416197441441685916L;

	@Autowired
	private SpringViewProvider viewProvider;

	@Override
	protected void init(VaadinRequest vaadinRequest) {

		Navigator nav = new Navigator(this, this);
		nav.addProvider(viewProvider);
	}

}
