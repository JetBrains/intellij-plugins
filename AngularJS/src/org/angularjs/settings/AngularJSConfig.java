package org.angularjs.settings;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Created by denofevil on 26/11/13.
 */
@State(
    name="AngularJSConfig",
    storages = {
            @Storage(
                    file = StoragePathMacros.APP_CONFIG + "/other.xml"
            )}
)
public class AngularJSConfig implements PersistentStateComponent<AngularJSConfig> {
    public boolean INSERT_WHITESPACE = false;

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
