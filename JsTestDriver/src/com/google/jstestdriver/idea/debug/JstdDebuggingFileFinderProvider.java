package com.google.jstestdriver.idea.debug;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.config.ParsedConfiguration;
import com.google.jstestdriver.config.ResolvedConfiguration;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.idea.rt.util.JstdConfigParsingUtils;
import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.model.BasePaths;
import com.intellij.execution.ExecutionException;
import com.intellij.javascript.debugger.RemoteDebuggingFileFinder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.UriUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;

public class JstdDebuggingFileFinderProvider {
  private static final Logger LOG = Logger.getInstance(JstdDebuggingFileFinderProvider.class);

  private final File myConfigFile;
  private final JstdServer myServer;

  public JstdDebuggingFileFinderProvider(@NotNull File configFile, @NotNull JstdServer server) {
    myConfigFile = configFile;
    myServer = server;
  }

  @NotNull
  public RemoteDebuggingFileFinder provideFileFinder() throws ExecutionException {
    ResolvedConfiguration resolvedConfiguration = resolveConfiguration();
    BiMap<String, VirtualFile> mappings = HashBiMap.create();
    addAllRemoteUrlMappings(resolvedConfiguration.getTests(), mappings);
    addAllRemoteUrlMappings(resolvedConfiguration.getFilesList(), mappings);
    return new RemoteDebuggingFileFinder(mappings, null);
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

  private void addAllRemoteUrlMappings(@NotNull Collection<FileInfo> filesInfo, @NotNull BiMap<String, VirtualFile> map) {
    LocalFileSystem fileSystem = LocalFileSystem.getInstance();
    for (FileInfo fileInfo : filesInfo) {
      String displayPath = fileInfo.getDisplayPath();
      File file = fileInfo.toFile();
      if (StringUtil.isNotEmpty(displayPath) && file.isFile()) {
        VirtualFile virtualFile = fileSystem.findFileByIoFile(file);
        if (virtualFile != null) {
          String url = "http://127.0.0.1:" + myServer.getSettings().getPort() + "/test/" + UriUtil.trimLeadingSlashes(displayPath);
          map.forcePut(url, virtualFile);
        }
      }
    }
  }

}
