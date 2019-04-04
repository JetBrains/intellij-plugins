package com.google.jstestdriver.idea.config;

import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FlagsImpl;
import com.google.jstestdriver.PathResolver;
import com.google.jstestdriver.config.ParsedConfiguration;
import com.google.jstestdriver.config.ResolvedConfiguration;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.util.DisplayPathSanitizer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.VoidDataExternalizer;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Sergey Simonchik
 */
public class JstdTestFilePathIndex extends FileBasedIndexExtension<String, Void> {

  private static final ID<String, Void> KEY = ID.create("jstd.jsFile.path");

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return KEY;
  }

  @NotNull
  @Override
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return new DataIndexer<String, Void, FileContent>() {
      @Override
      @NotNull
      public Map<String, Void> map(@NotNull final FileContent inputData) {
        VirtualFile file = inputData.getFile();
        if (file.isValid()) {
          VirtualFile dir = file.getParent();
          if (dir.isValid()) {
            BasePaths basePaths = new BasePaths(new File(dir.getPath()));
            Reader reader = new InputStreamReader(new ByteArrayInputStream(inputData.getContent()), Charset.defaultCharset());
            try {
              return doIndexConfigFile(reader, basePaths);
            }
            catch (Exception ignored) {
            }
            finally {
              try {
                reader.close();
              }
              catch (IOException ignored) {
              }
            }
          }
        }
        return Collections.emptyMap();
      }
    };
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @NotNull
  @Override
  public DataExternalizer<Void> getValueExternalizer() {
    return VoidDataExternalizer.INSTANCE;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(JstdConfigFileType.INSTANCE);
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return 1;
  }

  @NotNull
  private static Map<String, Void> doIndexConfigFile(@NotNull Reader configFileReader, @NotNull BasePaths initialBasePaths) {
    YamlParser yamlParser = new YamlParser();
    final Map<String, Void> map = new THashMap<>();
    ParsedConfiguration parsedConfiguration = (ParsedConfiguration) yamlParser.parse(configFileReader, initialBasePaths);
    PathResolver pathResolver = new PathResolver(
      parsedConfiguration.getBasePaths(),
      Collections.emptySet(),
      new DisplayPathSanitizer()
    );
    FlagsImpl flags = new FlagsImpl();
    flags.setServer("test:1");
    ResolvedConfiguration resolvedConfiguration = (ResolvedConfiguration) parsedConfiguration.resolvePaths(pathResolver, flags);
    doPutAll(map, resolvedConfiguration.getTests());
    doPutAll(map, resolvedConfiguration.getFilesList());
    return map;
  }

  private static void doPutAll(@NotNull Map<String, Void> map, @NotNull Collection<FileInfo> fileInfos) {
    for (FileInfo fileInfo : fileInfos) {
      File file = fileInfo.toFile();
      String path = FileUtil.toSystemIndependentName(file.getAbsolutePath());
      map.put(path, null);
    }
  }

  @NotNull
  public static List<VirtualFile> findConfigFilesInProject(@NotNull VirtualFile jsTestFile, @NotNull Project project) {
    GlobalSearchScope allScope = GlobalSearchScope.allScope(project);
    return findConfigFilesInScope(jsTestFile, allScope);
  }

  @NotNull
  public static List<VirtualFile> findConfigFilesInScope(@NotNull VirtualFile jsTestFile, @NotNull GlobalSearchScope scope) {
    final List<VirtualFile> jstdConfigs = new ArrayList<>(1);
    FileBasedIndex.getInstance().processValues(
      KEY,
      jsTestFile.getPath(),
      null,
      (file, value) -> {
        jstdConfigs.add(file);
        return true;
      },
      scope
    );
    return jstdConfigs;
  }
}
