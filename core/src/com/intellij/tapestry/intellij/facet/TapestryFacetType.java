package com.intellij.tapestry.intellij.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.autodetecting.FacetDetector;
import com.intellij.facet.autodetecting.FacetDetectorRegistry;
import com.intellij.facet.autodetecting.UnderlyingFacetSelector;
import com.intellij.facet.impl.autodetecting.FacetDetectorRegistryEx;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.javaee.web.facet.WebFacetConfiguration;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.*;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.log.Logger;
import com.intellij.tapestry.core.log.LoggerFactory;
import com.intellij.tapestry.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

public class TapestryFacetType extends FacetType<TapestryFacet, TapestryFacetConfiguration> {

    public static final FacetTypeId<TapestryFacet> ID = new FacetTypeId<TapestryFacet>("tapestry");
    public static final TapestryFacetType INSTANCE = new TapestryFacetType();

    private static final Logger _logger = LoggerFactory.getInstance().getLogger(TapestryFacetType.class);

    public TapestryFacetType() {
        super(ID, "tapestry", "Tapestry", WebFacet.ID);
    }

    public TapestryFacetConfiguration createDefaultConfiguration() {
        return new TapestryFacetConfiguration();
    }

    public TapestryFacet createFacet(@NotNull Module module, String name, @NotNull TapestryFacetConfiguration configuration, @Nullable Facet underlyingFacet) {
        return new TapestryFacet(this, module, name, configuration, underlyingFacet);
    }

    public boolean isSuitableModuleType(ModuleType moduleType) {
        return moduleType instanceof JavaModuleType;
    }

    public Icon getIcon() {
        return Icons.TAPESTRY_LOGO_SMALL;
    }

    public void registerDetectors(final FacetDetectorRegistry<TapestryFacetConfiguration> facetDetectorRegistry) {
        FacetDetectorRegistryEx<TapestryFacetConfiguration> registry = (FacetDetectorRegistryEx<TapestryFacetConfiguration>) facetDetectorRegistry;
        TapestryFacetDetector detector = new TapestryFacetDetector();

        VirtualFilePattern fileNamePattern = PlatformPatterns.virtualFile().withName(StandardPatterns.string().equalTo("web.xml"));
        XmlFilePattern.Capture rootTagPattern = XmlPatterns.xmlFile().withRootTag(XmlPatterns.xmlTag().withName("web-app"));

        registry.registerSubFacetDetectorForWizard(StdFileTypes.XML, fileNamePattern, detector, new UnderlyingFacetSelector<VirtualFile, FacetConfiguration>() {
            public FacetConfiguration selectUnderlyingFacet(VirtualFile virtualFile, Collection<FacetConfiguration> facetConfigurations) {
                for (FacetConfiguration facetConfiguration : facetConfigurations){
                    if (facetConfiguration instanceof WebFacetConfiguration) return facetConfiguration;
                }
                return null;
            }
        });
        registry.registerOnTheFlyDetector(StdFileTypes.XML, fileNamePattern, rootTagPattern, FacetDetectorRegistryEx.convertDetector(detector));
    }

    private static class TapestryFacetDetector extends FacetDetector<VirtualFile, TapestryFacetConfiguration> {

        public TapestryFacetConfiguration detectFacet(final VirtualFile source, final Collection<TapestryFacetConfiguration> existentFacetConfigurations) {
            if (existentFacetConfigurations.size() > 0) return null;
            try {
                byte[] bytes = new byte[source.getInputStream().available()];
                source.getInputStream().read(bytes);
                return new String(bytes).contains(TapestryConstants.FILTER_CLASS) ? new TapestryFacetConfiguration() : null;
            } catch (Exception ex) {
                _logger.error(ex);
                return null;
            }
        }
    }
}
