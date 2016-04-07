package de.schwibbes.tourpicker.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.UI;

import de.schwibbes.tourpicker.data.PartDAO;
import de.schwibbes.tourpicker.data.TourDAO;
import de.schwibbes.tourpicker.data.User;
import de.schwibbes.tourpicker.data.UserDAO;
import de.schwibbes.tourpicker.util.ComponentUtil;

@SpringView(name = "")
public class UserSelectionView extends AbsoluteLayout implements View {

	private static final long serialVersionUID = 1L;

	private ComponentUtil util = ComponentUtil.getInstance();

	private ListSelect userList;

	@Autowired
	public UserSelectionView(TourDAO tourDao, PartDAO partDao, UserDAO userDao) {
		setSizeFull();

		userList = createUserList(userDao);
		userList.setImmediate(true);

		userList.addValueChangeListener(e -> {
			pickedUser(userList);
		});
	}

	private ListSelect createUserList(UserDAO userDao) {
		ListSelect list = util.newList("");

		addComponent(list, "top: 50px; left: 200px");
		list.setContainerDataSource(createUserContainer(userDao));
		return list;
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
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}
}
