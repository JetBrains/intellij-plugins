package com.intellij.lang.javascript.linter.eslint;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.history.LocalHistory;
import com.intellij.ide.actions.OpenFileAction;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.linter.JSLinterUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class JSLinterReformatterTask {

  protected final @NotNull Project myProject;
  private final @NotNull String myLinterName;
  private final @NotNull Collection<? extends VirtualFile> myRoots;
  private final @NotNull Runnable myCompleteCallback;
  private final MultiMap<VirtualFile, Pair<@InspectionMessage String, IntentionAction[]>> myProblems = new MultiMap<>();

  public JSLinterReformatterTask(@NotNull Project project,
                                 @Nls(capitalization = Nls.Capitalization.Title) @NotNull String linterName,
                                 @NotNull Collection<? extends VirtualFile> roots,
                                 @NotNull Runnable completeCallback) {
    myProject = project;
    myLinterName = linterName;
    myRoots = roots;
    myCompleteCallback = completeCallback;
  }

  private void doRun(@NotNull ProgressIndicator indicator) {
    LocalHistory localHistory = LocalHistory.getInstance();
    localHistory.putSystemLabel(myProject, JavaScriptBundle.message("javascript.linter.action.fix.problems.name.start", myLinterName));
    try {
      for (VirtualFile file : myRoots) {
        if (!file.isDirectory()) {
          final String parentPath = file.getParent() == null ? "" : (" (" + file.getParent().getPath() + ")");
          indicator.setText(JavaScriptBundle.message("progress.text.processing", file.getName(), parentPath));
          formatFile(file);
        }
      }
    }
    finally {
      String message = JavaScriptBundle.message("javascript.linter.action.fix.problems.name.finish", myLinterName);
      localHistory.putSystemLabel(myProject, message);
      myCompleteCallback.run();
    }
  }

  protected void doOnSuccess() {
    if (!myProblems.isEmpty()) {
      new MyNotification(myProject, myLinterName, myProblems).showNotification();
    }
  }

  private static class MyNotification {
    private final List<IntentionAction> intentions = new ArrayList<>();
    private final List<VirtualFile> files = new ArrayList<>();
    private final @NotNull Project myProject;
    private final @NotNull String myLinterName;
    private String myMessage;

    MyNotification(final @NotNull Project project, final @NotNull String linterName,
                   final @NotNull MultiMap<VirtualFile, Pair<String, IntentionAction[]>> problems) {
      myProject = project;
      myLinterName = linterName;
      if (problems.isEmpty()) return;
      myMessage = fillData(problems);
    }

    private String fillData(MultiMap<VirtualFile, Pair<@InspectionMessage String, IntentionAction[]>> problems) {
      final Function<Map.Entry<VirtualFile, Collection<Pair<@InspectionMessage String, IntentionAction[]>>>, String> function =
        entry -> {
          final String path = entry.getKey().getPath();
          HtmlBuilder builder = new HtmlBuilder()
            .appendLink("#file" + path, entry.getKey().getName()).append(": ");
          boolean withComma = false;
          for (Pair<@InspectionMessage String, IntentionAction[]> pair : entry.getValue()) {
            if (withComma) builder.br();
            withComma = true;
            if (pair.getSecond().length > 0) {
              builder.append(pair.getFirst()).append(" ").appendWithSeparators(
                HtmlChunk.text(", "),
                ContainerUtil.map(pair.getSecond(), action -> {
                  intentions.add(action);
                  files.add(entry.getKey());
                  return HtmlChunk.link("#action" + (intentions.size() - 1), action.getText());
                }));
            }
            else {
              builder.append(pair.getFirst());
            }
          }
          return builder.toString();
        };
      return StringUtil.join(problems.entrySet(), function, "<br/>");
    }

    public void showNotification() {
      if (myMessage == null || myMessage.isEmpty()) return;
      JSLinterUtil.NOTIFICATION_GROUP
        .createNotification(
          JavaScriptBundle.message("javascript.linter.error.notification.problem.with.reformatting", myLinterName, myMessage),
          NotificationType.ERROR)
        .setListener(new MyNotificationListener())
        .notify(myProject);
    }

    private class MyNotificationListener implements NotificationListener {
      @Override
      public void hyperlinkUpdate(@NotNull Notification notification,
                                  @NotNull HyperlinkEvent event) {
        final String description = event.getDescription();
        if (description != null && description.startsWith("#file")) {
          final String path = description.substring(5);
          OpenFileAction.openFile(path, myProject);
        }
        else if (description != null && description.startsWith("#action")) {
          final String number = description.substring(6);
          try {
            final int idx = Integer.parseInt(number);
            if (idx >= 0 && idx < intentions.size()) {
              final VirtualFile file = files.get(idx);
              PsiFile psiFile = null;
              Editor editor = null;
              if (file != null) {
                psiFile = PsiManager.getInstance(myProject).findFile(file);
                if (psiFile != null) {
                  editor = PsiEditorUtil.findEditor(psiFile);
                }
              }
              intentions.get(idx).invoke(myProject, editor, psiFile);
            }
          }
          catch (NumberFormatException e) {
            //ignore
          }
        }
      }
    }
  }

  private void formatFile(VirtualFile file) {
    PsiFile psiFile = ReadAction.compute(() -> PsiManager.getInstance(myProject).findFile(file));
    if (psiFile == null) {
      error(file, JavaScriptBundle.message("javascript.linter.error.can.not.find.psi.file", file.getPath()));
      return;
    }
    Document document = ReadAction.compute(() -> PsiDocumentManager.getInstance(myProject).getDocument(psiFile));
    if (document == null) {
      error(file, JavaScriptBundle.message("javascript.linter.error.can.not.find.document", psiFile.getName()));
      return;
    }

    runLinter(psiFile, document);
  }

  protected void error(final @NotNull VirtualFile file, @InspectionMessage String error, IntentionAction... fixes) {
    myProblems.putValue(file, Pair.create(error, fixes));
  }

  protected abstract void runLinter(@NotNull PsiFile psiFile, @NotNull Document document);

  public Task createTask(boolean modalProgress) {
    Task.Backgroundable task =
      new Task.Backgroundable(myProject, JavaScriptBundle.message("javascript.linter.progress.reformatting.with", myLinterName), true) {
        @Override
        public void run(@NotNull ProgressIndicator indicator) { doRun(indicator); }

        @Override
        public void onSuccess() { doOnSuccess(); }
      };
    return task.toModalIfNeeded(modalProgress);
  }
}
