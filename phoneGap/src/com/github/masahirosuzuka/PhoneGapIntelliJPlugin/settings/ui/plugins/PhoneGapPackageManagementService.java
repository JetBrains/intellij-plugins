package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.ui.plugins;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapCommandLine;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.commandLine.PhoneGapPluginsList;
import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.settings.ui.PhoneGapConfigurable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.util.CatchingConsumer;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.packaging.InstalledPackage;
import com.intellij.webcore.packaging.PackageManagementServiceEx;
import com.intellij.webcore.packaging.RepoPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class PhoneGapPackageManagementService extends PackageManagementServiceEx {
  private final PhoneGapCommandLine myCommands;
  private PhoneGapConfigurable.RepositoryStore myRepositoryStore;

  public PhoneGapPackageManagementService(@NotNull PhoneGapCommandLine commandLine,
                                          @NotNull PhoneGapConfigurable.RepositoryStore repositoryStore) {
    myRepositoryStore = repositoryStore;
    myCommands = commandLine;
  }

  @Nullable
  @Override
  public List<String> getAllRepositories() {
    return myRepositoryStore.getRepositories();
  }

  @Override
  public boolean canModifyRepository(String repositoryUrl) {
    return !PhoneGapPluginsList.PLUGINS_URL.equals(repositoryUrl);
  }

  @Override
  public void addRepository(String repositoryUrl) {
    myRepositoryStore.addRepository(repositoryUrl);
  }

  @Override
  public void removeRepository(String repositoryUrl) {
    myRepositoryStore.remove(repositoryUrl);
  }

  @Override
  public List<RepoPackage> getAllPackages() throws IOException {

    List<RepoPackage> packages = PhoneGapPluginsList.listCached();
    List<String> repositories = getAllRepositories();
    assert repositories != null;

    packages.addAll(PhoneGapPluginsList.wrapRepo(repositories));
    return packages;
  }

  @Override
  public List<RepoPackage> reloadAllPackages() throws IOException {
    PhoneGapPluginsList.resetCache();
    return getAllPackages();
  }

  @Override
  public Collection<InstalledPackage> getInstalledPackages() throws IOException {
    List<String> names = myCommands.pluginList();


    return PhoneGapPluginsList.wrapInstalled(names);
  }

  @Override
  public List<RepoPackage> getAllPackagesCached() {
    try {
      return getAllPackages();
    }
    catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public void installPackage(final RepoPackage repoPackage,
                             @Nullable final String version,
                             boolean forceUpgrade,
                             @Nullable String extraOptions,
                             final Listener listener,
                             boolean installToUser) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        listener.operationStarted(repoPackage.getName());
        final Ref<String> errorMessage = new Ref<String>();
        try {
          String appendVersion = version == null ? "" : "@" + version;
          myCommands.pluginAdd(repoPackage.getName() + appendVersion);
        }
        catch (Exception e) {
          String message = e.getMessage();
          errorMessage.set(message == null ? e.toString() : message);
        }
        finally {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              listener.operationFinished(repoPackage.getName(), ErrorDescription.fromMessage(errorMessage.get()));
              scheduleFileSystemRefresh();
            }
          }, ModalityState.any());
        }
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
          public boolean process(final InstalledPackage aPackage) {
            listener.operationStarted(aPackage.getName());
            final Ref<String> errorMessage = new Ref<String>();
            try {
              myCommands.pluginRemove(aPackage.getName());
            }
            catch (Exception e) {
              errorMessage.set(e.getMessage() == null ? e.toString() : e.getMessage());
            }
            finally {
              ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                  listener.operationFinished(aPackage.getName(), ErrorDescription.fromMessage(errorMessage.get()));
                  scheduleFileSystemRefresh();
                }
              }, ModalityState.any());
            }
            return true;
          }
        });
      }
    });
  }

  @Override
  public void fetchPackageVersions(String packageName, CatchingConsumer<List<String>, Exception> consumer) {
    PhoneGapPluginsList.PhoneGapRepoPackage aPackage = PhoneGapPluginsList.getPackage(packageName);
    if (aPackage == null) {
      consumer.consume(ContainerUtil.<String>emptyList());
      return;
    }
    consumer.consume(ContainerUtil.newArrayList(aPackage.getLatestVersion()));
  }

  @Override
  public void fetchPackageDetails(String packageName, CatchingConsumer<String, Exception> consumer) {
    PhoneGapPluginsList.PhoneGapRepoPackage aPackage = PhoneGapPluginsList.getPackage(packageName);
    consumer.consume(aPackage != null ? aPackage.getDesc() : "");
  }

  private static void scheduleFileSystemRefresh() {
    ApplicationManager.getApplication().assertIsDispatchThread();
    LocalFileSystem.getInstance().refresh(true);
  }

  @Override
  public void updatePackage(final @NotNull InstalledPackage repoPackage,
                            final @Nullable String version,
                            final @NotNull Listener listener) {
    installPackage(new RepoPackage(repoPackage.getName(), ""), version, false, null, listener, false);
  }

  @Override
  public boolean shouldFetchLatestVersionsForOnlyInstalledPackages() {
    return true;
  }

  @Override
  public void fetchLatestVersion(@NotNull InstalledPackage pkg, @NotNull CatchingConsumer<String, Exception> consumer) {
    PhoneGapPluginsList.PhoneGapRepoPackage aPackage = PhoneGapPluginsList.getPackage(pkg.getName());

    consumer.consume(aPackage == null ? null : aPackage.getLatestVersion());
  }
}
