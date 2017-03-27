package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.io.NettyKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

class SafeOpener {
  private static final Logger LOG = Logger.getInstance(SafeOpener.class);

  private static final Set<String> SCHEMES = new HashSet<>(Arrays.asList(
    "http",
    "https"
  ));

  private static final Set<String> LOCALHOSTS = new HashSet<>(Arrays.asList(
    "localhost",
    "127.0.0.1",
    "::1",
    "0.0.0.0"
  ));

  private static final Set<String> SAFE_LOCAL_EXTENSIONS = new HashSet<>(Arrays.asList(
    "md",
    "png",
    "gif",
    "jpg",
    "jpeg",
    "bmp",
    "svg",
    "html"
  ));

  private SafeOpener() {
  }

  static void openLink(@NotNull String link) {
    final URI uri;
    try {
      if (!BrowserUtil.isAbsoluteURL(link)) {
        // TODO handle links opening via editor
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

    if (!isHttpScheme(uri.getScheme()) || isLocalHost(uri.getHost()) && !isSafeExtension(uri.getPath())) {
      LOG.error("Malicious code", new MaliciousURLOpenedException(link));
      return;
    }

    BrowserUtil.browse(uri);
  }

  private static boolean isHttpScheme(@Nullable String scheme) {
    return scheme != null && SCHEMES.contains(scheme.toLowerCase(Locale.US));
  }

  private static boolean isLocalHost(@Nullable String hostName) {
    return hostName == null
           || LOCALHOSTS.contains(hostName.toLowerCase(Locale.US))
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

  private static class MaliciousURLOpenedException extends IllegalArgumentException {
    public MaliciousURLOpenedException(String link) {
      super(link);
    }
  }
}
