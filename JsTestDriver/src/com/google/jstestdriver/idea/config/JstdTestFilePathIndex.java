package com.google.jstestdriver.idea.config;

import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FlagsImpl;
import com.google.jstestdriver.PathResolver;
import com.google.jstestdriver.config.ParsedConfiguration;
import com.google.jstestdriver.config.ResolvedConfiguration;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.hooks.FileParsePostProcessor;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.util.DisplayPathSanitizer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Sergey Simonchik
 */
public class JstdTestFilePathIndex extends FileBasedIndexExtension<String, Void> {

  private static final Logger LOG = Logger.getInstance(JstdTestFilePathIndex.class);

  private static final ID<String, Void> KEY = ID.create("jstd.jsFile.path");

  private static final FileBasedIndex.InputFilter JS_FILE_INPUT_FILTER = new FileBasedIndex.InputFilter() {
    @Override
    public boolean acceptInput(final VirtualFile file) {
      return JstdConfigFileType.INSTANCE == file.getFileType();
    }
  };

  private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

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
      public Map<String, Void> map(final FileContent inputData) {
        VirtualFile file = inputData.getFile();
        if (file.isValid()) {
          VirtualFile dir = file.getParent();
          if (dir.isValid()) {
            BasePaths basePaths = new BasePaths(new File(dir.getPath()));
            Reader reader = new InputStreamReader(new ByteArrayInputStream(inputData.getContent()), Charset.defaultCharset());
            try {
              return doIndexConfigFile(reader, basePaths);
            }
            catch (Exception e) {
              LOG.info("Can't index JsTD config file: " + file.getPath(), e);
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

  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return myKeyDescriptor;
  }

  @Override
  public DataExternalizer<Void> getValueExternalizer() {
    return ScalarIndexExtension.VOID_DATA_EXTERNALIZER;
  }

  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return JS_FILE_INPUT_FILTER;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @NotNull
  private static Map<String, Void> doIndexConfigFile(@NotNull Reader configFileReader, @NotNull BasePaths initialBasePaths) {
    YamlParser yamlParser = new YamlParser();
    final Map<String, Void> map = new THashMap<String, Void>();
    ParsedConfiguration parsedConfiguration = (ParsedConfiguration) yamlParser.parse(configFileReader, initialBasePaths);
    PathResolver pathResolver = new PathResolver(
      parsedConfiguration.getBasePaths(),
      Collections.<FileParsePostProcessor>emptySet(),
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

  @Nullable
  public static List<VirtualFile> findConfigFiles(@NotNull VirtualFile jsTestFile, @NotNull GlobalSearchScope scope) {
    final List<VirtualFile> jstdConfigs = new ArrayList<VirtualFile>();
    FileBasedIndex.getInstance().processValues(
      KEY,
      jsTestFile.getPath(),
      null,
      new FileBasedIndex.ValueProcessor<Void>() {
        @Override
        public boolean process(final VirtualFile file, final Void value) {
          jstdConfigs.add(file);
          return true;
        }
      },
      scope
    );
    return jstdConfigs;
  }
}
