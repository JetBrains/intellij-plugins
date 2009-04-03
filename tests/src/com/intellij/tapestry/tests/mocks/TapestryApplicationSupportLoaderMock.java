package com.intellij.tapestry.tests.mocks;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.tapestry.intellij.TapestryApplicationSupportLoader;

@State(
        name = "Loomy",
        storages = {
        @Storage(
                id = "Loomy",
                file = "$APP_CONFIG$/loomy.xml"
        )}
)
public class TapestryApplicationSupportLoaderMock extends TapestryApplicationSupportLoader {

    public void initComponent() {
        System.out.printf("");
    }
}
