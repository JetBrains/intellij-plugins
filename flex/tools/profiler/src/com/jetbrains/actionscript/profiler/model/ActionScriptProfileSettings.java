package com.jetbrains.actionscript.profiler.model;

import com.intellij.openapi.components.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.SystemProperties;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author: Fedor.Korotkov
 */
@State(
  name = "ActionScriptProfileSettings",
  storages = {
    @Storage(
      file = StoragePathMacros.APP_CONFIG + "/actionscript_profile_settings.xml"
    )}
)
public class ActionScriptProfileSettings implements PersistentStateComponent<Element> {
  public static final String ACTIONSCRIPT_PROFILER_SETTINGS = "ActionScriptProfileSettings";

  private static final int DEFAULT_PORT = 1310;
  private static final String DEFAULT_HOST = "127.0.0.1";

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private String pathToMmCfg = "";
  private boolean useCustomPathToMmCfg = false;

  private static final String PORT_ATTR_NAME = "port";
  private static final String HOST_ATTR_NAME = "host";
  private static final String PATH_ATTR_NAME = "path";
  private static final String CUSTOM_MMCFG_ATTR_NAME = "custom.mm.cfg";

  public static String getMmCfgPath() {
    if(getInstance().isUseCustomPathToMmCfg()) {
      return getInstance().getPathToMmCfg();
    }
    return getDefaultMmCfgPath();
  }

  public static String getDefaultMmCfgPath() {
    return SystemProperties.getUserHome() + File.separator + "mm.cfg";
  }

  public static ActionScriptProfileSettings getInstance() {
    return ServiceManager.getService(ActionScriptProfileSettings.class);
  }

  @Override
  public void loadState(Element state) {
    setPortFromString(state.getAttributeValue(PORT_ATTR_NAME));
    setHostFromString(state.getAttributeValue(HOST_ATTR_NAME));
    setPathFromString(state.getAttributeValue(PATH_ATTR_NAME));
    setCustomPathFromString(state.getAttributeValue(CUSTOM_MMCFG_ATTR_NAME));
  }

  private void setCustomPathFromString(String value) {
    useCustomPathToMmCfg = Boolean.valueOf(value);
  }

  private void setPathFromString(String value) {
    pathToMmCfg = value;
  }

  @Override
  public Element getState() {
    final Element element = new Element(ACTIONSCRIPT_PROFILER_SETTINGS);
    element.setAttribute(PORT_ATTR_NAME, String.valueOf(port));
    element.setAttribute(HOST_ATTR_NAME, host);
    element.setAttribute(PATH_ATTR_NAME, pathToMmCfg);
    element.setAttribute(CUSTOM_MMCFG_ATTR_NAME, Boolean.toString(useCustomPathToMmCfg));
    return element;
  }

  public void setHostFromString(String s) {
    if (!StringUtil.isEmpty(s)) {
      host = s;
    }
    else {
      host = DEFAULT_HOST;
    }
  }

  public void setPortFromString(String s) {
    if (s != null) {
      try {
        port = Integer.parseInt(s);
      }
      catch (NumberFormatException ex) {
        port = DEFAULT_PORT;
      }
    }
    else {
      port = DEFAULT_PORT;
    }
  }

  public int getPort() {
    return port;
  }

  public String getHost() {
    return host;
  }

  @NotNull
  public String getPathToMmCfg() {
    if (pathToMmCfg == null) {
      return "";
    }
    return pathToMmCfg;
  }

  public void setPathToMmCfg(String pathToMmCfg) {
    this.pathToMmCfg = pathToMmCfg;
  }

  public boolean isUseCustomPathToMmCfg() {
    return useCustomPathToMmCfg;
  }

  public void setUseCustomPathToMmCfg(boolean useCustomPathToMmCfg) {
    this.useCustomPathToMmCfg = useCustomPathToMmCfg;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ActionScriptProfileSettings that = (ActionScriptProfileSettings)o;

    if (port != that.port) return false;
    if (!host.equals(that.host)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = host.hashCode();
    result = 31 * result + port;
    return result;
  }
}
