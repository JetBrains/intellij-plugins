package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.library.JstdLibraryUtil;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitFileStructure;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitFileStructureBuilder;
import com.google.jstestdriver.idea.execution.JstdRuntimeConfigurationProducer;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.execution.*;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.util.Function;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JstdAssertionFrameworkLineMarkerProvider implements LineMarkerProvider {

  private enum Type {
    RUN, DEBUG
  }

  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
    Project project = element.getProject();
    JSFile jsFile = ObjectUtils.tryCast(element.getContainingFile(), JSFile.class);
    if (jsFile == null) {
      return null;
    }
    LineMarkerInfo lineMarkerInfo = getJstdLineMarkerInfo(project, jsFile, element);
    if (lineMarkerInfo == null) {
      lineMarkerInfo = getQUnitLineMarkerInfo(jsFile, element);
    }
    return lineMarkerInfo;
  }

  @Nullable
  private static LineMarkerInfo getJstdLineMarkerInfo(@NotNull Project project,
                                                      @NotNull JSFile jsFile,
                                                      @NotNull PsiElement element) {
    VirtualFile virtualFile = jsFile.getVirtualFile();
    if (virtualFile == null) {
      return null;
    }
    boolean inScope = JstdLibraryUtil.isFileInJstdLibScope(project, virtualFile);
    if (!inScope) {
      return null;
    }
    JstdTestFileStructure fileStructure = JstdTestFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
    String testElementName = fileStructure.getNameByPsiElement(element);
    if (testElementName == null) {
      return null;
    }
    return createLineMarkerFromElement(element, testElementName);
  }

  @Nullable
  private static LineMarkerInfo getQUnitLineMarkerInfo(@NotNull JSFile jsFile,
                                                       @NotNull PsiElement element) {
    QUnitFileStructure qunitFileStructure = QUnitFileStructureBuilder.getInstance().fetchCachedTestFileStructure(jsFile);
    String testElementName = qunitFileStructure.getNameByPsiElement(element);
    if (testElementName == null) {
      return null;
    }
    return createLineMarkerFromElement(element, testElementName);
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    // does nothing
  }

  private static LineMarkerInfo createLineMarkerFromElement(@NotNull PsiElement testElement,
                                                            @NotNull final String displayName) {
    return new LineMarkerInfo<PsiElement>(
      testElement,
      testElement.getTextRange(),
      AllIcons.Vcs.Arrow_right,
      Pass.UPDATE_ALL,
      new Function<PsiElement, String>() {
        @Override
        public String fun(PsiElement element) {
          return "Execute '" + displayName + "'";
        }
      },
      new GutterIconNavigationHandler<PsiElement>() {
        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
          if (elt.isValid()) {
            showPopup(e, elt, displayName);
          }
        }
      },
      GutterIconRenderer.Alignment.RIGHT
    );
  }

  private static void showPopup(@NotNull MouseEvent e, @NotNull final PsiElement psiElement, final String displayName) {
    final JBList list = new JBList(Type.values());
    list.setCellRenderer(new ListCellRendererWrapper() {
      @Override
      public void customize(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value == Type.RUN) {
          setIcon(AllIcons.Toolwindows.ToolWindowRun);
          setText("Run '" + displayName + "'");
        }
        else if (value == Type.DEBUG) {
          setIcon(AllIcons.Toolwindows.ToolWindowDebugger);
          setText("Debug '" + displayName + "'");
        }
      }
    });
    PopupChooserBuilder builder = new PopupChooserBuilder(list);
    JBPopup popup = builder.
      setMovable(true).
      setItemChoosenCallback(new Runnable() {
        @Override
        public void run() {
          int[] ids = list.getSelectedIndices();
          if (ids == null || ids.length == 0) return;
          Object[] selectedElements = list.getSelectedValues();
          for (Object element : selectedElements) {
            if (psiElement.isValid()) {
              if (element == Type.RUN) {
                execute(DefaultRunExecutor.getRunExecutorInstance(), psiElement);
              }
              else if (element == Type.DEBUG) {
                execute(DefaultDebugExecutor.getDebugExecutorInstance(), psiElement);
              }
            }
          }
        }
      }).
      createPopup();

    popup.show(new RelativePoint(e));
  }

  private static void execute(@NotNull Executor executor, @NotNull PsiElement element) {
    Project project = element.getProject();
    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    if (editor == null) {
      return;
    }
    DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());
    JstdRuntimeConfigurationProducer jstdOriginalProducer = getJstdRuntimeConfigurationProducer();
    if (jstdOriginalProducer == null) {
      return;
    }
    Location<PsiElement> location = PsiLocation.fromPsiElement(element);
    ConfigurationContext context = ConfigurationContext.getFromContext(dataContext);
    RuntimeConfigurationProducer producer = jstdOriginalProducer.createProducer(location, context);
    boolean created = false;
    RunnerAndConfigurationSettings configuration = producer.findExistingConfiguration(location, context);
    if (configuration == null) {
      created = true;
      configuration = producer.getConfiguration();
      if (configuration == null) {
        return;
      }
    }

    execute(project, executor, configuration, created);
  }

  @Nullable
  private static JstdRuntimeConfigurationProducer getJstdRuntimeConfigurationProducer() {
    return RuntimeConfigurationProducer.getInstance(JstdRuntimeConfigurationProducer.class);
  }

  private static void execute(@NotNull Project project,
                              @NotNull Executor executor,
                              @NotNull RunnerAndConfigurationSettings configuration,
                              boolean created) {
    RunManagerEx runManager = RunManagerEx.getInstanceEx(project);
    if (created) {
      runManager.setTemporaryConfiguration(configuration);
    }
    runManager.setSelectedConfiguration(configuration);
    if (configuration.isSingleton()) {
      ExecutionTarget activeTarget = ExecutionTargetManager.getActiveTarget(project);
      ExecutionManager.getInstance(project).restartRunProfile(project,
                                                              executor,
                                                              activeTarget,
                                                              configuration,
                                                              (RunContentDescriptor)null);
    }
    else {
      ProgramRunnerUtil.executeConfiguration(project, configuration, executor);
    }
  }

}
