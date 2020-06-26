// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.text.CharSequenceReader;
import com.intellij.util.xml.NanoXmlUtil;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class FlexCompilerConfigFileUtil {

  private static final Key<Pair<Long, Collection<NamespacesInfo>>> MOD_STAMP_TO_NAMESPACES_INFOS =
    Key.create("MOD_STAMP_TO_NAMESPACES_INFOS");
  private static final Key<Pair<Long, InfoFromConfigFile>> MOD_STAMP_AND_INFO_FROM_CONFIG_FILE =
    Key.create("MOD_STAMP_AND_INFO_FROM_CONFIG_FILE");

  private static final String TARGET_PLAYER_ELEMENT = "<flex-config><target-player>";
  private static final String FILE_SPEC_ELEMENT = "<flex-config><file-specs><path-element>";
  private static final String OUTPUT_ELEMENT = "<flex-config><output>";

  public static final String INCLUDE_NAMESPACES = "include-namespaces";
  public static final String NAMESPACES = "namespaces";
  public static final String NAMESPACE = "namespace";
  public static final String MANIFEST = "manifest";
  public static final String URI = "uri";

  public static final String DEFINE = "define";
  public static final String NAME = "name";
  public static final String VALUE = "value";

  public static final String FILE_SPECS = "file-specs";
  public static final String OUTPUT = "output";

  public static final class NamespacesInfo {
    public final String namespace;
    public final String manifest;
    public final boolean includedInSwc;

    private NamespacesInfo(final String namespace, final String manifest, final boolean includedInSwc) {
      this.namespace = namespace;
      this.manifest = manifest;
      this.includedInSwc = includedInSwc;
    }
  }

  private FlexCompilerConfigFileUtil() {
  }

  public static Collection<NamespacesInfo> getNamespacesInfos(final VirtualFile configFile) {
    if (configFile == null || !configFile.isValid() || configFile.isDirectory()) {
      return Collections.emptyList();
    }

    Pair<Long, Collection<NamespacesInfo>> data = configFile.getUserData(MOD_STAMP_TO_NAMESPACES_INFOS);

    final FileDocumentManager documentManager = FileDocumentManager.getInstance();
    final Document cachedDocument = documentManager.getCachedDocument(configFile);
    final Long currentTimestamp = cachedDocument != null ? cachedDocument.getModificationStamp() : configFile.getModificationCount();
    final Long cachedTimestamp = Pair.getFirst(data);

    if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
      data = null;
      configFile.putUserData(MOD_STAMP_TO_NAMESPACES_INFOS, data);

      try {
        final NamespacesXmlBuilder builder = new NamespacesXmlBuilder();
        if (cachedDocument != null) {
          NanoXmlUtil.parse(new CharSequenceReader(cachedDocument.getCharsSequence()), builder);
        }
        else {
          NanoXmlUtil.parse(configFile.getInputStream(), builder);
        }

        final Collection<NamespacesInfo> namespacesInfos = new ArrayList<>();
        final Collection<String> includedInSwcNamespaces = builder.getIncludedNamespaces();
        for (Pair<String, String> namespaceAndManifest : builder.getNamespacesAndManifests()) {
          namespacesInfos.add(new NamespacesInfo(namespaceAndManifest.first, namespaceAndManifest.second,
                                                 includedInSwcNamespaces.contains(namespaceAndManifest.first)));
        }

        data = Pair.create(currentTimestamp, namespacesInfos);
        configFile.putUserData(MOD_STAMP_TO_NAMESPACES_INFOS, data);
      }
      catch (IOException ignored) {
      }
    }

    return data == null ? Collections.emptyList() : data.second;
  }

  @NotNull
  public static InfoFromConfigFile getInfoFromConfigFile(final String configFilePath) {
    final VirtualFile configFile = configFilePath.isEmpty() ? null : LocalFileSystem.getInstance().findFileByPath(configFilePath);
    if (configFile == null) {
      return InfoFromConfigFile.DEFAULT;
    }

    Pair<Long, InfoFromConfigFile> data = configFile.getUserData(MOD_STAMP_AND_INFO_FROM_CONFIG_FILE);

    final FileDocumentManager documentManager = FileDocumentManager.getInstance();
    final Document cachedDocument = documentManager.getCachedDocument(configFile);
    final Long currentTimestamp = cachedDocument != null ? cachedDocument.getModificationStamp() : configFile.getModificationCount();
    final Long cachedTimestamp = Pair.getFirst(data);

    if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
      data = null;
      configFile.putUserData(MOD_STAMP_AND_INFO_FROM_CONFIG_FILE, data);

      final List<String> xmlElements = Arrays.asList(FILE_SPEC_ELEMENT, OUTPUT_ELEMENT, TARGET_PLAYER_ELEMENT);

      String mainClassPath = null;
      String outputPath = null;
      String targetPlayer = null;

      try {
        final InputStream inputStream =
          cachedDocument == null ? configFile.getInputStream() : new ByteArrayInputStream(cachedDocument.getText().getBytes(
            StandardCharsets.UTF_8));
        final Map<String, List<String>> map = FlexUtils.findXMLElements(inputStream, xmlElements);

        final List<String> fileSpecList = map.get(FILE_SPEC_ELEMENT);
        if (!fileSpecList.isEmpty()) {
          mainClassPath = fileSpecList.get(0).trim();
        }

        final List<String> outputList = map.get(OUTPUT_ELEMENT);
        if (!outputList.isEmpty()) {
          outputPath = outputList.get(0).trim();
          if (!FileUtil.isAbsolute(outputPath)) {
            try {
              outputPath =
                FileUtil.toSystemIndependentName(new File(configFile.getParent().getPath() + "/" + outputPath).getCanonicalPath());
            }
            catch (IOException e) {
              outputPath =
                FileUtil.toSystemIndependentName(new File(configFile.getParent().getPath() + "/" + outputPath).getAbsolutePath());
            }
          }
        }

        final List<String> targetPlayerList = map.get(TARGET_PLAYER_ELEMENT);
        if (!targetPlayerList.isEmpty()) {
          targetPlayer = targetPlayerList.get(0).trim();
        }
      }
      catch (IOException ignore) {/*ignore*/ }

      final String outputFileName = outputPath == null ? null : PathUtil.getFileName(outputPath);
      final String outputFolderPath = outputPath == null ? null : PathUtil.getParentPath(outputPath);
      data =
        Pair.create(currentTimestamp, new InfoFromConfigFile(configFile, mainClassPath, outputFileName, outputFolderPath, targetPlayer));
      configFile.putUserData(MOD_STAMP_AND_INFO_FROM_CONFIG_FILE, data);
    }

    return data.second;
  }
}
