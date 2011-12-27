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
  // color from wbpro, it is better than idea (because idea is more dark) (I think)
  // also, idea edtor toolbar (open any image — editor toolbar is toolbar where image size is shown) background color is 0xeeeeee, but we unified it (as wbpro looks like)
  private static const DESIGNER_AREA_OUTER_BACKGROUND_COLOR:int = 0xe9e9e9;

  override protected function initialize():void {
    super.initialize();

    // idea UI bug, so, we use our own insets, 5 instead of 4 px bottom (and 20 px height instead of 19 px) (otherwise, text bottom edge close to border bottom edge)
    data["ToolWindowManager.tabBar.rendererManager"] = new RendererManagerFactory(InteractiveBorderRendererManager, "ToolWindowManager.tabBar.item", createPanelLookAndFeel());
    data["ToolWindowManager.tabBar.item.textLineInsets"] = new TextLineInsets(TextRotation.ROTATE_90, 6, 8, 8);
    // todo selected border must be the same as idea 11
    data["ToolWindowManager.tabBar.item.b"] = new StatefulBorderImpl(new <Border>[RectangularBorder.create(NaN, 0xb4b4b4), RectangularBorder.create(0xc6c6c6, 0x777777)]);
    data["ToolWindowManager.tabBar.b"] = new ToolWindowBarBorder();
    data["ToolWindowManager.tabBar.layout"] = new ListLayoutFactory(20, 6, false);

    data["ElementTreeBar.rendererManager"] = new ElementTreeBarRMF();
    data["ElementTreeBar.interactor"] = data["SegmentedControl.interactor"];
    data["small.ElementTreeBar.layout"] = new ListLayoutFactory(17, 5);

    data["Panel"] = PanelSkin;
    const panelTitleBorderHeight:Number = Border(data["Panel.title.b"]).layoutHeight;
    data["Panel.b"] = new PanelBorder(new Insets(1, panelTitleBorderHeight, 1, 1));
    data["StyleInspector.DataGroup.b"] = RectangularBorder.create(0xffffff);

    data["ProjectView.TabView"] = EditorTabViewSkin;
    data["ProjectView.TabView.tabBar.layout"] = new ListLayoutFactory(26, 0);
    data["ProjectView.TabView.tabBar.rendererManager"] = new RendererManagerFactory(EditorTabBarRendererManager, "ProjectView.TabView.tabBar");
    data["ProjectView.TabView.tabBar.placement"] = Placement.PAGE_START_LINE_START;
    data["ProjectView.TabView.tabBar.interactor"] = data["TabView.tabBar.interactor"];

    data["Toolbar.b"] = null;

    data["Editor.Toolbar"] = data["Toolbar"];
    data["designerAreaOuterBackgroundColor"] = DESIGNER_AREA_OUTER_BACKGROUND_COLOR;
    data["Editor.Toolbar.b"] = RectangularBorder.create(DESIGNER_AREA_OUTER_BACKGROUND_COLOR);
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

class PanelBorder extends AbstractBorder {
  public function PanelBorder(contentInsets:Insets) {
    _contentInsets = contentInsets;
  }

  override public function draw(g:Graphics, w:Number, h:Number, x:Number = 0, y:Number = 0, view:View = null):void {
    g.lineStyle(1, 0x999999);
    g.moveTo(x, y);
    g.lineTo(x, y + h);
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