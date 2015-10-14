package com.intellij.flex.uiDesigner.ui.inspectors.styleInspector {
import cocoa.ListView;
import cocoa.ListViewDataSourceImpl;
import cocoa.renderer.RendererManager;

import com.intellij.flex.uiDesigner.Document;
import com.intellij.flex.uiDesigner.Module;
import com.intellij.flex.uiDesigner.css.CssDeclaration;
import com.intellij.flex.uiDesigner.css.CssRuleset;
import com.intellij.flex.uiDesigner.css.MergedCssStyleDeclarationEx;
import com.intellij.flex.uiDesigner.css.NonSharedStyleDeclarationProxy;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.ui.CustomTextFormatId;
import com.intellij.flex.uiDesigner.ui.inspectors.AbstractInspector;
import com.intellij.flex.uiDesigner.util.ImmutableFakeObjectProxy;

import flash.display.DisplayObjectContainer;
import flash.utils.Dictionary;

public class StyleInspector extends AbstractInspector implements StylePaneContext {
  protected var source:Vector.<Object>;
  //noinspection JSFieldCanBeLocal
  private var groupItemCache:Dictionary = new Dictionary(true);

  private const namespaceUnificator:NamespaceUnificator = new NamespaceUnificator();

  private var sourceItemCounter:int;

  private var module:Module;
  private var userDocument:Document;

  public function StyleInspector() {
    dataSource = new ListViewDataSourceImpl((source = new Vector.<Object>()));
  }

  public function get rulesets():Vector.<Object> {
    return source;
  }

  //private static const namespaceListPane:PaneItem = new ClassFactory(TextBlockRenderer);
  //    namespaceListRendererFactory.properties = {blockFactory:NamespaceListTextBlockFactory};

  private var _rulesetPrinter:CssRulesetPrinter;
  public function get rulesetPrinter():CssRulesetPrinter {
    return _rulesetPrinter;
  }

  //noinspection JSUnusedGlobalSymbols
  public function set rulesetPrinter(value:CssRulesetPrinter):void {
    _rulesetPrinter = value;
  }

  public function set document(value:Document):void {
    if (value == null) {
      module = null;
    }
    else {
      module = value.module;
    }

    userDocument = value;
    _rulesetPrinter.module = module;
    _rulesetPrinter.interactor.module = module;
  }

  override public function set component(value:Object):void {
    _rulesetPrinter.namespaceUnificator = namespaceUnificator;

    super.component = value;
  }

  public function get documentStyleManager():StyleManagerEx {
    return userDocument.styleManager;
  }

  override protected function isApplicable(element:Object):Boolean {
    return "inheritingStyles" in element;
  }

  override protected function createRendererManager(listView:ListView):RendererManager {
    return new StyleRendererManager(laf.getTextFormat(CustomTextFormatId.SIDE_PANE_GROUP_ITEM_LABEL), laf.getBorder("GroupItemRenderer.b"), this);
  }

  override protected function doUpdateData(element:Object):void {
    sourceItemCounter = 1;

    source[0] = namespaceUnificator;
    namespaceUnificator.reset();

    var collected:Dictionary = new Dictionary();
    collect(element.inheritingStyles, collected, false);

    source.length = sourceItemCounter;
  }

  private function collect(styleDeclarationProxy:ImmutableFakeObjectProxy, collected:Dictionary, onlyRulesetWithInheritedStyles:Boolean):void {
    var allocatedGroupItemIndex:int;
    if (onlyRulesetWithInheritedStyles) {
      allocatedGroupItemIndex = sourceItemCounter++;
      if (sourceItemCounter > source.length) {
        source.length = sourceItemCounter + 2;
      }
    }

    var declarations:Object = styleDeclarationProxy.declarations;
    for (var i:int = declarations.length - 1; i > -1; i--) {
      var declaration:Object = declarations[i];
      if (!(declaration in collected)) {
        collected[declaration] = true;
        if (declaration is MergedCssStyleDeclarationEx) {
          for each (var ruleset:CssRuleset in MergedCssStyleDeclarationEx(declaration).rulesets) {
            if (!onlyRulesetWithInheritedStyles || containsInheritStyle(ruleset)) {
              if (!ruleset.inline) {
                namespaceUnificator.process(ruleset);
              }
              source[sourceItemCounter++] = ruleset;
            }
          }
        }
        else if (!onlyRulesetWithInheritedStyles || containsInheritStyle(declaration.ruleset)) {
          source[sourceItemCounter++] = declaration.ruleset;
        }
      }
    }

    if (onlyRulesetWithInheritedStyles) {
      if (allocatedGroupItemIndex == (sourceItemCounter - 1)) {
        sourceItemCounter--;
      }
      else {
        var sd:NonSharedStyleDeclarationProxy = styleDeclarationProxy as NonSharedStyleDeclarationProxy;
        // if we have multi global (IDEA-72159), we must show it in one group (only one "Global" group item)
        if (sd != null) {
          source[allocatedGroupItemIndex] = createGroupItem(DisplayObjectContainer(sd.owner));
        }
        else {
          var globalItem:StyleDeclarationGroupItem = createGroupItem(null);
          var gI:Number = source.indexOf(globalItem);
          if (gI == -1 || gI < sourceItemCounter) {
            source[allocatedGroupItemIndex] = globalItem;
          }
          else {
            source[allocatedGroupItemIndex] = source[allocatedGroupItemIndex + 1];
            sourceItemCounter--;
          }
        }
      }
    }

    if (styleDeclarationProxy is NonSharedStyleDeclarationProxy) {
      collect(NonSharedStyleDeclarationProxy(styleDeclarationProxy).parent, collected, true);
    }
  }

  private function containsInheritStyle(ruleset:CssRuleset):Boolean {
    var declarations:Vector.<CssDeclaration> = ruleset.declarations;
    for (var i:int = 0, n:int = declarations.length; i < n; i++) {
      if (documentStyleManager.isInheritingStyle(declarations[i].name)) {
        return true;
      }
    }

    return false;
  }

  private function createGroupItem(owner:DisplayObjectContainer):StyleDeclarationGroupItem {
    var item:StyleDeclarationGroupItem = groupItemCache[owner];
    if (item == null) {
      item = new StyleDeclarationGroupItem(owner);
      groupItemCache[owner] = item;
    }
    return item;
  }

  override protected function clear():void {
    if (dataSource != null) {
      ListViewDataSourceImpl(dataSource).clear();
    }
  }
}
}