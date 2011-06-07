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
    // idea UI bug, so, we use our own insets, 5 instead of 4 px bottom (and 20 px height instead of 19 px) (otherwise, text bottom edge close to border bottom edge)
    data["Sidebar.iR.tLI"] = new TextLineInsets(TextRotation.ROTATE_90, 5, 9, 9);
    data["Sidebar.iR.b.off"] = RectangularBorder.createRounded(0xf9f5f2, 0x929292, 4);
    data["Sidebar.iR.b.on"] = LinearGradientBorder.createHRounded([0xc7c6c4, 0xf5f4f4], 0, 4);

    data["Panel"] = PanelSkin;
    const panelTitleBorderHeight:Number = 16;
    data["Panel.title.b"] = LinearGradientBorder.createVWithFixedHeight(panelTitleBorderHeight, [0xa7c5fc, 0x7d95c0]);
    data["Panel.b"] = RectangularBorder.create(NaN, 0x999999 /* idea UI 0x929292, but 0x999999 more Aqua UI */, new Insets(1, panelTitleBorderHeight, 1, 1), new FrameInsets(0, panelTitleBorderHeight - 1 /* hide top h line */));
    data["StyleInspector.DataGroup.b"] = RectangularBorder.create(0xffffff);

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