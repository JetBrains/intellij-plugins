package com.jetbrains.lang.dart.util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairConsumer;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Dumper;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class PubspecYamlUtil {

  public static final String PUBSPEC_YAML = "pubspec.yaml";

  private static final String NAME = "name";
  public static final String DEPENDENCIES = "dependencies";
  public static final String DEV_DEPENDENCIES = "dev_dependencies";
  public static final String DEPENDENCY_OVERRIDES = "dependency_overrides";
  public static final String PATH = "path";

  public static final String LIB_DIR_NAME = "lib";

  private static final Key<Pair<Long, Map<String, Object>>> MOD_STAMP_TO_PUBSPEC_NAME = Key.create("MOD_STAMP_TO_PUBSPEC_NAME");

  @Nullable
  public static VirtualFile findPubspecYamlFile(@NotNull final Project project, @NotNull final VirtualFile contextFile) {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    VirtualFile current = contextFile;
    VirtualFile parent = contextFile.isDirectory() ? contextFile : contextFile.getParent();

    while (parent != null && (LIB_DIR_NAME.equals(current.getName()) || fileIndex.isInContent(parent))) {
      current = parent;
      final VirtualFile file = parent.findChild(PUBSPEC_YAML);
      if (file != null && !file.isDirectory()) {
        return file;
      }
      parent = current.getParent();
    }

    return null;
  }

  @Nullable
  public static String getDartProjectName(@NotNull final VirtualFile pubspecYamlFile) {
    final Map<String, Object> yamlInfo = getPubspecYamlInfo(pubspecYamlFile);
    final Object name = yamlInfo == null ? null : yamlInfo.get(NAME);
    return name instanceof String ? (String)name : null;
  }

  public static void processInProjectPathPackagesRecursively(@NotNull final Project project,
                                                             @NotNull final VirtualFile pubspecYamlFile,
                                                             @NotNull final PairConsumer<String, VirtualFile> pathPackageNameAndDirConsumer) {

    processInProjectPathPackagesRecursively(project, pubspecYamlFile, new THashSet<>(), pathPackageNameAndDirConsumer);
  }

  private static void processInProjectPathPackagesRecursively(@NotNull final Project project,
                                                              @NotNull final VirtualFile pubspecYamlFile,
                                                              @NotNull final Set<VirtualFile> processedPubspecs,
                                                              @NotNull final PairConsumer<String, VirtualFile> pathPackageNameAndDirConsumer) {
    if (!processedPubspecs.add(pubspecYamlFile)) return;

    final VirtualFile baseDir = pubspecYamlFile.getParent();
    final Map<String, Object> yamlInfo = getPubspecYamlInfo(pubspecYamlFile);
    if (baseDir == null || yamlInfo == null) return;

    processYamlDepsRecursively(project, processedPubspecs, pathPackageNameAndDirConsumer, baseDir, yamlInfo.get(DEPENDENCIES));
    processYamlDepsRecursively(project, processedPubspecs, pathPackageNameAndDirConsumer, baseDir, yamlInfo.get(DEV_DEPENDENCIES));
    processYamlDepsRecursively(project, processedPubspecs, pathPackageNameAndDirConsumer, baseDir, yamlInfo.get(DEPENDENCY_OVERRIDES));
  }

  // Path packages: https://www.dartlang.org/tools/pub/dependencies.html#path-packages
  private static void processYamlDepsRecursively(@NotNull final Project project,
                                                 @NotNull final Set<VirtualFile> processedPubspecs,
                                                 @NotNull final PairConsumer<String, VirtualFile> pathPackageNameAndRelPathConsumer,
                                                 @NotNull final VirtualFile baseDir,
                                                 @Nullable final Object yamlDep) {
    // see com.google.dart.tools.core.pub.PubspecModel#processDependencies
    if (!(yamlDep instanceof Map)) return;

    //noinspection unchecked
    for (Map.Entry<String, Object> packageEntry : ((Map<String, Object>)yamlDep).entrySet()) {
      final String packageName = packageEntry.getKey();

      final Object packageEntryValue = packageEntry.getValue();
      if (packageEntryValue instanceof Map) {
        final Object pathObj = ((Map)packageEntryValue).get(PATH);
        if (pathObj instanceof String) {
          final VirtualFile packageFolder = VfsUtilCore.findRelativeFile(pathObj + "/" + LIB_DIR_NAME, baseDir);
          if (packageFolder != null &&
              packageFolder.isDirectory() &&
              ProjectRootManager.getInstance(project).getFileIndex().isInContent(packageFolder)) {
            pathPackageNameAndRelPathConsumer.consume(packageName, packageFolder);

            final VirtualFile otherPubspecYaml = packageFolder.getParent().findChild(PUBSPEC_YAML);
            if (otherPubspecYaml != null && !otherPubspecYaml.isDirectory()) {
              processInProjectPathPackagesRecursively(project, otherPubspecYaml, processedPubspecs, pathPackageNameAndRelPathConsumer);
            }
          }
        }
      }
    }
  }

  @Nullable
  private static Map<String, Object> getPubspecYamlInfo(final @NotNull VirtualFile pubspecYamlFile) {
    // do not use Yaml plugin here - IntelliJ IDEA Community Edition doesn't contain it.
    Pair<Long, Map<String, Object>> data = pubspecYamlFile.getUserData(MOD_STAMP_TO_PUBSPEC_NAME);

    final FileDocumentManager documentManager = FileDocumentManager.getInstance();
    final Document cachedDocument = documentManager.getCachedDocument(pubspecYamlFile);
    final Long currentTimestamp = cachedDocument != null ? cachedDocument.getModificationStamp() : pubspecYamlFile.getModificationCount();
    final Long cachedTimestamp = data == null ? null : data.first;

    if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
      data = null;
      pubspecYamlFile.putUserData(MOD_STAMP_TO_PUBSPEC_NAME, null);
      try {
        final Map<String, Object> pubspecYamlInfo;
        if (cachedDocument != null) {
          pubspecYamlInfo = loadPubspecYamlInfo(cachedDocument.getText());
        }
        else {
          pubspecYamlInfo = loadPubspecYamlInfo(VfsUtilCore.loadText(pubspecYamlFile));
        }

        if (pubspecYamlInfo != null) {
          data = Pair.create(currentTimestamp, pubspecYamlInfo);
          pubspecYamlFile.putUserData(MOD_STAMP_TO_PUBSPEC_NAME, data);
        }
      }
      catch (IOException ignored) {/* unlucky */}
    }

    return data == null ? null : data.second;
  }

  @Nullable
  private static Map<String, Object> loadPubspecYamlInfo(final @NotNull String pubspecYamlFileContents) {
    // see com.google.dart.tools.core.utilities.yaml.PubYamlUtils#parsePubspecYamlToMap()
    // deprecated constructor used to be compatible with old snakeyaml version in testng.jar (it wins when running from sources or tests)
    //noinspection deprecation
    final Yaml yaml = new Yaml(new Loader(new Constructor()), new Dumper(new Representer(), new DumperOptions()), new Resolver() {
      @Override
      protected void addImplicitResolvers() {
        addImplicitResolver(Tag.NULL, NULL, "~nN\0");
        addImplicitResolver(Tag.NULL, EMPTY, null);
        addImplicitResolver(Tag.VALUE, VALUE, "=");
        addImplicitResolver(Tag.MERGE, MERGE, "<");
      }
    });

    try {
      //noinspection unchecked
      return (Map<String, Object>)yaml.load(pubspecYamlFileContents);
    }
    catch (Exception e) {
      return null; // malformed yaml, e.g. because of typing in it
    }
  }
}
