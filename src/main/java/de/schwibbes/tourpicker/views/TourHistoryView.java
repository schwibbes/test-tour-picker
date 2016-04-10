package de.schwibbes.tourpicker.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;

import de.schwibbes.tourpicker.data.Tour;
import de.schwibbes.tourpicker.data.TourDAO;

@SpringView(name = ViewNames.HISTORY)
public class TourHistoryView extends VerticalLayout implements View {

	private static final long serialVersionUID = 1L;

	@Autowired
	public TourHistoryView(TourDAO tourDao) {

		BeanItemContainer<Tour> container = loadContainer(tourDao);
		Grid tours = createGrid(container);

		setMargin(true);
		addComponent(tours);
		setComponentAlignment(tours, Alignment.MIDDLE_CENTER);
	}

	private Grid createGrid(BeanItemContainer<Tour> container) {
		Grid tours = new Grid();
		tours.setWidth("80%");
		tours.setHeight("100%");
		tours.setContainerDataSource(container);
		tours.removeColumn("id");
		tours.setColumnOrder("timestamp", "tester", "tour", "feature", "data");
		tours.sort("timestamp", SortDirection.DESCENDING);
		return tours;
	}

	private BeanItemContainer<Tour> loadContainer(TourDAO tourDao) {
		BeanItemContainer<Tour> container = new BeanItemContainer<>(Tour.class);
		container.addAll(tourDao.findAll());
		System.out.println("found tours: " + container.size());
		return container;
	}

	@Override
	public void enter(ViewChangeEvent event) {

	}

}
