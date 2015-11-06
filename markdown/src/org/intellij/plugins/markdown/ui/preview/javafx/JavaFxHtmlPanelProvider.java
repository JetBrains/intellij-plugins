package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class JavaFxHtmlPanelProvider extends MarkdownHtmlPanelProvider {
  private static final Logger LOG = Logger.getInstance(JavaFxHtmlPanelProvider.class);

  private static ClassLoader MY_CLASS_LOADER = null;

  @NotNull
  @Override
  public MarkdownHtmlPanel createHtmlPanel() {
    try {
      return (MarkdownHtmlPanel)MY_CLASS_LOADER
        .loadClass("org.intellij.plugins.markdown.ui.preview.javafx.JavaFxHtmlPanel")
        .newInstance();
    }
    catch (ClassNotFoundException e) {
      throw new IllegalStateException("Should not be called if unavailable", e);
    }
    catch (InstantiationException e) {
      throw new IllegalStateException("Should not be called if unavailable", e);
    }
    catch (IllegalAccessException e) {
      throw new IllegalStateException("Should not be called if unavailable", e);
    }
  }

  @NotNull
  @Override
  public AvailabilityInfo isAvailable() {
    if (shouldUseCustomJFX()) {
      JavaFXMoveInstallationFix.INSTANCE.runFixIfCan();
    }

    if (hasClass("javafx.scene.web.WebView", true)) {
      return AvailabilityInfo.AVAILABLE;
    }

    if (shouldUseCustomJFX()) {
      return new AvailabilityInfo() {
        @Override
        public boolean checkAvailability(@NotNull JComponent parentComponent) {
          if (!installOpenJFXAndReport(parentComponent)) {
            return false;
          }
          return hasClass("javafx.scene.web.WebView", true);
        }
      };
    }
    return AvailabilityInfo.UNAVAILABLE;
  }

  @NotNull
  @Override
  public ProviderInfo getProviderInfo() {
    return new ProviderInfo("JavaFX WebView", JavaFxHtmlPanelProvider.class.getName());
  }

  @NotNull
  private static ClassLoader createClassLoader() {
    if (!shouldUseCustomJFX()) {
      return JavaFxHtmlPanelProvider.class.getClassLoader();
    }

    final ArrayList<URL> urls = new ArrayList<URL>();
    try {
      final String installationPathRoot = JavaFXInstallator.INSTANCE.getInstallationPath();

      urls.add(new URI("file", "", installationPathRoot + "/jre/lib/ext/jfxrt.jar", null).toURL());
      urls.add(new URI("file", "", installationPathRoot + "/jre/lib/jfxswt.jar", null).toURL());
      urls.add(new URI("file", "", installationPathRoot + "/jre/lib/*.dylib", null).toURL());
      urls.add(JavaFxHtmlPanelProvider.class.getClassLoader()
                 .getResource("/org/intellij/plugins/markdown/ui/preview/javafx/JavaFxHtmlPanel.class"));
    }
    catch (Exception ignore) {
    }

    final ClassLoader parent = JavaFxHtmlPanelProvider.class.getClassLoader();
    if (parent instanceof PluginClassLoader) {
      urls.addAll(((PluginClassLoader)parent).getUrls());
    }

    LOG.info("ClassLoader urls: " + urls.toString());

    return new MyClassLoader(urls.toArray(new URL[urls.size()]), parent);
  }

  private static boolean hasClass(@NotNull String name, boolean withoutCache) {
    try {
      if (!withoutCache) {
        if (Class.forName(name, false, JavaFxHtmlPanelProvider.class.getClassLoader()) != null) {
          return true;
        }
      }
      else {
        if (MY_CLASS_LOADER != null) {
          return MY_CLASS_LOADER.loadClass(name) != null;
        }

        final ClassLoader classLoader = createClassLoader();
        final boolean success = classLoader.loadClass(name) != null;

        if (success) {
          MY_CLASS_LOADER = classLoader;
        }
        return success;
      }
    }
    catch (Exception ignore) {
    }
    return false;
  }

  private static boolean installOpenJFXAndReport(@NotNull JComponent parentComponent) {
    final int answer = Messages.showYesNoDialog(parentComponent,
                                                "Would you like to download and install OpenJFX?",
                                                "OpenJFX Installation",
                                                null);
    if (answer == Messages.NO) {
      return false;
    }
    return JavaFXInstallator.INSTANCE.installOpenJFXAndReport(parentComponent);
  }

  private static boolean shouldUseCustomJFX() {
    return SystemInfo.isJetbrainsJvm && SystemInfo.isMac;
  }

  private static class MyClassLoader extends URLClassLoader {
    public MyClassLoader(URL[] urls, ClassLoader classLoader) {
      super(urls, classLoader);
    }

    @Override
    protected Class<?> loadClass(String s, boolean b) throws ClassNotFoundException {
      if (s.startsWith("java.") || s.startsWith("javax.")) {
        return super.loadClass(s, b);
      }

      Class<?> loadedClass = findLoadedClass(s);
      if (loadedClass == null) {
        if (!s.contains("JavaFxHtmlPanel$") && !s.endsWith("JavaFxHtmlPanel")) {
          try {
            loadedClass = getParent().loadClass(s);
          }
          catch (ClassNotFoundException ignore) {
          }
        }

        if (loadedClass == null) {
          loadedClass = findClass(s);
        }
      }

      if (b) {
        resolveClass(loadedClass);
      }

      return loadedClass;
    }
  }
}
