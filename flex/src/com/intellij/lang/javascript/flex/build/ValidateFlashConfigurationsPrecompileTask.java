package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.CompilerWorkspaceConfiguration;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.BuildConfigurationEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.DependencyEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.ui.CompositeConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexBCConfigurable;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import com.intellij.openapi.compiler.CompilerBundle;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureProblemType;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Trinity;
import com.intellij.pom.Navigatable;
import com.intellij.ui.navigation.Place;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.util.Collection;
import java.util.Set;

public class ValidateFlashConfigurationsPrecompileTask implements CompileTask {

  private static final String FLASH_COMPILER_GROUP_ID = "Flash Compiler";

  private boolean myParallelCompilationSuggested = false;

  public boolean execute(final CompileContext context) {
    if (CompilerWorkspaceConfiguration.getInstance(context.getProject()).useOutOfProcessBuild()) {
      FlexCompilerHandler.getInstance(context.getProject()).getBuiltInFlexCompilerHandler().stopCompilerProcess();
      return validateConfiguration(context);
    }

    return true;
  }

  private boolean validateConfiguration(final CompileContext context) {
    try {
      final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile =
        FlexCompiler.getModulesAndBCsToCompile(context.getCompileScope());

      suggestParallelCompilationIfNeeded(context.getProject(), modulesAndBCsToCompile);

      final Collection<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> problems = ApplicationManager.getApplication()
        .runReadAction(new Computable<Collection<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>>>() {
          public Collection<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> compute() {
            return FlexCompiler.getProblems(context.getCompileScope(), modulesAndBCsToCompile);
          }
        });

      if (!problems.isEmpty()) {
        boolean hasErrors = false;
        for (Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem> problem : problems) {
          if (problem.getThird().severity == ProjectStructureProblemType.Severity.ERROR) {
            hasErrors = true;
            break;
          }
        }

        if (hasErrors) {
          // todo remove this senseless error message when 'show first error in editor' functionality respect canNavigateToSource()
          context.addMessage(CompilerMessageCategory.ERROR,
                             "Flash build configurations contain errors. " +
                             "Double-click error message below to navigate to the corresponding field in the Project Structure dialog",
                             null, -1, -1);
        }

        reportProblems(context, problems);
        return !hasErrors;
      }
    }
    catch (ConfigurationException e) {
      context.addMessage(CompilerMessageCategory.ERROR, FlexBundle.message("project.setup.problem", e.getMessage()), null, -1, -1);
      return false;
    }

    return true;
  }

  private void suggestParallelCompilationIfNeeded(final Project project,
                                                  final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile) {
    if (myParallelCompilationSuggested) return;
    if (CompilerWorkspaceConfiguration.getInstance(project).PARALLEL_COMPILATION) return;
    if (modulesAndBCsToCompile.size() < 2) return;
    if (!independentBCsExist(modulesAndBCsToCompile)) return;

    final NotificationListener listener = new NotificationListener() {
      public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
        notification.expire();

        if ("enable".equals(event.getDescription())) {
          CompilerWorkspaceConfiguration.getInstance(project).PARALLEL_COMPILATION = true;

          final NotificationListener listener1 = new NotificationListener() {
            public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
              notification.expire();
              ShowSettingsUtil.getInstance().showSettingsDialog(project, CompilerBundle.message("compiler.configurable.display.name"));
            }
          };
          new Notification(FLASH_COMPILER_GROUP_ID, FlexBundle.message("parallel.compilation.enabled"),
                           FlexBundle.message("see.settings.compiler"), NotificationType.INFORMATION, listener1).notify(project);
        }
        else if ("open".equals(event.getDescription())) {
          ShowSettingsUtil.getInstance().showSettingsDialog(project, CompilerBundle.message("compiler.configurable.display.name"));
        }
      }
    };

    new Notification(FLASH_COMPILER_GROUP_ID, FlexBundle.message("parallel.compilation.hint.title"),
                     FlexBundle.message("parallel.compilation.hint"), NotificationType.INFORMATION, listener).notify(project);

    myParallelCompilationSuggested = true;
  }

  private static boolean independentBCsExist(final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile) {
    final Set<FlexBuildConfiguration> bcs = new THashSet<FlexBuildConfiguration>();

    for (Pair<Module, FlexBuildConfiguration> moduleAndBC : modulesAndBCsToCompile) {
      bcs.add(moduleAndBC.second);
    }

    int independentBCsCount = 0;

    OUTER:
    for (FlexBuildConfiguration bc : bcs) {
      for (final DependencyEntry entry : bc.getDependencies().getEntries()) {
        if (entry instanceof BuildConfigurationEntry) {
          final FlexBuildConfiguration dependencyBC = ((BuildConfigurationEntry)entry).findBuildConfiguration();
          if (dependencyBC != null && bcs.contains(dependencyBC)) {
            continue OUTER;
          }
        }
      }

      independentBCsCount++;
      if (independentBCsCount > 1) {
        return true;
      }
    }

    return false;
  }

  private static void reportProblems(final CompileContext context,
                                     final Collection<Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem>> problems) {
    for (Trinity<Module, FlexBuildConfiguration, FlashProjectStructureProblem> trinity : problems) {
      final Module module = trinity.getFirst();
      final FlexBuildConfiguration bc = trinity.getSecond();
      final FlashProjectStructureProblem problem = trinity.getThird();

      final String message = problem instanceof FlashProjectStructureProblem.FlexUnitOutputFolderProblem
                             ? problem.errorMessage
                             : FlexBundle.message("bc.0.module.1.problem.2", bc.getName(), module.getName(), problem.errorMessage);
      final CompilerMessageCategory severity = problem.severity == ProjectStructureProblemType.Severity.ERROR
                                               ? CompilerMessageCategory.ERROR
                                               : CompilerMessageCategory.WARNING;
      context.addMessage(severity, message, null, -1, -1, new BCProblemNavigatable(module, bc.getName(), problem));
    }
  }

  private static class BCProblemNavigatable implements Navigatable {
    @NotNull private final Module myModule;
    @NotNull private final String myBCNme;
    @NotNull private final FlashProjectStructureProblem myProblem;

    private BCProblemNavigatable(final @NotNull Module module,
                                 final @NotNull String bcName,
                                 final @NotNull FlashProjectStructureProblem problem) {
      myModule = module;
      myBCNme = bcName;
      myProblem = problem;
    }

    public boolean canNavigateToSource() {
      return false;
    }

    public boolean canNavigate() {
      return !myModule.isDisposed() && FlexBuildConfigurationManager.getInstance(myModule).findConfigurationByName(myBCNme) != null;
    }

    public void navigate(final boolean requestFocus) {
      final ProjectStructureConfigurable configurable = ProjectStructureConfigurable.getInstance(myModule.getProject());

      ShowSettingsUtil.getInstance().editConfigurable(myModule.getProject(), configurable, new Runnable() {
        public void run() {
          final Place place;

          if (myProblem instanceof FlashProjectStructureProblem.FlexUnitOutputFolderProblem) {
            place = new Place()
              .putPath(ProjectStructureConfigurable.CATEGORY, configurable.getProjectConfig());
          }
          else {
            place = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getPlaceFor(myModule, myBCNme)
              .putPath(CompositeConfigurable.TAB_NAME, myProblem.tabName)
              .putPath(FlexBCConfigurable.LOCATION_ON_TAB, myProblem.locationOnTab);
          }

          configurable.navigateTo(place, true);
        }
      });
    }
  }
}
