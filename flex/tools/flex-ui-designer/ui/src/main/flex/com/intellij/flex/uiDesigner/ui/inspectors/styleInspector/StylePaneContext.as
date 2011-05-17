package com.intellij.flex.uiDesigner.ui.inspectors.styleInspector {
import com.intellij.flex.uiDesigner.css.StyleManagerEx;

public interface StylePaneContext {
  function get rulesetPrinter():CssRulesetPrinter;
  
  function get styleManager():StyleManagerEx;
  
  function get rulesets():Vector.<Object>;
}
}