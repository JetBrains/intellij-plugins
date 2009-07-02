package com.intellij.tapestry.intellij;

import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.tapestry.core.log.LoggerFactory;
import com.intellij.tapestry.intellij.core.log.IntellijLoggerFactory;
import com.intellij.tapestry.intellij.facet.TapestryFacetType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

@State(
        name = "Loomy",
        storages = {
        @Storage(
                id = "Loomy",
                file = "$APP_CONFIG$/tapestry.xml"
        )}
)
public class TapestryApplicationSupportLoader implements ApplicationComponent {

    public static final String PLUGIN_ID = "Loomy";


    static {
        LoggerFactory.setLoggerFactoryImplementation(new IntellijLoggerFactory());
    }//static


    public static TapestryApplicationSupportLoader getInstance() {
        return ApplicationManager.getApplication().getComponent(TapestryApplicationSupportLoader.class);
    }//getInstance


    @NonNls
    @NotNull
    public String getComponentName() {
        return TapestryApplicationSupportLoader.class.getName();
    }//getComponentName


    public void initComponent() {
        // Add Tapestry support for web modules.
        FacetTypeRegistry.getInstance().registerFacetType(TapestryFacetType.INSTANCE);
    }

    public void disposeComponent() { /* do nothing */ }

}//TapestryApplicationSupportLoader
