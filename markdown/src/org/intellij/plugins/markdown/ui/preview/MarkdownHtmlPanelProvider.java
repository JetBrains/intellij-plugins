package org.intellij.plugins.markdown.ui.preview;

import com.intellij.CommonBundle;
import com.intellij.openapi.ui.Messages;
import org.intellij.plugins.markdown.ui.preview.lobo.LoboHtmlPanelProvider;
import org.jetbrains.annotations.NotNull;

public abstract class MarkdownHtmlPanelProvider {

  public static final MarkdownHtmlPanelProvider DEFAULT = new LoboHtmlPanelProvider();

  public abstract MarkdownHtmlPanel createHtmlPanel();

  public abstract boolean isAvailable();

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
      return DEFAULT;
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
  }
}
