package com.intellij.javascript.bower;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class BowerValidationInfo {

  public static final String LINK_TEMPLATE = "{{LINK}}";
  private static final Logger LOG = Logger.getInstance(BowerValidationInfo.class);

  private final Component myComponent;
  private final @Nls String myErrorHtmlDescription;
  private final @Nls String myLinkText;

  public BowerValidationInfo(@Nullable Component component,
                             @Nls @NotNull String errorHtmlDescriptionTemplate,
                             @Nls @NotNull String linkText) {
    myComponent = component;
    if (!errorHtmlDescriptionTemplate.contains(LINK_TEMPLATE)) {
      LOG.warn("Cannot find " +  LINK_TEMPLATE + " in " + errorHtmlDescriptionTemplate);
    }
    String linkHtml = HtmlChunk.link(linkText, linkText).toString();
    myErrorHtmlDescription = errorHtmlDescriptionTemplate.replace(LINK_TEMPLATE, linkHtml);
    myLinkText = linkText;
  }

  public @Nullable Component getComponent() {
    return myComponent;
  }

  public @NotNull String getErrorHtmlDescription() {
    return myErrorHtmlDescription;
  }

  public @Nullable String getLinkText() {
    return myLinkText;
  }
}
