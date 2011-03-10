package com.intellij.flex.uiDesigner.flex {
import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.flex.cssBlockRenderer.CssRulesetPrinter;

public interface StylePaneContext {
  function get rulesetPrinter():CssRulesetPrinter;
  
  function get styleManager():StyleManagerEx;
  
  function get rulesets():Vector.<Object>;
}
}
