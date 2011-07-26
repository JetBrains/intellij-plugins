package com.intellij.tapestry.tests.mocks;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.tapestry.intellij.TapestryApplicationSupportLoader;

@State(
        name = "Loomy",
        storages = {
        @Storage(
                file = "$APP_CONFIG$/loomy.xml"
        )}
)
public class TapestryApplicationSupportLoaderMock extends TapestryApplicationSupportLoader {

    @Override
    public void initComponent() {
        System.out.printf("");
    }
}
