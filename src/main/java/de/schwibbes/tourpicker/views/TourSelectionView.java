package de.schwibbes.tourpicker.views;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.util.BeanItem;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ClassResource;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.schwibbes.tourpicker.data.Part;
import de.schwibbes.tourpicker.data.PartDAO;
import de.schwibbes.tourpicker.data.PartType;
import de.schwibbes.tourpicker.data.Tour;
import de.schwibbes.tourpicker.data.TourDAO;
import de.schwibbes.tourpicker.data.User;
import de.schwibbes.tourpicker.data.UserDAO;
import de.schwibbes.tourpicker.util.ComponentUtil;

@SpringView(name = "tours")
public class TourSelectionView extends VerticalLayout implements View {

	private static final long serialVersionUID = 5724079810773094089L;
	private ComponentUtil util = ComponentUtil.getInstance();
	private BeanItem<User> user;

	@Autowired
	public TourSelectionView(TourDAO tourDao, PartDAO partDao, UserDAO userDao) {
		setMargin(true);
		setSpacing(true);

		List<Part> parts = partDao.findAll();

		List<Part> tourParts = parts.stream().filter(p -> p.getPartType() == PartType.TOUR)
				.collect(Collectors.toList());
		List<Part> featureParts = parts.stream().filter(p -> p.getPartType() == PartType.FEATURE)
				.collect(Collectors.toList());
		List<Part> dataParts = parts.stream().filter(p -> p.getPartType() == PartType.DATA)
				.collect(Collectors.toList());

		HorizontalLayout lists = new HorizontalLayout();

		util.createList(lists, "tour", tourParts);
		util.createList(lists, "feature", featureParts);
		util.createList(lists, "data", dataParts);
		addComponent(lists);
		setComponentAlignment(lists, Alignment.MIDDLE_CENTER);

		Button pick = new Button("pick");
		pick.setWidth("300px");
		addComponent(pick);
		setComponentAlignment(pick, Alignment.MIDDLE_CENTER);
		pick.addClickListener(e -> {

			Long selectedTour = selectRandom((ListSelect) lists.getComponent(0));
			Long selectedFeature = selectRandom((ListSelect) lists.getComponent(1));
			Long selectedData = selectRandom((ListSelect) lists.getComponent(2));

			saveTour(tourDao, selectedTour, selectedFeature, selectedData);
			showSuccess();
			pick.setEnabled(false);
		});

	}

	private void saveTour(TourDAO tourDao, Long selectedTour, Long selectedFeature, Long selectedData) {

		Part tourPart = new Part();
		tourPart.setId(selectedTour);

		Part featurePart = new Part();
		featurePart.setId(selectedFeature);

		Part dataPart = new Part();
		dataPart.setId(selectedData);

		Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());

		tourDao.save(new Tour(user.getBean(), now, dataPart, tourPart, featurePart));
	}

	private void showSuccess() {
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Berlin"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

		Panel panel = new Panel("Und los!");
		panel.setWidth("400px");
		addComponent(panel);
		setComponentAlignment(panel, Alignment.TOP_CENTER);

		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		h.setMargin(true);
		panel.setContent(h);

		h.addComponent(loadImage("ok.png"));

		VerticalLayout v = new VerticalLayout();
		h.addComponent(v);

		v.addComponent(new Label("Your tour starts @" + now.format(formatter)));
		v.addComponent(new Label("you have time until: " + now.plusHours(2).format(formatter)));
		v.addComponent(new Label("Have Fun!"));

	}

	private Image loadImage(String path) {
		Image image = new Image(null, new ClassResource(path));
		image.setWidth("4em");
		return image;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		user = (BeanItem<User>) UI.getCurrent().getSession().getAttribute("user");
		if (user == null) {
			UI.getCurrent().getNavigator().navigateTo("");
		} else {
			addComponent(new Label("Starting Tour as: " + user.getItemProperty("name")), 0);
			Button button = new Button("restart");
			button.setWidth("300px");
			addComponent(button);
			setComponentAlignment(button, Alignment.TOP_CENTER);
			button.addClickListener(e -> {
				UI.getCurrent().getNavigator().navigateTo("");
			});
		}
	}

	private Long selectRandom(ListSelect list) {
		Random rnd = new Random();

		int min = 0;
		int max = list.size();
		int chosenValue = rnd.nextInt(max - min) + min;
		List<?> itemIds = (List<?>) list.getItemIds();

		Long selectedItemId = (Long) itemIds.get(chosenValue);
		list.select(selectedItemId);
		return selectedItemId;
	}
}
