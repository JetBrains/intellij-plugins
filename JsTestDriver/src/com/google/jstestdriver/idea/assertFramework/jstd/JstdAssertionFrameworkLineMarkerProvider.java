package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.library.JstdLibraryUtil;
import com.google.jstestdriver.idea.debug.JstdDebugProgramRunner;
import com.google.jstestdriver.idea.execution.JstdRunConfigurationProducer;
import com.google.jstestdriver.idea.execution.JstdSettingsUtil;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.execution.*;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructure;
import com.intellij.javascript.testFramework.qunit.QUnitFileStructureBuilder;
import com.intellij.lang.javascript.psi.JSCallExpression;
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
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
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

  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
    Project project = element.getProject();
    JSFile jsFile = ObjectUtils.tryCast(element.getContainingFile(), JSFile.class);
    if (jsFile == null) {
      return null;
    }
    if (!JstdSettingsUtil.areJstdConfigFilesInProjectCached(project)) {
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
    if (element instanceof JSCallExpression) {
      JSCallExpression callExpression = (JSCallExpression) element;
      String testElementName = qunitFileStructure.getNameByPsiElement(callExpression);
      if (testElementName == null) {
        return null;
      }
      return createLineMarkerFromElement(element, testElementName);
    }
    return null;
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    // does nothing
  }

  private static LineMarkerInfo createLineMarkerFromElement(@NotNull PsiElement testElement,
                                                            @NotNull final String displayName) {
    return new LineMarkerInfo<>(
      testElement,
      testElement.getTextRange(),
      AllIcons.Vcs.Arrow_right,
      Pass.LINE_MARKERS,
      element -> "Execute '" + displayName + "'",
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
    final JBList list = new JBList(getAvailableTypes());
    list.setCellRenderer(new ListCellRendererWrapper<Type>() {
      @Override
      public void customize(JList list, Type value, int index, boolean selected, boolean hasFocus) {
        setIcon(value.getIcon());
        setText(value.getTitle(displayName));
      }
    });
    PopupChooserBuilder builder = new PopupChooserBuilder(list);
    JBPopup popup = builder.
      setMovable(true).
      setItemChoosenCallback(() -> {
        int[] ids = list.getSelectedIndices();
        if (ids.length == 0) return;
        Type type = ObjectUtils.tryCast(list.getSelectedValue(), Type.class);
        if (type != null) {
          if (psiElement.isValid()) {
            execute(type.getExecutor(), psiElement);
          }
        }
      }).
      createPopup();

    popup.show(new RelativePoint(e));
  }

  @NotNull
  private static Type[] getAvailableTypes() {
    List<Type> types = ContainerUtil.filter(Type.values(), type -> type.isAvailable());
    return types.toArray(new Type[types.size()]);
  }

  private static void execute(@NotNull Executor executor, @NotNull final PsiElement element) {
    Project project = element.getProject();
    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    if (editor == null) {
      return;
    }
    DataContext dataContext = createDataContext(editor, element);
    RunConfigurationProducer jstdOriginalProducer = getJstdRunConfigurationProducer();
    ConfigurationContext context = ConfigurationContext.getFromContext(dataContext);
    boolean created = false;
    RunnerAndConfigurationSettings configuration = jstdOriginalProducer.findExistingConfiguration(context);
    if (configuration == null) {
      created = true;
      ConfigurationFromContext fromContext = jstdOriginalProducer.createConfigurationFromContext(context);
      if (fromContext != null) {
        configuration = fromContext.getConfigurationSettings();
      }
      else {
        return;
      }
    }

    execute(project, executor, configuration, created);
  }

  private static DataContext createDataContext(@NotNull Editor editor, @NotNull final PsiElement element) {
    final DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());
    return new DataContext() {
      @Nullable
      @Override
      public Object getData(@NonNls String dataId) {
        if (Location.DATA_KEY.is(dataId)) {
          return new PsiLocation<>(element.getProject(), element);
        }
        return dataContext.getData(dataId);
      }
    };
  }

  @Nullable
  private static RunConfigurationProducer getJstdRunConfigurationProducer() {
    return RunConfigurationProducer.getInstance(JstdRunConfigurationProducer.class);
  }

  private static void execute(@NotNull Project project,
                              @NotNull Executor executor,
                              @NotNull RunnerAndConfigurationSettings configuration,
                              boolean created) {
    RunManager runManager = RunManager.getInstance(project);
    if (created) {
      runManager.setTemporaryConfiguration(configuration);
    }
    runManager.setSelectedConfiguration(configuration);
    ExecutionEnvironmentBuilder builder = ExecutionEnvironmentBuilder.createOrNull(executor, configuration);
    if (builder != null) {
      ExecutionManager.getInstance(project).restartRunProfile(builder.build());
    }
  }

  private enum Type {
    RUN {
      @Override
      boolean isAvailable() {
        return true;
      }

      @NotNull
      @Override
      Icon getIcon() {
        return AllIcons.Toolwindows.ToolWindowRun;
      }

      @Override
      String getTitle(@NotNull String displayName) {
        return "Run '" + displayName + "'";
      }

      @NotNull
      @Override
      Executor getExecutor() {
        return DefaultRunExecutor.getRunExecutorInstance();
      }
    },
    DEBUG {
      @Override
      boolean isAvailable() {
        return JstdDebugProgramRunner.Companion.isAvailable();
      }

      @NotNull
      @Override
      Icon getIcon() {
        return AllIcons.Toolwindows.ToolWindowDebugger;
      }

      @Override
      String getTitle(@NotNull String displayName) {
        return "Debug '" + displayName + "'";
      }

      @NotNull
      @Override
      Executor getExecutor() {
        return DefaultDebugExecutor.getDebugExecutorInstance();
      }
    };

    abstract boolean isAvailable();

    @NotNull
    abstract Icon getIcon();

    abstract String getTitle(@NotNull String displayName);

    @NotNull
    abstract Executor getExecutor();
  }

}
