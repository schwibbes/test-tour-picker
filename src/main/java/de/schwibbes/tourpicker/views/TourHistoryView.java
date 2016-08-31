package de.schwibbes.tourpicker.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;

import de.schwibbes.tourpicker.data.Tour;
import de.schwibbes.tourpicker.data.TourDAO;
import de.schwibbes.tourpicker.util.ComponentUtil;

@SpringView(name = ViewNames.HISTORY)
public class TourHistoryView extends VerticalLayout implements View {

	private static final long serialVersionUID = 1L;
	private Grid tours;
	private Button delete;
	private TourDAO tourDao;

	@Autowired
	public TourHistoryView(TourDAO tourDao) {
		this.tourDao = tourDao;
		BeanItemContainer<Tour> container = loadContainer();
		Grid tours = createGrid(container);

		setMargin(true);
		setSpacing(true);
		addComponent(tours);
		setComponentAlignment(tours, Alignment.MIDDLE_CENTER);
	}

	private Grid createGrid(BeanItemContainer<Tour> container) {
		tours = new Grid();
		tours.setWidth("80%");
		tours.setHeight("100%");
		tours.setContainerDataSource(container);
		tours.removeColumn("id");
		tours.setColumnOrder("timestamp", "tester", "tour", "feature", "data");
		tours.sort("timestamp", SortDirection.DESCENDING);
		tours.addSelectionListener(e -> selectionChanged());
		return tours;
	}

	private void selectionChanged() {
		if (tours.getSelectedRow() != null) {
			if (delete != null) {
				removeComponent(delete);
			}
			delete = ComponentUtil.createSmallButton(this, "Tour löschen");
			delete.addClickListener(e -> deleteTour());
		}
	}

	private void deleteTour() {
		tourDao.delete((Tour) tours.getSelectedRow());
		tours.setContainerDataSource(loadContainer());
		removeComponent(delete);
	}

	private BeanItemContainer<Tour> loadContainer() {
		BeanItemContainer<Tour> container = new BeanItemContainer<>(Tour.class);
		container.addAll(tourDao.findAll());
		System.out.println("found tours: " + container.size());
		return container;
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}

}
