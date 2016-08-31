package de.schwibbes.tourpicker.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gwt.thirdparty.guava.common.base.Strings;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ClassResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.schwibbes.tourpicker.data.Part;
import de.schwibbes.tourpicker.data.PartDAO;
import de.schwibbes.tourpicker.data.PartType;
import de.schwibbes.tourpicker.data.Tour;
import de.schwibbes.tourpicker.data.TourDAO;
import de.schwibbes.tourpicker.data.TourDesc;
import de.schwibbes.tourpicker.data.TourDescDAO;
import de.schwibbes.tourpicker.data.User;
import de.schwibbes.tourpicker.data.UserDAO;
import de.schwibbes.tourpicker.data.WorkingStatus;
import de.schwibbes.tourpicker.data.Workspace;
import de.schwibbes.tourpicker.data.WorkspaceDAO;
import de.schwibbes.tourpicker.util.ComponentUtil;

@SpringView(name = ViewNames.SELECTION)
public class TourSelectionView extends VerticalLayout implements View {

	private static final String WS_REPO = "http://cas-source-01.home.cas.de/svn/MerlinProjekte/workspaces/";
	private static final long serialVersionUID = 5724079810773094089L;

	private BeanItem<User> user;
	private HorizontalLayout lists;
	private TourDAO tourDao;
	private PartDAO partDao;
	private WorkspaceDAO workspaceDao;
	private TourDescDAO tourDescDao;
	private Panel successPanel;
	private Tour savedTour;
	private Button start;
	private ListSelect tourList;
	private ListSelect featureList;
	private ListSelect dataList;

	@Autowired
	public TourSelectionView(TourDAO tourDao, PartDAO partDao, UserDAO userDao, WorkspaceDAO workspaceDAO,
			TourDescDAO tourDescDao) {
		this.tourDao = tourDao;
		this.partDao = partDao;
		this.workspaceDao = workspaceDAO;
		this.tourDescDao = tourDescDao;
		setUp();
	}

	private void setUp() {
		setMargin(true);
		setSpacing(true);

		List<Part> parts = partDao.findAll();

		List<Part> tourParts = parts.stream().filter(p -> p.getPartType() == PartType.TOUR && !p.isDeleted())
				.collect(Collectors.toList());
		List<Part> featureParts = parts.stream().filter(p -> p.getPartType() == PartType.FEATURE && !p.isDeleted())
				.collect(Collectors.toList());
		List<Part> dataParts = prepareDataList(parts);

		lists = new HorizontalLayout();
		lists.setSpacing(true);

		VerticalLayout tourLayout = new VerticalLayout();
		tourList = ComponentUtil.createList(tourLayout, PartType.TOUR.getDisplayName(), tourParts, workspaceDao,
				tourDescDao);

		VerticalLayout featureLayout = new VerticalLayout();
		featureList = ComponentUtil.createList(featureLayout, PartType.FEATURE.getDisplayName(), featureParts,
				workspaceDao, tourDescDao);

		VerticalLayout dataLayout = new VerticalLayout();
		dataList = ComponentUtil.createList(dataLayout, PartType.DATA.getDisplayName(), dataParts, workspaceDao,
				tourDescDao);

		lists.addComponent(tourLayout);
		lists.setComponentAlignment(tourLayout, Alignment.MIDDLE_CENTER);
		createButtons(PartType.TOUR, tourLayout, tourList);
		lists.addComponent(featureLayout);
		lists.setComponentAlignment(featureLayout, Alignment.MIDDLE_CENTER);
		createButtons(PartType.FEATURE, featureLayout, featureList);
		lists.addComponent(dataLayout);
		lists.setComponentAlignment(dataLayout, Alignment.MIDDLE_CENTER);
		createButtons(PartType.DATA, dataLayout, dataList);

		addComponent(lists);
		setComponentAlignment(lists, Alignment.MIDDLE_CENTER);
	}

	private List<Part> prepareDataList(List<Part> parts) {
		List<Part> dataParts = parts.stream().filter(p -> p.getPartType() == PartType.DATA && !p.isDeleted())
				.collect(Collectors.toList());
		List<String> workspaces = findAllWorkspaces();
		if (workspaces.isEmpty()) {
			// Reading workspaces failed. Use our own db without updating
			// anything
			return dataParts;
		}

		// delete all parts not contained in workspace list
		List<Part> toSave = new ArrayList<>();
		dataParts.stream().filter(p -> !workspaces.contains(p.getName())).forEach(p -> {
			p.setDeleted(true);
			toSave.add(p);
		});

		// add all workspaces not contained in partDao
		workspaces.stream().map(s -> new Part(s, PartType.DATA)).filter(p -> !dataParts.contains(p)).forEach(p -> {
			toSave.add(p);
		});
		partDao.save(toSave);

		// refresh data list
		parts = partDao.findAll();
		List<Part> result = parts.stream().filter(p -> p.getPartType() == PartType.DATA && !p.isDeleted())
				.collect(Collectors.toList());
		return result;
	}

	private List<String> findAllWorkspaces() {
		List<String> workspaces = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new URL(WS_REPO).openStream(), StandardCharsets.UTF_8))) {
			String workspaceString = "";
			while (reader.ready()) {
				workspaceString += reader.readLine();
			}
			// find all <li> items
			Pattern liPattern = Pattern.compile("<li>(.*?)</li>");
			Matcher liMatcher = liPattern.matcher(workspaceString);
			while (liMatcher.find()) {
				// extract name from <li> items
				Pattern namePattern = Pattern.compile(">(.*?)/</a>");
				Matcher nameMatcher = namePattern.matcher(liMatcher.group(1));
				if (nameMatcher.find()) {
					workspaces.add(nameMatcher.group(1));
				}
			}
		} catch (IOException e) {
			return new ArrayList<>();
		}
		return workspaces;
	}

	private void createButtons(PartType type, AbstractOrderedLayout layout, ListSelect list) {
		HorizontalLayout hlayout = new HorizontalLayout();
		hlayout.setSpacing(true);
		layout.addComponent(hlayout);
		layout.setComponentAlignment(hlayout, Alignment.MIDDLE_CENTER);
		if (type != PartType.DATA) {
			Button addButton = ComponentUtil.createSmallButton(hlayout, "Hinzufügen");
			addButton.addClickListener(e -> addButtonClicked(type));

			Button removeButton = ComponentUtil.createSmallButton(hlayout, "Entfernen");
			removeButton.addClickListener(e -> removeButtonClicked(type, list));
		} else {
			Button changeStatus = ComponentUtil.createSmallButton(hlayout, "Status wechseln");
			changeStatus.addClickListener(e -> changeStatus(partDao.findOne((Long) list.getValue())));
		}
	}

	private void changeStatus(Part part) {
		List<Workspace> all = workspaceDao.findAll();
		List<Workspace> ws = all.stream().filter(p -> part.getName().equals(p.getName())).collect(Collectors.toList());
		if (!ws.isEmpty()) {
			Workspace workspace = ws.get(0);
			workspace.setWorkingStatus(workspace.getWorkingStatus().next());
			workspaceDao.save(workspace);
		} else {
			workspaceDao.save(new Workspace(part.getName(), WorkingStatus.GOOD));
		}
		// refresh dataList without losing selection
		Object value = dataList.getValue();
		List<Part> parts = partDao.findAll();
		List<Part> data = parts.stream().filter(p -> p.getPartType() == PartType.DATA && !p.isDeleted())
				.collect(Collectors.toList());
		dataList.setContainerDataSource(ComponentUtil.createPartContainer(data, workspaceDao, tourDescDao));
		dataList.setValue(value);
	}

	private void removeButtonClicked(PartType type, ListSelect list) {
		Label label = new Label("Jetzt zu löschende/s " + type.getDisplayName() + " anklicken");
		label.setWidthUndefined();
		addComponent(label);
		setComponentAlignment(label, Alignment.MIDDLE_CENTER);
		changeButtonVisibility(false);
		Button cancel = new Button("Abbrechen");
		cancel.addClickListener(e -> {
			removeComponent(label);
			removeComponent(cancel);
			changeButtonVisibility(true);
		});
		addComponent(cancel);
		setComponentAlignment(cancel, Alignment.MIDDLE_CENTER);
		list.addValueChangeListener(e -> {
			Part value = partDao.findOne((Long) list.getValue());
			value.setDeleted(true);
			partDao.save(value);
			updateAll();
		});
	}

	@SuppressWarnings("serial")
	private void addButtonClicked(PartType type) {
		changeButtonVisibility(false);
		if (type == PartType.TOUR) {
			TextField field = new TextField("Name:Beschreibung");
			addComponent(field);
			setComponentAlignment(field, Alignment.MIDDLE_CENTER);
			field.addShortcutListener(new ShortcutListener("EnterListener", ShortcutAction.KeyCode.ENTER, null) {

				@Override
				public void handleAction(Object sender, Object target) {
					if (!Strings.isNullOrEmpty(field.getValue()) && field.getValue().contains(":")) {
						String[] split = field.getValue().split(":");
						partDao.save(new Part(split[0], type));
						tourDescDao.save(new TourDesc(split[0], split[1]));
						updateAll();
					}
					removeComponent(field);
					changeButtonVisibility(true);
				}
			});
			field.focus();
		} else {
			TextField field = new TextField("Name");
			addComponent(field);
			setComponentAlignment(field, Alignment.MIDDLE_CENTER);
			field.addShortcutListener(new ShortcutListener("EnterListener", ShortcutAction.KeyCode.ENTER, null) {

				@Override
				public void handleAction(Object sender, Object target) {
					if (!Strings.isNullOrEmpty(field.getValue())) {
						partDao.save(new Part(field.getValue(), type));
						updateAll();
					}
					removeComponent(field);
					changeButtonVisibility(true);
				}
			});
			field.focus();
		}
	}

	private void updateAll() {
		removeAllComponents();
		setUp();
		start = null;
		reDraw();
	}

	private void changeButtonVisibility(boolean visible) {
		for (int i = 0; i < lists.getComponentCount(); i++) {
			if (lists.getComponent(i) instanceof VerticalLayout) {
				VerticalLayout layout = (VerticalLayout) lists.getComponent(i);
				for (int j = 0; j < layout.getComponentCount(); j++) {
					if (layout.getComponent(j) instanceof HorizontalLayout) {
						layout.getComponent(j).setVisible(visible);
					}
				}
			}
		}
		start.setVisible(visible);
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
			UI.getCurrent().getNavigator().navigateTo(ViewNames.START);
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
		Long selectedTour = selectRandom(tourList);
		Long selectedFeature = selectRandom(featureList);
		Long selectedData = selectRandom(dataList);

		showStart(selectedTour, selectedFeature, selectedData);
	}

	private void showStart(Long selectedTour, Long selectedFeature, Long selectedData) {
		if (start != null)
			start.setVisible(true);
		else {
			start = new Button("Tour starten");
			HorizontalLayout hLayout = new HorizontalLayout();
			hLayout.setMargin(true);
			hLayout.setSpacing(true);
			hLayout.addComponent(start);
			hLayout.setComponentAlignment(start, Alignment.MIDDLE_CENTER);

			VerticalLayout vLayout = new VerticalLayout();
			vLayout.setSpacing(true);
			vLayout.addComponent(new Label(tourList.getItemCaption(tourList.getValue())));
			vLayout.addComponent(new Label(featureList.getItemCaption(featureList.getValue())));
			vLayout.addComponent(new Label(dataList.getItemCaption(dataList.getValue())));

			hLayout.addComponent(vLayout);
			hLayout.setComponentAlignment(vLayout, Alignment.MIDDLE_LEFT);

			addComponent(hLayout);
			setComponentAlignment(hLayout, Alignment.MIDDLE_CENTER);
			start.addClickListener(e -> startTour(selectedTour, selectedFeature, selectedData));
		}
	}

	private void startTour(Long selectedTour, Long selectedFeature, Long selectedData) {
		saveTour(tourDao, selectedTour, selectedFeature, selectedData);
		start.setVisible(false);
		changeButtonVisibility(false);
		showSuccess();
	}

	public void reDraw() {
		if (successPanel != null) {
			removeComponent(successPanel);
		}
		draw();
		changeButtonVisibility(true);

	}
}
