package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.TestFileStructureManager;
import com.google.jstestdriver.idea.assertFramework.TestFileStructurePack;
import com.google.jstestdriver.idea.assertFramework.library.JstdLibraryUtil;
import com.google.jstestdriver.idea.execution.JstdRuntimeConfigurationProducer;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.execution.*;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    VirtualFile virtualFile = jsFile.getVirtualFile();
    if (virtualFile == null) {
      return null;
    }
    boolean inScope = JstdLibraryUtil.isFileInJstdLibScope(project, virtualFile);
    if (!inScope) {
      return null;
    }
    TestFileStructurePack pack = TestFileStructureManager.fetchTestFileStructurePackByJsFile(jsFile);
    if (pack == null) {
      return null;
    }
    String testElementName = element.getUserData(JstdTestFileStructure.TEST_ELEMENT_NAME_KEY);
    if (testElementName != null) {
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
    return new LineMarkerInfo<PsiElement>(
      testElement,
      testElement.getTextRange(),
      AllIcons.General.Run,
      Pass.UPDATE_ALL,
      new Function<PsiElement, String>() {
        @Override
        public String fun(PsiElement element) {
          return "Run '" + displayName + "'";
        }
      },
      new GutterIconNavigationHandler<PsiElement>() {
        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
          if (elt.isValid()) {
            run(elt);
          }
        }
      },
      GutterIconRenderer.Alignment.CENTER
    );
  }

  private static void run(@NotNull PsiElement element) {
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

    Executor runExecutor = DefaultRunExecutor.getRunExecutorInstance();
    if (runExecutor != null) {
      execute(project, runExecutor, configuration, created);
    }
  }

  @Nullable
  private static JstdRuntimeConfigurationProducer getJstdRuntimeConfigurationProducer() {
    RuntimeConfigurationProducer[] configurationProducers =
      ApplicationManager.getApplication().getExtensions(RuntimeConfigurationProducer.RUNTIME_CONFIGURATION_PRODUCER);
    for (RuntimeConfigurationProducer producer : configurationProducers) {
      if (producer instanceof JstdRuntimeConfigurationProducer) {
        return (JstdRuntimeConfigurationProducer) producer;
      }
    }
    return null;
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
                                                              null);
    }
    else {
      ProgramRunnerUtil.executeConfiguration(project, configuration, executor);
    }
  }

}
