package com.intellij.flex.uiDesigner.plaf {
import cocoa.ClassFactory;
import cocoa.FrameInsets;
import cocoa.Insets;
import cocoa.TextLineInsets;
import cocoa.border.LinearGradientBorder;
import cocoa.border.RectangularBorder;
import cocoa.plaf.Placement;
import cocoa.plaf.aqua.AquaLookAndFeel;

import flash.text.engine.TextRotation;

[Abstract]
public class IdeaLookAndFeel extends AquaLookAndFeel {
  /**
   * see com.intellij.util.ui.UIUtil
   */
  public static const BORDER_COLOR:int = 0xaaaaaa;
  
  override protected function initialize():void {
    super.initialize();

    data["Sidebar"] = SidebarSkin;
    data["Sidebar.iR.tLI"] = new TextLineInsets(TextRotation.ROTATE_90, 5, 9, 9);
    data["Sidebar.iR.b.off"] = RectangularBorder.createRounded(0xf9f5f2, 0x929292, 4);
    data["Sidebar.iR.b.on"] = LinearGradientBorder.createHRounded([0xc7c6c4, 0xf5f4f4], 0, 4);

    data["Panel"] = PanelSkin;
    data["Panel.title.b"] = LinearGradientBorder.createVWithFixedHeight(16, [0xa7c5fc, 0x7d95c0]);
    data["Panel.b"] = RectangularBorder.create(0xffffff, 0x999999 /* idea UI 0x929292, but 0x999999 more Aqua UI */, new Insets(1, 1, 1, 1), new FrameInsets(0, 15 /* hide top h line */));

    data["ProjectView"] = ProjectViewSkin;
    data["ProjectView.TabView"] = EditorTabViewSkin;
    data["ProjectView.TabView.segmentedControl.iR"] = new ClassFactory(EditorTabLabelRenderer);
    data["ProjectView.TabView.segmentedControl.iR.tLI"] = new TextLineInsets(null, 15, 10, 10);
    data["ProjectView.TabView.segmentedControl.iR.b.off"] = RectangularBorder.create(NaN, NaN);
    data["ProjectView.TabView.segmentedControl.iR.b.on"] = RectangularBorder.create(NaN, NaN);
    data["ProjectView.TabView.segmentedControl.gap"] = 0;
    data["ProjectView.TabView.segmentedControl.placement"] = Placement.PAGE_START_LINE_START;

    data["Toolbar.b"] = null;

    data["Editor.Toolbar"] = data["Toolbar"];
    data["Editor.Toolbar.b"] = RectangularBorder.create(0xeeeeee);
  }
}
}