package org.angularjs;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;

/**
 * Created by johnlindquist on 7/11/13.
 */
@State(
        name = "JavaCodeFoldingSettings",
        storages = {
                @Storage(
                        file = StoragePathMacros.APP_CONFIG + "/angularjs.xml"
                )}
)
public class AngularJSConfigAnnotationPatch {
}
