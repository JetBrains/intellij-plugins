package com.intellij.tapestry.intellij;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.codeInspection.ModifiableModel;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.profile.codeInspection.InspectionProfileManager;
//import com.logical.licensemanager.License;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.log.LoggerFactory;
import com.intellij.tapestry.intellij.core.log.IntellijLoggerFactory;
import com.intellij.tapestry.intellij.facet.TapestryFacetType;
import com.intellij.tapestry.intellij.lang.completion.TemplateCompletionData;
//import com.logical.tapestry.intellij.license.LicenseManager;
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
public class TapestryApplicationSupportLoader implements ApplicationComponent, PersistentStateComponent<TapestryApplicationSupportLoader.ApplicationConfiguration> {

    public static final String PLUGIN_ID = "Loomy";

//    private License                     _license;
    private ApplicationConfiguration    _configuration;

    static {
        LoggerFactory.setLoggerFactoryImplementation(new IntellijLoggerFactory());
    }//static


    public TapestryApplicationSupportLoader() {
        _configuration = new ApplicationConfiguration();
    }//Constructor

    public static TapestryApplicationSupportLoader getInstance() {
        return ApplicationManager.getApplication().getComponent(TapestryApplicationSupportLoader.class);
    }//getInstance


    public boolean hasValidLicense() {
        String pluginVersion;

        // for unit testing
        if (PluginManager.getPlugin(PluginId.getId(PLUGIN_ID)) != null)
            pluginVersion = PluginManager.getPlugin(PluginId.getId(PLUGIN_ID)).getVersion();
        else
            pluginVersion = "1.0";

//        return _license != null && _license.isValid(pluginVersion) == License.VALID_LICENSE;
        return true;
    }//hasValidLicence

    
    @NonNls
    @NotNull
    public String getComponentName() {
        return TapestryApplicationSupportLoader.class.getName();
    }//getComponentName


    public void initComponent() {
//        _license = LicenseManager.getLicense(PluginManager.getPlugin(PluginId.getId(PLUGIN_ID)).getVersion());

        if (!hasValidLicense()) {
            return;
        }

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
     * {@inheritDoc}
     */
    public ApplicationConfiguration getState() {
        return _configuration;
    }//getState


    /**
     * {@inheritDoc}
     */
    public void loadState(ApplicationConfiguration state) {
        _configuration = state;
    }//loadState



    /**
     * Disables some inspections so that Tapestry templates are correctly highlighted.
     * @throws IOException if an error occurs disabling the inspections.
     */
    public void disableInspections() throws IOException {

        ModifiableModel inspectionModel = ((InspectionProfileImpl) InspectionProfileManager.getInstance().getProfile("default")).getModifiableModel();
        inspectionModel.disableTool("XmlWrongRootElement");
        inspectionModel.disableTool("HtmlUnknownAttribute");
        inspectionModel.commit();
    }//disableInspections


//    protected void setLicense(License license) {
//        _license = license;
//    }//setLicense


    public static class ApplicationConfiguration {

        private String _jiraUsername = "";
        private String _jiraPassword = "";


        public String getJiraUsername() {
            return _jiraUsername;
        }//getJiraUsername


        public void setJiraUsername(String jiraUsername) {
            _jiraUsername = jiraUsername;
        }//setJiraUsername


        public String getJiraPassword() {
            return _jiraPassword;
        }//getJiraPassword


        public void setJiraPassword(String jiraPassword) {
            _jiraPassword = jiraPassword;
        }//setJiraPassword
    }//ApplicationConfiguration

}//TapestryApplicationSupportLoader
