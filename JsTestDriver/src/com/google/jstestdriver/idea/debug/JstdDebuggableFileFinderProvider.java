package com.google.jstestdriver.idea.debug;

import com.google.jstestdriver.config.ParsedConfiguration;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.model.BasePaths;
import com.intellij.execution.ExecutionException;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.javascript.debugger.execution.RemoteJavaScriptDebugConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JstdDebuggableFileFinderProvider {

  private JstdDebuggableFileFinderProvider() {}

  public static RemoteDebuggingFileFinder createFileFinder(@NotNull Project project, @NotNull File configFile) throws ExecutionException {
    List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> mapping = extractMappings(configFile);
    return new RemoteDebuggingFileFinder(project, mapping, false);
  }

  private static List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> extractMappings(@NotNull File configFile) throws
                                                                                                                         ExecutionException {
    VirtualFile configVirtualFile = VfsUtil.findFileByIoFile(configFile, false);
    if (configVirtualFile == null) {
      throw new ExecutionException("Can not find config file " + configFile.getAbsolutePath());
    }
    BasePaths dirBasePaths = new BasePaths(configFile.getParentFile());
    byte[] content;
    try {
      content = configVirtualFile.contentsToByteArray();
    }
    catch (IOException e) {
      throw new ExecutionException("Can not read " + configFile.getAbsolutePath());
    }
    Reader reader = new InputStreamReader(new ByteArrayInputStream(content), Charset.defaultCharset());
    try {
      BasePaths allBasePaths = readBasePath(reader, dirBasePaths);
      List<RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean> mappings = ContainerUtilRt.newArrayList();
      for (File basePath : allBasePaths) {
        RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean mappingBean = createMappingBean(basePath, configVirtualFile);
        if (mappingBean != null) {
          mappings.add(mappingBean);
        }
      }
      return mappings;
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
    throw new ExecutionException("Unknown error");
  }

  @Nullable
  private static RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean createMappingBean(@NotNull File basePath,
                                                                                           @Nullable VirtualFile configVirtualFile) {
    if (configVirtualFile == null) {
      return null;
    }
    VirtualFile basePathVirtualFile = VfsUtil.findFileByIoFile(basePath, true);
    VirtualFile configFileDir = configVirtualFile.getParent();
    if (basePathVirtualFile == null || configFileDir == null) {
      return null;
    }
    boolean useRelativeBasePath = VfsUtilCore.isAncestor(configFileDir, basePathVirtualFile, false);
    String remoteBaseUrl = "http://localhost:9876/test";
    if (!useRelativeBasePath) {
      remoteBaseUrl = remoteBaseUrl + "/" + basePathVirtualFile.getPath();
    }
    return new RemoteJavaScriptDebugConfiguration.RemoteUrlMappingBean(basePathVirtualFile.getPath(), remoteBaseUrl);
  }

  @NotNull
  private static BasePaths readBasePath(@NotNull Reader configFileReader, @NotNull BasePaths initialBasePaths) {
    YamlParser yamlParser = new YamlParser();
    ParsedConfiguration parsedConfiguration = (ParsedConfiguration) yamlParser.parse(configFileReader, initialBasePaths);
    return parsedConfiguration.getBasePaths();
  }

}
