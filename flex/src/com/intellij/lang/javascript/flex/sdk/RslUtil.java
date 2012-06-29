package com.intellij.lang.javascript.flex.sdk;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.PathUtil;
import gnu.trove.THashMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RslUtil {
  private static final Logger LOG = Logger.getInstance(RslUtil.class.getName());

  private static final Map<String, Pair<Long, Map<String, List<String>>>> ourConfigFilePathToTimestampAndSwcPathToRslUrls =
    new THashMap<String, Pair<Long, Map<String, List<String>>>>();

  public static boolean canBeRsl(final Sdk sdk, final String swcPath) {
    return !getRslUrls(sdk, swcPath).isEmpty();
  }

  public static List<String> getRslUrls(final Sdk sdk, final String swcPath) {
    final Map<String, List<String>> swcPathToRslUrlMap = getSwcPathToRslUrlsMap(sdk);
    final List<String> rslUrls = swcPathToRslUrlMap.get(SystemInfo.isFileSystemCaseSensitive ? swcPath : swcPath.toLowerCase());
    return rslUrls == null ? Collections.<String>emptyList() : rslUrls;
  }

  private synchronized static Map<String, List<String>> getSwcPathToRslUrlsMap(final Sdk sdk) {
    final String configFilePath = sdk.getHomePath() + "/frameworks/flex-config.xml";
    final File configFile = new File(configFilePath);
    if (!configFile.isFile()) {
      LOG.warn("File not found: " + configFilePath);
      ourConfigFilePathToTimestampAndSwcPathToRslUrls.remove(configFilePath);
      return Collections.emptyMap();
    }

    Pair<Long, Map<String, List<String>>> data = ourConfigFilePathToTimestampAndSwcPathToRslUrls.get(configFilePath);

    final Long currentTimestamp = configFile.lastModified();
    final Long cachedTimestamp = data == null ? null : data.first;

    if (cachedTimestamp == null || !cachedTimestamp.equals(currentTimestamp)) {
      ourConfigFilePathToTimestampAndSwcPathToRslUrls.remove(configFilePath);

      final Map<String, List<String>> swcPathToRslMap = new THashMap<String, List<String>>();
      try {
        final Document document = JDOMUtil.loadDocument(configFile);
        final Element rootElement = document.getRootElement();
        if (rootElement != null) {
          //noinspection unchecked
          for (Element rslElement : ((Iterable<Element>)rootElement.getChildren("runtime-shared-library-path",
                                                                                rootElement.getNamespace()))) {
            final Element swcPathElement = rslElement.getChild("path-element", rslElement.getNamespace());
            if (swcPathElement != null) {
              final List<String> rslUrls = new ArrayList<String>(2);
              //noinspection unchecked
              for (Element rslUrlElement : ((Iterable<Element>)rslElement.getChildren("rsl-url", rootElement.getNamespace()))) {
                final String rslUrl = rslUrlElement.getTextNormalize();
                rslUrls.add(rslUrl);
              }
              final String swcPath = PathUtil.getParentPath(configFilePath) + "/" + swcPathElement.getTextNormalize();
              swcPathToRslMap.put(SystemInfo.isFileSystemCaseSensitive ? swcPath : swcPath.toLowerCase(), rslUrls);
            }
          }
        }
      }
      catch (IOException e) {
        LOG.warn(configFilePath, e);
      }
      catch (JDOMException e) {
        LOG.warn(configFilePath, e);
      }

      data = Pair.create(currentTimestamp, swcPathToRslMap);
      ourConfigFilePathToTimestampAndSwcPathToRslUrls.put(configFilePath, data);
    }

    return data.second;
  }
}
