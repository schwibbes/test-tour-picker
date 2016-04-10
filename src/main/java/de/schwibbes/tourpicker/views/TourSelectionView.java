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
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
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
	private HorizontalLayout lists;
	private TourDAO tourDao;
	private PartDAO partDao;
	private UserDAO userDao;
	private Panel successPanel;
	private Tour savedTour;

	@Autowired
	public TourSelectionView(TourDAO tourDao, PartDAO partDao, UserDAO userDao) {
		this.tourDao = tourDao;
		this.partDao = partDao;
		this.userDao = userDao;

		setMargin(true);
		setSpacing(true);

		List<Part> parts = partDao.findAll();

		List<Part> tourParts = parts.stream().filter(p -> p.getPartType() == PartType.TOUR)
				.collect(Collectors.toList());
		List<Part> featureParts = parts.stream().filter(p -> p.getPartType() == PartType.FEATURE)
				.collect(Collectors.toList());
		List<Part> dataParts = parts.stream().filter(p -> p.getPartType() == PartType.DATA)
				.collect(Collectors.toList());

		lists = new HorizontalLayout();

		util.createList(lists, "tour", tourParts);
		util.createList(lists, "feature", featureParts);
		util.createList(lists, "data", dataParts);
		addComponent(lists);
		setComponentAlignment(lists, Alignment.MIDDLE_CENTER);
	}

	private void saveTour(TourDAO tourDao, Long selectedTour, Long selectedFeature, Long selectedData) {

		Part tourPart = new Part();
		tourPart.setId(selectedTour);

		Part featurePart = new Part();
		featurePart.setId(selectedFeature);

		Part dataPart = new Part();
		dataPart.setId(selectedData);

		Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());

		savedTour = tourDao.save(new Tour(user.getBean(), now, dataPart, tourPart, featurePart));
	}

	private void showSuccess() {

		if (successPanel != null)
			removeComponent(successPanel);

		LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Berlin"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

		successPanel = new Panel("Und los!");
		successPanel.setWidth("400px");
		addComponent(successPanel);
		setComponentAlignment(successPanel, Alignment.MIDDLE_CENTER);
		setExpandRatio(successPanel, 10);

		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		h.setMargin(true);
		successPanel.setContent(h);

		h.addComponent(loadImage("ok.png"));

		VerticalLayout v = new VerticalLayout();
		h.addComponent(v);

		v.addComponent(new Label( //
				String.format("Deine Tour beginnt um: <b>%s</b>", now.format(formatter)), //
				ContentMode.HTML));
		v.addComponent(new Label( //
				String.format("Du hast Zeit bis: <b>%s</b>", now.plusHours(2).format(formatter)), //
				ContentMode.HTML));
		v.addComponent(new Label("Viel Spaß!"));

	}

	private Image loadImage(String path) {
		Image image = new Image(null, new ClassResource(path));
		image.setWidth("4em");
		return image;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void enter(ViewChangeEvent event) {
		user = (BeanItem<User>) UI.getCurrent().getSession().getAttribute("user");
		if (user == null) {
			UI.getCurrent().getNavigator().navigateTo("");
		} else {
			draw();
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

	private void draw() {
		Long selectedTour = selectRandom((ListSelect) lists.getComponent(0));
		Long selectedFeature = selectRandom((ListSelect) lists.getComponent(1));
		Long selectedData = selectRandom((ListSelect) lists.getComponent(2));

		saveTour(tourDao, selectedTour, selectedFeature, selectedData);
		showSuccess();

	}

	public void reDraw() {
		if (savedTour != null) {
			tourDao.delete(savedTour.getId());
		}
		draw();

	}
}
