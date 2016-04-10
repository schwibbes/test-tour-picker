package de.schwibbes.tourpicker;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.schwibbes.tourpicker.data.User;
import de.schwibbes.tourpicker.views.TourSelectionView;

@SpringUI
public class TourpickerUI extends UI {
	private static final long serialVersionUID = 1416197441441685916L;

	@Autowired
	private SpringViewProvider viewProvider;

	private Button restart;

	private Button redraw;

	private Button history;

	private ObjectProperty<String> headerMessageProperty;

	@Override
	protected void init(VaadinRequest vaadinRequest) {

		VerticalLayout v = new VerticalLayout();
		v.setWidth("100%");
		v.setHeight("100%");
		setContent(v);

		createHeader(v);
		Panel content = createContent(v);
		createFooter(v);

		Navigator nav = new Navigator(this, content);
		nav.addProvider(viewProvider);
		nav.addViewChangeListener(new ViewChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				@SuppressWarnings("unchecked")
				BeanItem<User> user = (BeanItem<User>) UI.getCurrent().getSession().getAttribute("user");
				switch (event.getViewName()) {
				case "":
					return true;
				case "tours":
					return user != null;
				case "history":
					return user != null;
				default:
					throw new IllegalStateException("view not found:" + event.getViewName());
				}
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
				switch (event.getViewName()) {
				case "":
					restart.setEnabled(false);
					redraw.setEnabled(false);
					history.setEnabled(true);
					headerMessageProperty.setValue("Hallo, wer bist Du?");
					break;
				case "tours":
					restart.setEnabled(true);
					redraw.setEnabled(true);
					history.setEnabled(true);
					@SuppressWarnings("unchecked")
					BeanItem<User> user = (BeanItem<User>) UI.getCurrent().getSession().getAttribute("user");
					headerMessageProperty.setValue(
							String.format("Hallo <b>%s</b>, das ist deine Tour!", user.getItemProperty("name")));
					break;
				case "history":
					restart.setEnabled(true);
					redraw.setEnabled(false);
					history.setEnabled(false);
					headerMessageProperty.setValue("Zu folgenden Touren liegen bereits Daten vor");
					break;
				default:
					throw new IllegalStateException("view not found:" + event.getViewName());
				}
			}
		});
	}

	private void createHeader(VerticalLayout v) {
		AbsoluteLayout header = new AbsoluteLayout();
		v.addComponent(header);
		v.setExpandRatio(header, 2);
		header.setSizeFull();
		headerMessageProperty = new ObjectProperty<String>("");
		Label headerMessage = new Label(headerMessageProperty, ContentMode.HTML);
		header.addComponent(headerMessage, "left: 100px; bottom: 10px");
	}

	private Panel createContent(VerticalLayout v) {
		Panel content = new Panel();
		v.addComponent(content);
		v.setExpandRatio(content, 6);
		content.setSizeFull();
		return content;
	}

	private void createFooter(VerticalLayout v) {
		HorizontalLayout footer = new HorizontalLayout();
		v.addComponent(footer);
		v.setExpandRatio(footer, 1);
		footer.setSizeFull();

		restart = new Button("Neu Starten");
		footer.addComponent(restart);
		footer.setComponentAlignment(restart, Alignment.MIDDLE_CENTER);
		restart.addClickListener(e -> {
			UI.getCurrent().getNavigator().navigateTo("");
		});

		redraw = new Button("Ziehung wiederholen");
		footer.addComponent(redraw);
		footer.setComponentAlignment(redraw, Alignment.MIDDLE_CENTER);
		redraw.addClickListener(e -> {
			View currentView = UI.getCurrent().getNavigator().getCurrentView();
			if (currentView instanceof TourSelectionView) {
				((TourSelectionView) currentView).reDraw();
			}
		});

		history = new Button("Abgeschlossene Touren");
		footer.addComponent(history);
		footer.setComponentAlignment(history, Alignment.MIDDLE_CENTER);
		history.addClickListener(e -> {
			UI.getCurrent().getNavigator().navigateTo("history");
		});
	}

}
