package org.intellij.plugins.markdown.ui.preview;

import com.intellij.CommonBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public abstract class MarkdownHtmlPanelProvider {

  public static final ExtensionPointName<MarkdownHtmlPanelProvider> EP_NAME =
    ExtensionPointName.create("org.intellij.markdown.html.panel.provider");

  private static MarkdownHtmlPanelProvider[] ourProviders = null;

  @NotNull
  public abstract MarkdownHtmlPanel createHtmlPanel();

  public abstract boolean isAvailable();

  @NotNull
  public abstract ProviderInfo getProviderInfo();

  @NotNull
  public static MarkdownHtmlPanelProvider[] getProviders() {
    if (ourProviders == null) {
      ourProviders = EP_NAME.getExtensions();
      Logger.getInstance(MarkdownHtmlPanelProvider.class).warn(Arrays.deepToString(ourProviders));
    }
    return ourProviders;
  }

  @NotNull
  public static MarkdownHtmlPanelProvider createFromInfo(@NotNull ProviderInfo providerInfo) {
    try {
      return ((MarkdownHtmlPanelProvider)Class.forName(providerInfo.getClassName()).newInstance());
    }
    catch (Exception e) {
      Messages.showMessageDialog(
        "Cannot set preview panel provider (" + providerInfo.getName() + "):\n" + e.getMessage(),
        CommonBundle.getErrorTitle(),
        Messages.getErrorIcon()
      );
      return getProviders()[0];
    }
  }

  public static class ProviderInfo {
    @NotNull
    private String myName;
    @NotNull
    private String className;

    public ProviderInfo(@NotNull String name, @NotNull String className) {
      myName = name;
      this.className = className;
    }

    @NotNull
    public String getName() {
      return myName;
    }

    @NotNull
    public String getClassName() {
      return className;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ProviderInfo info = (ProviderInfo)o;

      if (!myName.equals(info.myName)) return false;
      if (!className.equals(info.className)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = myName.hashCode();
      result = 31 * result + className.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return myName;
    }
  }
}
