package de.schwibbes.tourpicker.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;

import de.schwibbes.tourpicker.data.Tour;
import de.schwibbes.tourpicker.data.TourDAO;

@SpringView(name = "history")
public class TourHistoryView extends VerticalLayout implements View {

	private static final long serialVersionUID = 1L;

	@Autowired
	public TourHistoryView(TourDAO tourDao) {
		Grid tours = new Grid();
		BeanItemContainer<Tour> container = new BeanItemContainer<>(Tour.class);
		container.addAll(tourDao.findAll());
		System.out.println("found tours: " + container.size());
		tours.setContainerDataSource(container);
		addComponent(tours);
	}

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
