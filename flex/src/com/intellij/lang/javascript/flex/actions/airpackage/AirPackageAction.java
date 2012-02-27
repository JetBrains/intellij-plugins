package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.build.FlexCompiler;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import gnu.trove.THashSet;

import java.util.Collection;
import java.util.Set;

public class AirPackageAction extends DumbAwareAction {

  public void update(final AnActionEvent e) {
    final Project project = e.getProject();

    boolean flexModulePresent = false;
    boolean airAppPresent = false;

    if (project != null) {
      final FlexModuleType flexModuleType = FlexModuleType.getInstance();

      MODULES_LOOP:
      for (Module module : ModuleManager.getInstance(project).getModules()) {
        if (ModuleType.get(module) == flexModuleType) {
          flexModulePresent = true;

          for (FlexIdeBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
            final BuildConfigurationNature nature = bc.getNature();
            if (nature.isApp() && !nature.isWebPlatform()) {
              airAppPresent = true;
              break MODULES_LOOP;
            }
          }
        }
      }
    }

    e.getPresentation().setVisible(flexModulePresent);
    e.getPresentation().setEnabled(airAppPresent &&
                                   !CompilerManager.getInstance(project).isCompilationActive() &&
                                   !AirPackageParameters.getInstance(project).isPackagingInProgress());
  }

  public void actionPerformed(final AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return;

    final AirPackageDialog dialog = new AirPackageDialog(project);
    dialog.show();

    if (!dialog.isOK()) return;

    final Collection<Pair<Module, FlexIdeBuildConfiguration>> modulesAndBCs = dialog.getSelectedBCs();
    final Set<Module> modules = new THashSet<Module>();
    for (Pair<Module, FlexIdeBuildConfiguration> bc : modulesAndBCs) {
      modules.add(bc.first);
    }

    final CompilerManager compilerManager = CompilerManager.getInstance(project);
    final CompileScope compileScope = compilerManager.createModulesCompileScope(modules.toArray(new Module[modules.size()]), false);
    compileScope.putUserData(FlexCompiler.MODULES_AND_BCS_TO_COMPILE, modulesAndBCs);

    compilerManager.make(compileScope, new CompileStatusNotification() {
      public void finished(final boolean aborted, final int errors, final int warnings, final CompileContext compileContext) {
        if (!aborted && errors == 0) {
          createPackages(modulesAndBCs, compileContext);
        }
      }
    });
  }

  private static void createPackages(final Collection<Pair<Module, FlexIdeBuildConfiguration>> modulesAndBCs,
                                     final CompileContext compileContext) {
/*    ProgressManager.getInstance().run(new Task.Backgroundable(compileContext.getProject(), "Creating AIR package") {
      public void run(@NotNull final ProgressIndicator indicator) {
        try {
          AirPackageParameters.getInstance(compileContext.getProject()).setPackagingInProgress(true);
          Thread.sleep(10000);
        }
        catch (InterruptedException e) {

        }
        finally {
          AirPackageParameters.getInstance(compileContext.getProject()).setPackagingInProgress(false);
        }
      }
    });*/

    compileContext.addMessage(CompilerMessageCategory.ERROR,
                              "AIR application packaging is not supported yet. Please wait for the next IntelliJ IDEA EAP.", null, -1, -1);
  }

  /*
  private static ExternalTask createAirInstallerTask(final Project project, final AirInstallerParameters parameters) {
    return new AdtTask(project, parameters.getFlexSdk()) {
      protected void appendAdtOptions(List<String> command) {
        command.add(parameters.DO_NOT_SIGN ? "-prepare" : "-package");
        if (!parameters.DO_NOT_SIGN) {
          appendSigningOptions(command, parameters);
        }
        appendPaths(command, parameters);
      }
    };
  }
  */
}
