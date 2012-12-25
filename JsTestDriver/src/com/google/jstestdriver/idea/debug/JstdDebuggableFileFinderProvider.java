package com.google.jstestdriver.idea.debug;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.config.ParsedConfiguration;
import com.google.jstestdriver.config.ResolvedConfiguration;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.idea.util.JstdConfigParsingUtils;
import com.google.jstestdriver.model.BasePaths;
import com.intellij.execution.ExecutionException;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;

/**
 * @author Sergey Simonchik
 */
public class JstdDebuggableFileFinderProvider {
  private static final Logger LOG = Logger.getInstance(JstdDebuggableFileFinderProvider.class);

  private final File myConfigFile;

  public JstdDebuggableFileFinderProvider(@NotNull File configFile) {
    myConfigFile = configFile;
  }

  @NotNull
  public RemoteDebuggingFileFinder provideFileFinder() throws ExecutionException {
    ResolvedConfiguration resolvedConfiguration = resolveConfiguration();
    BiMap<String, VirtualFile> mappings = HashBiMap.create();
    addAllRemoteUrlMappings(resolvedConfiguration.getTests(), mappings);
    addAllRemoteUrlMappings(resolvedConfiguration.getFilesList(), mappings);
    return new RemoteDebuggingFileFinder(mappings, false);
  }

  @NotNull
  private ResolvedConfiguration resolveConfiguration() throws ExecutionException {
    VirtualFile configVirtualFile = VfsUtil.findFileByIoFile(myConfigFile, false);
    if (configVirtualFile == null) {
      throw new ExecutionException("Cannot find JsTestDriver configuration file " + myConfigFile.getAbsolutePath());
    }
    BasePaths dirBasePaths = new BasePaths(myConfigFile.getParentFile());
    final byte[] content;
    try {
      content = configVirtualFile.contentsToByteArray();
    }
    catch (IOException e) {
      throw new ExecutionException("Cannot read JsTestDriver configuration file " + configVirtualFile.getPath());
    }
    Reader reader = new InputStreamReader(new ByteArrayInputStream(content), Charset.defaultCharset());
    try {
      YamlParser yamlParser = new YamlParser();
      ParsedConfiguration parsedConfiguration = (ParsedConfiguration) yamlParser.parse(reader, dirBasePaths);
      JstdConfigParsingUtils.wipeCoveragePlugin(parsedConfiguration);
      return JstdConfigParsingUtils.resolveConfiguration(parsedConfiguration);
    }
    catch (Exception e) {
      String message = "Malformed JsTestDriver configuration file " + configVirtualFile.getPath();
      LOG.warn(message, e);
      throw new ExecutionException(message);
    }
    finally {
      try {
        reader.close();
      }
      catch (IOException ignored) {
      }
    }
  }

  private static void addAllRemoteUrlMappings(@NotNull Collection<FileInfo> filesInfo, @NotNull BiMap<String, VirtualFile> mappings) {
    LocalFileSystem fileSystem = LocalFileSystem.getInstance();
    for (FileInfo fileInfo : filesInfo) {
      String displayPath = fileInfo.getDisplayPath();
      File file = fileInfo.toFile();
      if (StringUtil.isNotEmpty(displayPath) && file.isFile()) {
        VirtualFile virtualFile = fileSystem.findFileByIoFile(file);
        if (virtualFile != null) {
          String remotePath = "http://localhost:9876/test/" + displayPath;
          mappings.put(remotePath, virtualFile);
        }
      }
    }
  }
}