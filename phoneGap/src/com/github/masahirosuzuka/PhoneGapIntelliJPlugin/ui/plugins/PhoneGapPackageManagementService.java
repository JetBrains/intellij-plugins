package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ui.plugins;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommands;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapPluginsList;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.CatchingConsumer;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.packaging.InstalledPackage;
import com.intellij.webcore.packaging.PackageManagementService;
import com.intellij.webcore.packaging.RepoPackage;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class PhoneGapPackageManagementService extends PackageManagementService {
  private final PhoneGapCommands myCommands;

  public PhoneGapPackageManagementService(Project project, String path) {
    myCommands = new PhoneGapCommands(path, project.getBasePath());
  }

  @Override
  public List<RepoPackage> getAllPackages() throws IOException {
    return PhoneGapPluginsList.listCached();
  }

  @Override
  public List<RepoPackage> reloadAllPackages() throws IOException {
    PhoneGapPluginsList.resetCache();
    return PhoneGapPluginsList.listCached();
  }

  @Override
  public Collection<InstalledPackage> getInstalledPackages() throws IOException {
    return PhoneGapPluginsList.wrapInstalled(myCommands.pluginList());
  }

  @Override
  public List<RepoPackage> getAllPackagesCached() {
    return PhoneGapPluginsList.listCached();
  }

  @Override
  public void installPackage(final RepoPackage repoPackage,
                             @Nullable String version,
                             boolean forceUpgrade,
                             @Nullable String extraOptions,
                             final Listener listener,
                             boolean installToUser) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        listener.operationStarted(repoPackage.getName());
        String errorMessage = null;
        try {
          myCommands.pluginAdd(repoPackage.getName());
        }
        catch (Exception e) {
          String message = e.getMessage();
          errorMessage = message == null ? e.toString() : message;
        }
        listener.operationFinished(repoPackage.getName(), errorMessage);
      }
    });
  }

  @Override
  public void uninstallPackages(final List<InstalledPackage> installedPackages, final Listener listener) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        ContainerUtil.process(installedPackages, new Processor<InstalledPackage>() {
          @Override
          public boolean process(InstalledPackage aPackage) {
            listener.operationStarted(aPackage.getName());
            try {
              myCommands.pluginRemove(aPackage.getName());
              listener.operationFinished(aPackage.getName(), null);
            }
            catch (Exception e) {
              String message = e.getMessage();
              listener.operationFinished(aPackage.getName(), message == null ? e.toString() : message);
            }
            return true;
          }
        });
      }
    });
  }

  @Override
  public void fetchPackageVersions(String packageName, CatchingConsumer<List<String>, Exception> consumer) {
    consumer.consume(ContainerUtil.newArrayList(""));
  }

  @Override
  public void fetchPackageDetails(String packageName, CatchingConsumer<String, Exception> consumer) {
    consumer.consume(PhoneGapPluginsList.getDescription(packageName));
  }
}
