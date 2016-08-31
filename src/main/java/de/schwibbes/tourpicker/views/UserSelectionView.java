package de.schwibbes.tourpicker.views;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gwt.thirdparty.guava.common.base.Strings;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.schwibbes.tourpicker.data.PartDAO;
import de.schwibbes.tourpicker.data.TourDAO;
import de.schwibbes.tourpicker.data.User;
import de.schwibbes.tourpicker.data.UserDAO;
import de.schwibbes.tourpicker.util.ComponentUtil;

@SpringView(name = ViewNames.START)
public class UserSelectionView extends HorizontalLayout implements View {

	private static final long serialVersionUID = 1L;

	private ListSelect userList;
	private UserDAO userDao;
	private Label label;
	private boolean deleteMode;
	private VerticalLayout vLayout;
	private Button addButton;
	private Button removeButton;

	private Button cancel;

	@Autowired
	public UserSelectionView(TourDAO tourDao, PartDAO partDao, UserDAO userDao) {
		setSizeFull();
		deleteMode = false;
		this.userDao = userDao;

		vLayout = new VerticalLayout();
		vLayout.setSpacing(true);
		userList = createUserList();
		vLayout.addComponent(userList);
		vLayout.setComponentAlignment(userList, Alignment.MIDDLE_CENTER);

		HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.setSpacing(true);
		vLayout.addComponent(hLayout);
		vLayout.setComponentAlignment(hLayout, Alignment.MIDDLE_CENTER);

		addButton = ComponentUtil.createSmallButton(hLayout, "Nutzer hinzufügen");
		addButton.addClickListener(e -> handleAdd());

		removeButton = ComponentUtil.createSmallButton(hLayout, "Nutzer entfernen");
		removeButton.addClickListener(e -> handleRemove());

		addComponent(vLayout);
		setComponentAlignment(vLayout, Alignment.MIDDLE_CENTER);
	}

	@SuppressWarnings("serial")
	private void handleAdd() {
		TextField field = new TextField("Nutzernamen");
		vLayout.addComponent(field);
		vLayout.setComponentAlignment(field, Alignment.MIDDLE_CENTER);
		changeButtonVisibility(false);
		ShortcutListener listener = new ShortcutListener("EnterListener", ShortcutAction.KeyCode.ENTER, null) {

			@Override
			public void handleAction(Object sender, Object target) {
				if (!Strings.isNullOrEmpty(field.getValue())) {
					userDao.save(new User(field.getValue()));
					userList.setContainerDataSource(createUserContainer());
				}
				vLayout.removeComponent(field);
				changeButtonVisibility(true);
			}
		};
		field.addShortcutListener(listener);
		field.focus();
	}

	private void handleRemove() {
		label = new Label("Jetzt zu löschenden Benutzer anklicken");
		label.setWidthUndefined();
		vLayout.addComponent(label);
		vLayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
		cancel = ComponentUtil.createSmallButton(vLayout, "Abbrechen");
		cancel.addClickListener(e -> cancel());
		changeButtonVisibility(false);
		deleteMode = true;
	}

	private void cancel() {
		vLayout.removeComponent(cancel);
		vLayout.removeComponent(label);
		changeButtonVisibility(true);
		deleteMode = false;
	}

	private ListSelect createUserList() {
		ListSelect list = ComponentUtil.newList("");

		list.setContainerDataSource(createUserContainer());

		list.setImmediate(true);
		list.addValueChangeListener(e -> {
			pickedUser();
		});
		return list;
	}

	private BeanItemContainer<User> createUserContainer() {
		BeanItemContainer<User> container = new BeanItemContainer<>(User.class);
		List<User> users = userDao.findAll();
		container.addAll(users.stream().filter(user -> !user.isDeleted()).collect(Collectors.toList()));
		return container;
	}

	private void pickedUser() {
		User userChosen = (User) userList.getValue();

		if (deleteMode) {
			vLayout.removeComponent(userList);

			userChosen.setDeleted(true);
			userDao.save(userChosen);

			userList = createUserList();
			vLayout.addComponentAsFirst(userList);
			vLayout.setComponentAlignment(userList, Alignment.MIDDLE_CENTER);
			vLayout.removeComponent(label);
			vLayout.removeComponent(cancel);
			changeButtonVisibility(true);
			deleteMode = false;
		} else {
			Item item = userList.getItem(userChosen);
			UI.getCurrent().getSession().setAttribute("user", item);
			UI.getCurrent().getNavigator().navigateTo(ViewNames.SELECTION);
		}
	}

	private void changeButtonVisibility(boolean visible) {
		addButton.setVisible(visible);
		removeButton.setVisible(visible);
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}
}
