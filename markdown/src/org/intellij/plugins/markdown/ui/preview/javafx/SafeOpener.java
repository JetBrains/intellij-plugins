package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ObjectUtils;
import com.intellij.util.PsiNavigateUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.NettyKt;
import org.intellij.plugins.markdown.MarkdownBundle;
import org.intellij.plugins.markdown.lang.references.MarkdownAnchorReference;
import org.intellij.plugins.markdown.ui.preview.MarkdownSplitEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
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

    return ReadAction.compute(() -> {
      String anchor = uri.getFragment();
      String path = uri.getPath();

      final VirtualFile targetFile = LocalFileSystem.getInstance().findFileByPath(path);
      if (targetFile == null) {
        return false;
      }

      Project project = ProjectUtil.guessProjectForContentFile(targetFile);
      if (project == null) {
        return false;
      }

      if (anchor == null) {
        navigateToFile(project, targetFile);
        return true;
      }

      final JFrame frame = WindowManager.getInstance().getFrame(project);
      final Point mousePosition = Objects.requireNonNull(frame).getMousePosition();
      if (mousePosition == null) return false;
      RelativePoint point = new RelativePoint(frame, mousePosition);

      ApplicationManager.getApplication().invokeLater(() -> {
        Collection<PsiElement> headers = ReadAction.compute(
          () -> MarkdownAnchorReference.Companion.getPsiHeaders(project, anchor, PsiManager.getInstance(project).findFile(targetFile)));

        if (headers.isEmpty()) {
          showCannotNavigateNotification(project, anchor, point);
        }
        else if (headers.size() == 1) {
          navigateToHeader(targetFile, Objects.requireNonNull(ContainerUtil.getFirstItem(headers)));
        }
        else {
          showHeadersPopup(headers, point);
        }
      });

      return true;
    });
  }

  private static void showCannotNavigateNotification(@NotNull Project project, @NotNull String anchor, @NotNull RelativePoint point) {
    BalloonBuilder balloonBuilder = JBPopupFactory.getInstance()
      .createHtmlTextBalloonBuilder(MarkdownBundle.message("markdown.navigate.to.header.no.headers", anchor), MessageType.WARNING,
                                    null);
    final Balloon balloon = balloonBuilder.createBalloon();
    balloon.show(point, Balloon.Position.below);
    Disposer.register(project, balloon);
  }

  private static void navigateToHeader(@NotNull VirtualFile targetFile, @NotNull PsiElement item) {
    MarkdownSplitEditor splitEditor = Objects.requireNonNull(
      ObjectUtils.tryCast(FileEditorManager.getInstance(item.getProject()).getSelectedEditor(targetFile), MarkdownSplitEditor.class));

    boolean oldAutoScrollPreview = splitEditor.isAutoScrollPreview();

    if (!oldAutoScrollPreview) splitEditor.setAutoScrollPreview(true);
    PsiNavigateUtil.navigate(item);

    if (!oldAutoScrollPreview) splitEditor.setAutoScrollPreview(false);
  }

  private static void navigateToFile(@NotNull Project project, @NotNull VirtualFile targetFile) {
    PsiNavigationSupport.getInstance().createNavigatable(project, targetFile, -1).navigate(true);
    focusProjectWindow(project, true);
  }

  private static void showHeadersPopup(@NotNull Collection<PsiElement> headers, @NotNull RelativePoint point) {
    ListPopupStep headersPopup =
      new BaseListPopupStep<PsiElement>(MarkdownBundle.message("markdown.navigate.to.header"), ContainerUtil.newArrayList(headers)) {
        @NotNull
        @Override
        public String getTextFor(PsiElement value) {
          Document document = FileDocumentManager.getInstance().getDocument(value.getContainingFile().getVirtualFile());
          String name = value.getContainingFile().getVirtualFile().getName();

          return value.getText() + " (" + name + ":" + (Objects.requireNonNull(document).getLineNumber(value.getTextOffset()) + 1) + ")";
        }

        @Override
        public PopupStep onChosen(final PsiElement selectedValue, boolean finalChoice) {
          return doFinalStep(() -> navigateToHeader(selectedValue.getContainingFile().getVirtualFile(), selectedValue));
        }
      };

    JBPopupFactory.getInstance().createListPopup(headersPopup).show(point);
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
