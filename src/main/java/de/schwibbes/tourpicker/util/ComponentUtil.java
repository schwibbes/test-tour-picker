package de.schwibbes.tourpicker.util;

import java.util.List;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ListSelect;

import de.schwibbes.tourpicker.data.Part;
import de.schwibbes.tourpicker.model.SelectionContainer;

public final class ComponentUtil {

	public static ComponentUtil getInstance() {
		return new ComponentUtil();
	}

	public ListSelect newList(String caption) {
		ListSelect list = new ListSelect(caption);
		list.setRows(10);
		list.setWidth("300px");
		list.setNullSelectionAllowed(false);
		list.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		list.setItemCaptionPropertyId("name");
		return list;
	}

	public ListSelect createList(AbstractOrderedLayout parent, String caption, List<Part> data) {
		ListSelect list = newList(caption);
		parent.addComponent(list);
		parent.setComponentAlignment(list, Alignment.MIDDLE_CENTER);

		SelectionContainer c = new SelectionContainer();
		for (Part selection : data) {
			c.addBean(selection);
		}
		list.setContainerDataSource(c);

		return list;
	}
}
