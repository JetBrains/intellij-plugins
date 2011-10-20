package com.intellij.flex.uiDesigner.ui.inspectors.styleInspector {
import com.intellij.flex.uiDesigner.css.StyleManagerEx;

public interface StylePaneContext {
  function get rulesetPrinter():CssRulesetPrinter;
  
  function get documentStyleManager():StyleManagerEx;
  
  function get rulesets():Vector.<Object>;
}
}