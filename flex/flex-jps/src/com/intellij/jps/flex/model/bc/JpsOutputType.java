package com.intellij.jps.flex.model.bc;


import com.intellij.jps.flex.JpsFlexBundle;

public enum JpsOutputType {
  Application(JpsFlexBundle.message("bc.app.long.text"), JpsFlexBundle.message("bc.app.short.text")),
  Library(JpsFlexBundle.message("bc.lib.long.text"), JpsFlexBundle.message("bc.lib.short.text")),
  RuntimeLoadedModule(JpsFlexBundle.message("bc.rlm.long.text"), JpsFlexBundle.message("bc.rlm.short.text"));

  private final String myPresentableText;
  private final String myShortText;

  public String getPresentableText() {
    return myPresentableText;
  }

  public String getShortText() {
    return myShortText;
  }

  JpsOutputType(final String presentableText, final String shortText) {
    myPresentableText = presentableText;
    myShortText = shortText;
  }
}
