package org.angularjs.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
@State(name = "AngularJSConfig", storages = @Storage("other.xml"))
public class AngularJSConfig implements PersistentStateComponent<AngularJSConfig> {
  public boolean INSERT_WHITESPACE;

  public static AngularJSConfig getInstance() {
    return ServiceManager.getService(AngularJSConfig.class);
  }

  @Nullable
  @Override
  public AngularJSConfig getState() {
    return this;
  }

  @Override
  public void loadState(AngularJSConfig state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
