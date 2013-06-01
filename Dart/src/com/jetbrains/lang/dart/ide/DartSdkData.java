package com.jetbrains.lang.dart.ide;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.util.xmlb.XmlSerializerUtil;

/**
 * @author: Fedor.Korotkov
 */
public class DartSdkData implements SdkAdditionalData, PersistentStateComponent<DartSdkData> {
  private String homePath = "";
  private String version = "";

  public DartSdkData() {
  }

  public DartSdkData(String homePath, String version) {
    this.homePath = homePath;
    this.version = version;
  }

  public String getHomePath() {
    return homePath;
  }

  public String getVersion() {
    return version;
  }

  @SuppressWarnings({"CloneDoesntCallSuperClone"})
  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public DartSdkData getState() {
    return this;
  }

  public void loadState(DartSdkData state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
