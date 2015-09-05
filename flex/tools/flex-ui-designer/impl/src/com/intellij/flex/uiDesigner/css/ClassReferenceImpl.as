package com.intellij.flex.uiDesigner.css {
import com.intellij.flex.uiDesigner.flex.ClassReference;

public class ClassReferenceImpl implements ClassReference {
  public function ClassReferenceImpl(className:String) {
    _className = className;
  }

  private var _className:String;
   public function get className():String {
    return _className;
  }
}
}