package com.intellij.javascript.bower;

import com.google.common.base.Splitter;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.CatchingConsumer;
import com.intellij.util.ModalityUiUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.webcore.packaging.InstalledPackage;
import com.intellij.webcore.packaging.PackageManagementServiceEx;
import com.intellij.webcore.packaging.RepoPackage;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class BowerPackagingService extends PackageManagementServiceEx {
  private static final Logger LOG = Logger.getInstance(BowerPackagingService.class);

  private final Project myProject;
  private final BowerSettings mySettings;
  private final BowerPackageInfoManager myPackageInfoManager;

  public BowerPackagingService(@NotNull Project project, @NotNull BowerSettings settings) {
    myProject = project;
    mySettings = settings;
    myPackageInfoManager = new BowerPackageInfoManager(settings);
  }

  public @NotNull BowerSettings getSettings() {
    return mySettings;
  }

  @Override
  public @Nullable String getID() {
    return "Bower";
  }

  @Override
  public List<RepoPackage> getAllPackages() throws IOException {
    return doGetPackages(false);
  }

  @Override
  public List<RepoPackage> reloadAllPackages() throws IOException {
    return doGetPackages(true);
  }

  private List<RepoPackage> doGetPackages(boolean forceReload) throws IOException {
    List<String> packages = BowerAvailablePackagesManager.getInstance().getOrLoadAvailablePackages(mySettings, forceReload);
    return ContainerUtil.map(packages, packageName -> new RepoPackage(packageName, ""));
  }

  @Override
  public @NotNull List<? extends InstalledPackage> getInstalledPackagesList() throws ExecutionException {
    ProcessOutput output = runCommand("list", "--json");
    try {
      return BowerInstalledPackagesParser.parse(output.getStdout());
    }
    catch (IOException e) {
      throw new ExecutionException(e);
    }
  }

  @Override
  public void installPackage(final RepoPackage repoPackage,
                             final @Nullable String version,
                             boolean forceUpgrade, final @Nullable String extraOptions, final Listener listener, boolean installToUser) {
    ApplicationManager.getApplication().executeOnPooledThread(
      () -> doInstallPackage(repoPackage.getName(), version, extraOptions, listener));
  }

  // called in a pooled thread
  private void doInstallPackage(@NotNull String packageName,
                                @Nullable String version,
                                @Nullable String extraOptions,
                                @NotNull Listener listener) {
    listener.operationStarted(packageName);
    String endpoint = packageName;
    if (version != null) {
      endpoint += "#" + version;
    }
    if (extraOptions == null) {
      extraOptions = "--save";
    }
    List<String> args = new ArrayList<>(Arrays.asList("install", endpoint, "--force"));
    if (!extraOptions.trim().isEmpty()) {
      Iterable<String> extraArgs = Splitter.on(" ").trimResults().split(extraOptions);
      ContainerUtil.addAll(args, extraArgs);
    }
    try {
      String[] argArray = ArrayUtilRt.toStringArray(args);
      ProcessOutput output = runCommand(argArray);
      LOG.info("Package installation output: " + output.getStdout());
      listener.operationFinished(packageName, null);
      refreshBowerComponents();
    }
    catch (ExecutionException e) {
      listener.operationFinished(packageName, ErrorDescription.fromMessage(e.getMessage()));
    }
  }

  private void refreshBowerComponents() {
    ModalityUiUtil.invokeLaterIfNeeded(ModalityState.defaultModalityState(), () -> ApplicationManager.getApplication().runWriteAction(() -> {
      VirtualFile rootDir = ProjectUtil.guessProjectDir(myProject);
      VirtualFile bowerJson = VfsUtil.findFileByIoFile(new File(mySettings.getBowerJsonPath()), false);
      if (bowerJson != null && bowerJson.isValid()) {
        VirtualFile bowerJsonDir = bowerJson.getParent();
        if (bowerJsonDir != null && bowerJsonDir.isDirectory() && bowerJsonDir.isValid()) {
          rootDir = bowerJsonDir;
        }
      }
      if (rootDir != null) {
        rootDir.refresh(true, true);
      }
    }));
  }

  @Override
  public void uninstallPackages(List<? extends InstalledPackage> installedPackages, Listener listener) {
    List<BowerInstalledPackage> bowerInstalledPackages = new ArrayList<>();
    for (InstalledPackage installedPackage : installedPackages) {
      BowerInstalledPackage pkg = ObjectUtils.tryCast(installedPackage, BowerInstalledPackage.class);
      if (pkg != null) {
        bowerInstalledPackages.add(pkg);
      }
    }
    uninstallBowerPackages(bowerInstalledPackages, listener);
  }

  private void uninstallBowerPackages(final @NotNull List<? extends BowerInstalledPackage> packages, final @NotNull Listener listener) {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      for (BowerInstalledPackage pkg : packages) {
        doUninstallPackage(pkg, listener, true);
      }
    });
  }

  // called in a pooled thread
  private void doUninstallPackage(final BowerInstalledPackage pkg, final Listener listener, boolean refreshFsOnFinish) {
    listener.operationStarted(pkg.getName());
    try {
      runCommand("uninstall", "--save", pkg.getName());
      listener.operationFinished(pkg.getName(), null);
      if (refreshFsOnFinish) {
        refreshBowerComponents();
      }
    }
    catch (ExecutionException e) {
      listener.operationFinished(pkg.getName(), ErrorDescription.fromMessage(e.getMessage()));
    }
  }

  @Override
  public void fetchPackageVersions(String packageName, final CatchingConsumer<? super List<String>, ? super Exception> consumer) {
    myPackageInfoManager.fetchPackageInfo(new BowerPackageInfoManager.PackageInfoConsumer(packageName, true) {
      @Override
      public void onPackageInfo(@Nullable BowerPackageInfo packageInfo) {
        List<String> versions = Collections.emptyList();
        if (packageInfo != null) {
          versions = packageInfo.getVersions();
        }
        consumer.consume(versions);
      }

      @Override
      public void onException(@NotNull Exception e) {
        consumer.consume(e);
      }
    });
  }

  @Override
  public void fetchPackageDetails(String packageName, final CatchingConsumer<? super @Nls String, ? super Exception> consumer) {
    myPackageInfoManager.fetchPackageInfo(new BowerPackageInfoManager.PackageInfoConsumer(packageName, true) {
      @Override
      public void onPackageInfo(@Nullable BowerPackageInfo packageInfo) {
        String html = BowerBundle.message("bower.no_description_available.text");
        if (packageInfo != null) {
          html = packageInfo.formatHtmlDescription();
        }
        consumer.consume(html);
      }

      @Override
      public void onException(@NotNull Exception e) {
        consumer.consume(e);
      }
    });
  }

  @Override
  public void updatePackage(@NotNull InstalledPackage installedPackage,
                            final @Nullable String version,
                            final @NotNull Listener listener) {
    final BowerInstalledPackage pkg = ObjectUtils.tryCast(installedPackage, BowerInstalledPackage.class);
    if (pkg != null) {
      final String packageName = pkg.getName();
      listener.operationStarted(packageName);
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
        final Ref<ErrorDescription> errorDescriptionRef = Ref.create();
        Listener innerListener = new Listener() {
          @Override
          public void operationStarted(String packageName1) {
            // does nothing
          }

          @Override
          public void operationFinished(String packageName1, @Nullable ErrorDescription errorDescription) {
            errorDescriptionRef.set(errorDescription);
          }
        };
        listener.operationStarted(packageName);
        doUninstallPackage(pkg, innerListener, false);
        if (errorDescriptionRef.get() == null) {
          doInstallPackage(packageName, version, "--save", innerListener);
        }
        listener.operationFinished(packageName, errorDescriptionRef.get());
      });
    }
  }

  @Override
  public void fetchLatestVersion(@NotNull InstalledPackage pkg, @NotNull CatchingConsumer<? super String, ? super Exception> consumer) {
    String latestVersion = null;
    if (pkg instanceof BowerInstalledPackage bowerPkg) {
      latestVersion = bowerPkg.getLatestVersion();
    }
    consumer.consume(latestVersion);
  }

  public @NotNull ProcessOutput runCommand(String... commands) throws ExecutionException {
    return BowerCommandLineUtil.runBowerCommand(null, mySettings, commands);
  }
}
