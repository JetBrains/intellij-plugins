package com.intellij.flex.uiDesigner.plaf {
import cocoa.Border;
import cocoa.FrameInsets;
import cocoa.Insets;
import cocoa.TextLineInsets;
import cocoa.border.LinearGradientBorder;
import cocoa.border.RectangularBorder;
import cocoa.border.StatefulBorderImpl;
import cocoa.layout.ListLayoutFactory;
import cocoa.plaf.Placement;
import cocoa.plaf.RendererManagerFactory;
import cocoa.plaf.aqua.AquaLookAndFeel;
import cocoa.renderer.InteractiveBorderRendererManager;

import flash.text.engine.TextRotation;

[Abstract]
public class IdeaLookAndFeel extends AquaLookAndFeel {
  /**
   * see com.intellij.util.ui.UIUtil
   */
  public static const BORDER_COLOR:int = 0xc0c0c0;
  
  override protected function initialize():void {
    super.initialize();

    // idea UI bug, so, we use our own insets, 5 instead of 4 px bottom (and 20 px height instead of 19 px) (otherwise, text bottom edge close to border bottom edge)
    data["ToolWindowManager.tabBar.rendererManager"] = new RendererManagerFactory(InteractiveBorderRendererManager, this, "ToolWindowManager.tabBar.item");
    data["ToolWindowManager.tabBar.item.textLineInsets"] = new TextLineInsets(TextRotation.ROTATE_90, 5, 9, 9);
    data["ToolWindowManager.tabBar.item.b"] = new StatefulBorderImpl(new <Border>[RectangularBorder.create(NaN, 0xb4b4b4), LinearGradientBorder.createHRounded([0xc7c6c4, 0xf5f4f4], 0, 4)]);
    data["ToolWindowManager.tabBar.b"] = new ToolWindowBarBorder();
    data["ToolWindowManager.tabBar.layout"] = new ListLayoutFactory(20, 6, false);

    data["ElementTreeBar.rendererManager"] = new ElementTreeBarRMF();
    data["ElementTreeBar.interactor"] = data["SegmentedControl.interactor"];
    data["ElementTreeBar.layout"] = new ListLayoutFactory(20, 5);

    data["Panel"] = PanelSkin;
    const panelTitleBorderHeight:Number = 16;
    data["Panel.title.b"] = LinearGradientBorder.createVWithFixedHeight(panelTitleBorderHeight, [0xa7c5fc, 0x7d95c0]);
    data["Panel.b"] = RectangularBorder.create(NaN, 0x999999 /* idea UI 0x929292, but 0x999999 more Aqua UI */, new Insets(1, panelTitleBorderHeight, 1, 1), new FrameInsets(0, panelTitleBorderHeight - 1 /* hide top h line */));
    data["StyleInspector.DataGroup.b"] = RectangularBorder.create(0xffffff);

    data["ProjectView.TabView"] = EditorTabViewSkin;
    data["ProjectView.TabView.tabBar.layout"] = new ListLayoutFactory(20, 0);
    data["ProjectView.TabView.tabBar.rendererManager"] = new RendererManagerFactory(EditorTabBarRendererManager, this, "ProjectView.TabView.tabBar");
    data["ProjectView.TabView.tabBar.placement"] = Placement.PAGE_START_LINE_START;
    data["ProjectView.TabView.tabBar.interactor"] = data["TabView.tabBar.interactor"];

    data["Toolbar.b"] = null;

    data["Editor.Toolbar"] = data["Toolbar"];
    data["Editor.Toolbar.b"] = RectangularBorder.create(0xeeeeee);
  }
}
}

import cocoa.ClassFactory;
import cocoa.Insets;
import cocoa.View;
import cocoa.border.AbstractBorder;
import cocoa.renderer.InteractiveTextRendererManager;

import flash.display.Graphics;

final class ElementTreeBarRMF extends ClassFactory {
  function ElementTreeBarRMF() {
     super(null);
   }

  override public function newInstance():* {
    return new ElementTreeBarRM(new Insets(2, 0, 0, 3));
  }
}

class ElementTreeBarRM extends InteractiveTextRendererManager {
  public function ElementTreeBarRM(insets:Insets) {
    super(null, insets);
  }

  override public function getItemIndexAt(x:Number, y:Number):int {
    return 0;
  }
}

final class ToolWindowBarBorder extends AbstractBorder {
  public function ToolWindowBarBorder() {
    _contentInsets = new Insets(3, 1, 2, 1);
  }

  override public function draw(g:Graphics, w:Number, h:Number, x:Number = 0, y:Number = 0, view:View = null):void {
    g.lineStyle(1, 0x999999);
    g.beginFill(0xe1e1e1);
    g.moveTo(x, y);
    g.lineTo(x, y + h);
    
    g.lineStyle();
    g.lineTo(x + w, y + h);
    g.lineTo(x + w,  y);
    g.lineTo(x, y);

    g.endFill();
  }
}