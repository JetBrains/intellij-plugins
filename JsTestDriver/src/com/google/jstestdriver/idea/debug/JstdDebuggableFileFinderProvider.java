package com.google.jstestdriver.idea.debug;

import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.config.ParsedConfiguration;
import com.google.jstestdriver.config.ResolvedConfiguration;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.idea.util.JstdConfigParsingUtils;
import com.google.jstestdriver.model.BasePaths;
import com.intellij.execution.ExecutionException;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.javascript.debugger.execution.RemoteJavaScriptDebugConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JstdDebuggableFileFinderProvider {

  private static final Logger LOG = Logger.getInstance(JstdDebuggableFileFinderProvider.class);

  private final Project myProject;
  private final File myConfigFile;

  public JstdDebuggableFileFinderProvider(@NotNull Project project, @NotNull File configFile) {
    myProject = project;
    myConfigFile = configFile;
  }

  @NotNull
  public RemoteDebuggingFileFinder provideFileFinder() throws ExecutionException {
    List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> mapping = extractMappings();
    return new RemoteDebuggingFileFinder(myProject, mapping, false);
  }

  @NotNull
  private List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> extractMappings() throws ExecutionException {
    ResolvedConfiguration resolvedConfiguration = resolveConfiguration();
    List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> mappings = ContainerUtilRt.newArrayList();
    addAllRemoteUrlMappings(resolvedConfiguration.getTests(), mappings);
    addAllRemoteUrlMappings(resolvedConfiguration.getFilesList(), mappings);
    return mappings;
  }

  @NotNull
  private ResolvedConfiguration resolveConfiguration() throws ExecutionException {
    VirtualFile configVirtualFile = VfsUtil.findFileByIoFile(myConfigFile, false);
    if (configVirtualFile == null) {
      throw new ExecutionException("Can not find JsTestDriver configuration file " + myConfigFile.getAbsolutePath());
    }
    BasePaths dirBasePaths = new BasePaths(myConfigFile.getParentFile());
    final byte[] content;
    try {
      content = configVirtualFile.contentsToByteArray();
    }
    catch (IOException e) {
      throw new ExecutionException("Can not read JsTestDriver configuration file " + configVirtualFile.getPath());
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

  private static void addAllRemoteUrlMappings(@NotNull Collection<FileInfo> fileInfos,
                                              @NotNull List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> mappings) {
    for (FileInfo info : fileInfos) {
      RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean mappingBean = createMappingBean(info);
      if (mappingBean != null) {
        mappings.add(mappingBean);
      }
    }
  }

  @Nullable
  private static RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean createMappingBean(@NotNull FileInfo fileInfo) {
    String displayPath = fileInfo.getDisplayPath();
    File file = fileInfo.toFile();
    if (StringUtil.isNotEmpty(displayPath) && file.isFile()) {
      VirtualFile virtualFile = VfsUtil.findFileByIoFile(file, false);
      if (virtualFile != null) {
        String remotePath = "http://localhost:9876/test/" + displayPath;
        return new RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean(virtualFile.getPath(), remotePath);
      }
    }
    return null;
  }

}
