package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.ZipAndQueue;
import com.intellij.openapi.vfs.*;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.EnvironmentUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.connections.P4ConfigFields;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author irengrig
 */
public class PerforceLoginTicketsListener implements VirtualFileListener {
  // null -> listener disabled
  @Nullable
  private static final String ourP4TicketsFile = calculateAndNotify();
  private final AtomicReference<String> myLoadedText;
  private final PerforceLoginManager myLoginManager;
  private final ZipAndQueue myZipAndQueue;

  @Nullable
  private static String calculateAndNotify() {
    String value = calculatePath();
    if (value == null) {
      Notifications.Bus.notify(new Notification(PerforceVcs.getKey().getName(), PerforceBundle.message("login.monitor.failure"),
                                                PerforceBundle.message("login.monitor.failure.msg"), NotificationType.ERROR));
      return null;
    }
    try {
      value = new File(value).getCanonicalPath().replace('\\', '/');
    }
    catch (IOException e) {
      value = value.replace('\\', '/');
    }
    return value;
  }

  /**
   * Perforce client programs store tickets in the file specified by the P4TICKETS environment variable.
   * If this variable is not set, tickets are stored in %USERPROFILE%\p4tickets.txt on Windows
   * and in $HOME/.p4tickets on UNIX.
   */
  @Nullable
  private static String calculatePath() {
    String ticketsFilePath = EnvironmentUtil.getValue(P4ConfigFields.P4TICKETS);
    if (!StringUtil.isEmptyOrSpaces(ticketsFilePath)) return ticketsFilePath;

    if (SystemInfo.isWindows) {
      String userProfile = EnvironmentUtil.getValue("USERPROFILE");
      return StringUtil.isEmptyOrSpaces(userProfile) ? null : new File(userProfile, "p4tickets.txt").getPath();
    }
    else {
      String home = EnvironmentUtil.getValue("HOME");
      return StringUtil.isEmptyOrSpaces(home) ? null : new File(home, ".p4tickets").getPath();
    }
  }

  public void pingListening() {
    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(ourP4TicketsFile));
  }

  public PerforceLoginTicketsListener(final Project project, final PerforceLoginManager loginManager, @NotNull Disposable parentDisposable) {
    myLoginManager = loginManager;
    myLoadedText = new AtomicReference<>();
    myZipAndQueue = new ZipAndQueue(project, -1, PerforceBundle.message("login.refresh.auth.state"), parentDisposable, () -> myLoginManager.refreshLoginState());
  }

  @Override
  public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
    processEvent(event);
  }

  @Override
  public void contentsChanged(@NotNull VirtualFileEvent event) {
    processEvent(event);
  }

  @Override
  public void fileCreated(@NotNull VirtualFileEvent event) {
    processEvent(event);
  }

  @Override
  public void fileDeleted(@NotNull VirtualFileEvent event) {
    processEvent(event);
  }

  @Override
  public void fileMoved(@NotNull VirtualFileMoveEvent event) {
    processEvent(event);
  }

  @Override
  public void fileCopied(@NotNull VirtualFileCopyEvent event) {
    processEvent(event);
  }

  public static boolean shouldRegister() {
    return ourP4TicketsFile != null;
  }

  private void processEvent(VirtualFileEvent event) {
    if (ourP4TicketsFile == null) return;
    final String path = event.getFile().getPath();
    if (SystemInfo.isWindows && path.equalsIgnoreCase(ourP4TicketsFile) || path.equals(ourP4TicketsFile)) {
      try {
        final File file = new File(ourP4TicketsFile);
        final char[] text = file.exists() ? FileUtil.loadFileText(file) : ArrayUtilRt.EMPTY_CHAR_ARRAY;
        final String asString = String.valueOf(text);
        final String was = myLoadedText.getAndSet(asString);
        if (! asString.equals(was)) {
          myZipAndQueue.request();
        }
      }
      catch (IOException e) {
        //
      }
    }
  }
}
