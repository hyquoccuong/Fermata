package com.immrhy.learningandroidapptool.fermata.addon;

import com.immrhy.learningandroidapptool.fermata.ui.fragment.ToolBarMediator;
import com.immrhy.learningandroidapptool.utils.ui.fragment.ActivityFragment;
import com.immrhy.learningandroidapptool.utils.ui.view.ToolBarView;

/**
 * @author Andrey Pavlenko
 */
public interface FermataToolAddon extends FermataActivityAddon {
	void contributeTool(ToolBarMediator m, ToolBarView tb, ActivityFragment f);
}
