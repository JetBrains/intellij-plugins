package jetbrains.plugins.yeoman.generators;

import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import jetbrains.plugins.yeoman.settings.YeomanGlobalSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class YeomanInstalledGeneratorListProvider {
  public static final String GENERATOR_PREFIX = "generator-";

  public static YeomanInstalledGeneratorListProvider getProvider() {
    return new YeomanInstalledGeneratorListProvider();
  }

  private static List<VirtualFile> getGlobalInstalledGeneratorFiles() {
    NodeJsLocalInterpreter interpreter = YeomanGlobalSettings.getInstance().getInterpreter();
    if (interpreter == null) return ContainerUtil.emptyList();


    List<VirtualFile> possibleCandidates = new ArrayList<>();
    final VirtualFile virtualFile = interpreter.getGlobalNodeModulesVirtualDir();
    if (virtualFile == null) return ContainerUtil.emptyList();
    for (VirtualFile file : virtualFile.getChildren()) {
      if (file.isValid() && file.isDirectory() && file.getName().startsWith(GENERATOR_PREFIX)) {
        possibleCandidates.add(file);
      }
    }

    return possibleCandidates;
  }

  public List<YeomanInstalledGeneratorInfo> getAllInstalledGenerators() {
    return ContainerUtil.concat(getGlobalInstalledGenerators(), getLocalInstalledGenerators());
  }


  @NotNull
  private static List<YeomanInstalledGeneratorInfo> getGlobalInstalledGenerators() {
    return ContainerUtil.map(getGlobalInstalledGeneratorFiles(), file -> new YeomanInstalledGeneratorInfo(file));
  }

  @NotNull
  private static List<YeomanInstalledGeneratorInfo> getLocalInstalledGenerators() {
    final File file = new File(PathManager.getSystemPath(), YeomanGeneratorInstaller.LOCAL_GENERATORS_RELATIVE_PATH);
    if (!file.exists()) return ContainerUtil.emptyList();

    final File[] files = file.listFiles();
    if (files == null) return ContainerUtil.emptyList();


    List<File> fileList = ContainerUtil.filter(files, file12 -> file12.isDirectory() && file12.getName().startsWith("generator-"));

    return ContainerUtil.map(fileList, file1 -> new YeomanInstalledGeneratorInfo(file1));
  }


  @Nullable
  public YeomanInstalledGeneratorInfo getGeneratorByYoName(@NotNull String name) {
    for (YeomanInstalledGeneratorInfo info : getAllInstalledGenerators()) {
      if (name.equals(info.getYoName())) {
        return info;
      }
    }

    return null;
  }
}
