package de.schwibbes.tourpicker.views;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinService;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.schwibbes.tourpicker.data.PartDAO;
import de.schwibbes.tourpicker.data.TourDAO;
import de.schwibbes.tourpicker.data.User;
import de.schwibbes.tourpicker.data.UserDAO;
import de.schwibbes.tourpicker.util.ComponentUtil;

@SpringView(name = "")
public class UserSelectionView extends VerticalLayout implements View {

	private static final String COOKIE_NAME = "last-chosen-tester";

	private static final long serialVersionUID = -5305231782427255393L;

	private ComponentUtil util = ComponentUtil.getInstance();

	private ListSelect userList;

	@Autowired
	public UserSelectionView(TourDAO tourDao, PartDAO partDao, UserDAO userDao) {
		setSizeFull();
		setSpacing(true);
		setMargin(true);

		userList = createUserList(userDao);
		userList.setImmediate(true);
		Button button = createPickButton(userList);

		userList.addValueChangeListener(e -> {
			button.setEnabled(userList.getValue() != null);
		});

	}

	private void preSelectUserFromCookie() {
		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
		Optional<Cookie> lastTester = Arrays.asList(cookies).stream().filter(c -> COOKIE_NAME.equals(c.getName()))
				.findFirst();
		if (lastTester.isPresent()) {
			long id = Long.parseLong(lastTester.get().getValue());
			System.out.println("found cookie with id:" + id);
			userList.select(id);
		}
	}

	private ListSelect createUserList(UserDAO userDao) {
		ListSelect list = util.newList("Wer bist Du?");
		addComponent(list);
		setComponentAlignment(list, Alignment.MIDDLE_CENTER);
		list.setContainerDataSource(createUserContainer(userDao));
		return list;
	}

	private Button createPickButton(ListSelect list) {
		Button button = new Button("Select");
		button.setWidth("300px");
		addComponent(button);
		setExpandRatio(button, 1);
		button.setEnabled(list.getValue() != null);
		setComponentAlignment(button, Alignment.TOP_CENTER);
		button.addClickListener(e -> {
			pickedUser(list);
		});
		return button;
	}

	private BeanItemContainer<User> createUserContainer(UserDAO userDao) {
		BeanItemContainer<User> container = new BeanItemContainer<>(User.class);
		container.addAll(userDao.findAll());
		return container;
	}

	private void pickedUser(ListSelect list) {
		User userChosen = (User) list.getValue();

		Item item = list.getItem(userChosen);
		UI.getCurrent().getSession().setAttribute("user", item);
		UI.getCurrent().getNavigator().navigateTo("tours");
		writeNameToCookie(userChosen.getId().toString());
	}

	private void writeNameToCookie(String user) {
		Cookie myCookie = new Cookie(COOKIE_NAME, user);

		myCookie.setMaxAge(365 * 24 * 60 * 60);
		myCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
		VaadinService.getCurrentResponse().addCookie(myCookie);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		preSelectUserFromCookie();
	}
}
