package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.playback.util.WindowSystemPlaybackCall;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.NettyKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

import static com.intellij.ide.impl.ProjectUtil.focusProjectWindow;

class SafeOpener {
  private static final Logger LOG = Logger.getInstance(SafeOpener.class);

  private static final Set<String> SCHEMES = ContainerUtil.newTroveSet(
    "http",
    "https"
  );

  private static final Set<String> SAFE_LOCAL_EXTENSIONS = ContainerUtil.newTroveSet(
    "md",
    "png",
    "gif",
    "jpg",
    "jpeg",
    "bmp",
    "svg",
    "html"
  );

  private SafeOpener() {
  }

  static void openLink(@NotNull String link) {
    final URI uri;
    try {
      if (!BrowserUtil.isAbsoluteURL(link)) {
        uri = new URI("http://" + link);
      }
      else {
        uri = new URI(link);
      }
    }
    catch (URISyntaxException e) {
      LOG.info(e);
      return;
    }

    if (tryOpenInEditor(uri)) {
      return;
    }
    if (!isHttpScheme(uri.getScheme()) || isLocalHost(uri.getHost()) && !isSafeExtension(uri.getPath())) {
      LOG.warn("Bad URL", new InaccessibleURLOpenedException(link));
      return;
    }

    BrowserUtil.browse(uri);
  }

  private static boolean tryOpenInEditor(@NotNull URI uri) {
    if (!"file".equals(uri.getScheme())) {
      return false;
    }

    VirtualFile file = ReadAction.compute(() -> VirtualFileManager.getInstance().findFileByUrl(uri.toString()));
    if (file == null) return false;

    WindowSystemPlaybackCall.findProject().doWhenDone((Consumer<Project>)project -> {
      ApplicationManager.getApplication().invokeLater(() -> {
        new OpenFileDescriptor(project, file).navigate(true);
        focusProjectWindow(project, true);
      });
    });

    return true;
  }

  private static boolean isHttpScheme(@Nullable String scheme) {
    return scheme != null && SCHEMES.contains(scheme.toLowerCase(Locale.US));
  }

  private static boolean isLocalHost(@Nullable String hostName) {
    return hostName == null
           || hostName.startsWith("127.")
           || hostName.endsWith(":1")
           || NettyKt.isLocalHost(hostName, false, false);
  }

  private static boolean isSafeExtension(@Nullable String path) {
    if (path == null) {
      return false;
    }
    final int i = path.lastIndexOf('.');
    return i != -1 && SAFE_LOCAL_EXTENSIONS.contains(path.substring(i + 1).toLowerCase(Locale.US));
  }

  private static class InaccessibleURLOpenedException extends IllegalArgumentException {
    public InaccessibleURLOpenedException(String link) {
      super(link);
    }
  }
}
