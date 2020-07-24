// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.model.sdk;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtilRt;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class RslUtil {
  private static final Logger LOG = Logger.getInstance(RslUtil.class.getName());

  private static final Map<String, Pair<Long, Map<String, List<String>>>> ourConfigFilePathToTimestampAndSwcPathToRslUrls =
    new HashMap<>();

  public static boolean canBeRsl(final String sdkHome, final String swcPath) {
    final List<String> rslUrls = getRslUrls(sdkHome, swcPath);
    for (String url : rslUrls) {
      if (url.startsWith("http://") ||
          new File(sdkHome + "/frameworks/rsls/" + url).isFile()) {
        return true;
      }
    }
    return false;
  }

  public static List<String> getRslUrls(final String sdkHome, final String swcPath) {
    final Map<String, List<String>> swcPathToRslUrlMap = getSwcPathToRslUrlsMap(sdkHome);
    final List<String> rslUrls = swcPathToRslUrlMap.get(SystemInfo.isFileSystemCaseSensitive ? swcPath : StringUtil.toLowerCase(swcPath));
    return rslUrls == null ? Collections.emptyList() : rslUrls;
  }

  private synchronized static Map<String, List<String>> getSwcPathToRslUrlsMap(final String sdkHome) {
    final String configFilePath = sdkHome + "/frameworks/flex-config.xml";
    final File configFile = new File(configFilePath);
    if (!configFile.isFile()) {
      LOG.warn("File not found: " + configFilePath);
      ourConfigFilePathToTimestampAndSwcPathToRslUrls.remove(configFilePath);
      return Collections.emptyMap();
    }

    Pair<Long, Map<String, List<String>>> data = ourConfigFilePathToTimestampAndSwcPathToRslUrls.get(configFilePath);

    final Long currentTimestamp = configFile.lastModified();
    final Long cachedTimestamp = Pair.getFirst(data);

    if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
      ourConfigFilePathToTimestampAndSwcPathToRslUrls.remove(configFilePath);

      final Map<String, List<String>> swcPathToRslMap = new HashMap<>();
      try {
        final Element rootElement = JDOMUtil.load(configFile);
        for (Element rslElement : rootElement.getChildren("runtime-shared-library-path",
                                                          rootElement.getNamespace())) {
          final Element swcPathElement = rslElement.getChild("path-element", rslElement.getNamespace());
          if (swcPathElement != null) {
            final List<String> rslUrls = new ArrayList<>(2);
            for (Element rslUrlElement : rslElement.getChildren("rsl-url", rootElement.getNamespace())) {
              final String rslUrl = rslUrlElement.getTextNormalize();
              rslUrls.add(rslUrl);
            }
            final String swcPath = PathUtilRt.getParentPath(configFilePath) + "/" + swcPathElement.getTextNormalize();
            swcPathToRslMap.put(SystemInfo.isFileSystemCaseSensitive ? swcPath : StringUtil.toLowerCase(swcPath), rslUrls);
          }
        }
      }
      catch (IOException | JDOMException e) {
        LOG.warn(configFilePath, e);
      }

      data = Pair.create(currentTimestamp, swcPathToRslMap);
      ourConfigFilePathToTimestampAndSwcPathToRslUrls.put(configFilePath, data);
    }

    return data.second;
  }
}
