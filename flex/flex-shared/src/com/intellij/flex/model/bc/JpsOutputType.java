package com.intellij.flex.model.bc;


import com.intellij.flex.FlexCommonBundle;

public enum JpsOutputType {
  Application(FlexCommonBundle.message("bc.app.long.text"), FlexCommonBundle.message("bc.app.short.text")),
  Library(FlexCommonBundle.message("bc.lib.long.text"), FlexCommonBundle.message("bc.lib.short.text")),
  RuntimeLoadedModule(FlexCommonBundle.message("bc.rlm.long.text"), FlexCommonBundle.message("bc.rlm.short.text"));

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
