package com.intellij.tapestry.intellij;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInspection.ModifiableModel;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.profile.codeInspection.InspectionProfileManager;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.log.LoggerFactory;
import com.intellij.tapestry.intellij.core.log.IntellijLoggerFactory;
import com.intellij.tapestry.intellij.facet.TapestryFacetType;
import com.intellij.tapestry.intellij.lang.completion.TemplateCompletionData;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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
//        _license = LicenseManager.getLicense(PluginManager.getPlugin(PluginId.getId(PLUGIN_ID)).getVersion());

        // Add Tapestry support for web modules.
        FacetTypeRegistry.getInstance().registerFacetType(TapestryFacetType.INSTANCE);

        // Register completion data.
        CompletionUtil.registerCompletionData(StdFileTypes.HTML, new TemplateCompletionData());

        // Register Tapestry template extension as HTML file type.
        if (FileTypeManager.getInstance().getFileTypeByExtension(TapestryConstants.TEMPLATE_FILE_EXTENSION.substring(1)) == StdFileTypes.UNKNOWN) {
            FileTypeManager.getInstance().associateExtension(StdFileTypes.HTML, TapestryConstants.TEMPLATE_FILE_EXTENSION.substring(1));
        }
    }

    public void disposeComponent() { /* do nothing */ }



    /**
     * Disables some inspections so that Tapestry templates are correctly highlighted.
     * @throws IOException if an error occurs disabling the inspections.
     */
    public void disableInspections() throws IOException {

        ModifiableModel inspectionModel = ((InspectionProfileImpl) InspectionProfileManager.getInstance().getProfile("default")).getModifiableModel();
        inspectionModel.disableTool("XmlWrongRootElement", (NamedScope)null);
        inspectionModel.disableTool("HtmlUnknownAttribute", (NamedScope)null);
        inspectionModel.commit();
    }//disableInspections

}//TapestryApplicationSupportLoader
