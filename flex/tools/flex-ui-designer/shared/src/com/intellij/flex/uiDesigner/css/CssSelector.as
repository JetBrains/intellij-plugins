package com.intellij.flex.uiDesigner.css {
public class CssSelector {
  public var subject:String;
  public var presentableSubject:String;
  public var namespacePrefix:String;
  public var conditions:Vector.<CssCondition>;
  public var ancestor:CssSelector;

  public function CssSelector(subject:String, presentableSubject:String, namespacePrefix:String, conditions:Vector.<CssCondition>, ancestor:CssSelector) {
    this.subject = subject;
    this.presentableSubject = presentableSubject;
    this.namespacePrefix = namespacePrefix;
    this.conditions = conditions;
    this.ancestor = ancestor;
  }

  public function matches(object:Object):Boolean {
    var condition:CssCondition;
    if (ancestor == null) {
      if (subject == null || object.matchesCSSType(subject)) {
        if (conditions != null) {
          for each (condition in conditions) {
            if (!condition.matches(object)) {
              return false;
            }
          }
        }

        return true;
      }
    }
    else {
      if (conditions != null) {
        for each (condition in conditions) {
          if (!condition.matches(object)) {
            return false;
          }
        }
      }

      var parent:Object = object.styleParent;
      while (parent != null) {
        if ((ancestor.subject == null || parent.matchesCSSType(ancestor.subject)) && ancestor.matches(parent)) {
          return true;
        }
        parent = parent.styleParent;
      }
    }

    return false;
  }

  public function getPseudoCondition():String {
    if (conditions != null) {
      for each (var condition:CssCondition in conditions) {
        if (condition is CssPseudoCondition) {
          return condition.value;
        }
      }
    }

    return null;
  }

  private var _specificity:int = -1;
  public function get specificity():int {
    if (_specificity == -1) {
      _specificity = 0;
      if (subject != null && subject != "global") {
        _specificity = 1;
      }

      if (conditions != null) {
        for each (var condition:CssCondition in conditions) {
          _specificity += condition.specificity;
        }
      }

      if (ancestor != null) {
        _specificity += ancestor.specificity;
      }
    }
    
    return _specificity;
  }
}
}
