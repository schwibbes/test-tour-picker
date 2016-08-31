package de.schwibbes.tourpicker.util;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.thirdparty.guava.common.base.Strings;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ListSelect;

import de.schwibbes.tourpicker.data.Part;
import de.schwibbes.tourpicker.data.PartType;
import de.schwibbes.tourpicker.data.TourDesc;
import de.schwibbes.tourpicker.data.TourDescDAO;
import de.schwibbes.tourpicker.data.WorkingStatus;
import de.schwibbes.tourpicker.data.Workspace;
import de.schwibbes.tourpicker.data.WorkspaceDAO;
import de.schwibbes.tourpicker.model.PartViewModel;
import de.schwibbes.tourpicker.model.SelectionContainer;

public final class ComponentUtil {

	public static ListSelect newList(String caption) {
		ListSelect list = new ListSelect(caption);
		list.setRows(10);
		list.setWidth("500px");
		list.setNullSelectionAllowed(false);
		list.setItemCaptionMode(ItemCaptionMode.PROPERTY);
		list.setItemCaptionPropertyId("name");
		return list;
	}

	public static ListSelect createList(AbstractOrderedLayout parent, String caption, List<Part> data,
			WorkspaceDAO workspaceDAO, TourDescDAO tourDescDao) {
		ListSelect list = newList(caption);
		parent.setSpacing(true);
		parent.addComponent(list);
		parent.setComponentAlignment(list, Alignment.MIDDLE_CENTER);

		list.setContainerDataSource(createPartContainer(data, workspaceDAO, tourDescDao));
		list.setItemCaptionPropertyId("displayName");
		return list;
	}

	public static SelectionContainer createPartContainer(List<Part> data, WorkspaceDAO workspaceDAO,
			TourDescDAO tourDescDao) {
		SelectionContainer c = new SelectionContainer();
		List<Workspace> status = workspaceDAO.findAll();
		List<TourDesc> descs = tourDescDao.findAll();
		for (Part selection : data) {
			c.addBean(wrapPart(selection, status, descs));
		}
		return c;
	}

	private static PartViewModel wrapPart(Part selection, List<Workspace> status, List<TourDesc> descs) {
		PartViewModel model = new PartViewModel(selection);
		if (selection.getPartType() == PartType.TOUR) {
			List<TourDesc> list = descs.stream().filter(d -> selection.getName().equals(d.getName()))
					.collect(Collectors.toList());
			if (!list.isEmpty())
				model.setDescription(list.get(0).getDescription());
		} else if (selection.getPartType() == PartType.DATA) {
			List<Workspace> list = status.stream().filter(s -> selection.getName().equals(s.getName()))
					.collect(Collectors.toList());
			if (!list.isEmpty())
				model.setWorkingStatus(list.get(0).getWorkingStatus());
			else
				model.setWorkingStatus(WorkingStatus.UNKNOWN);
		}
		return model;
	}

	public static Button createButton(AbstractOrderedLayout parent, String caption, String height, String styleClass) {
		Button button = new Button(caption);
		button.setHeight(height);
		if (!Strings.isNullOrEmpty(styleClass))
			button.addStyleName(styleClass);
		parent.addComponent(button);
		parent.setComponentAlignment(button, Alignment.MIDDLE_CENTER);
		return button;
	}

	public static Button createSmallButton(AbstractOrderedLayout parent, String caption) {
		return createButton(parent, caption, "20px", "small-button");
	}
}
