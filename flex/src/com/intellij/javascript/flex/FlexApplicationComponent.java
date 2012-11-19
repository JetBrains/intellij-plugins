package com.intellij.javascript.flex;

import com.intellij.icons.AllIcons;
import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;
import com.intellij.javascript.flex.mxml.schema.FlexMxmlNSDescriptor;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.MxmlFileType;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeImpl;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.filters.position.NamespaceFilter;
import com.intellij.psi.filters.position.RootTagFilter;
import com.intellij.psi.meta.MetaDataRegistrar;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author yole
 */
public class FlexApplicationComponent extends FileTypeFactory implements ApplicationComponent {
  private static final Icon ICON = AllIcons.FileTypes.JavaClass;

  public static final FileType SWF_FILE_TYPE = new FileType() {
    @NotNull
    public String getName() {
      return "SWF";
    }

    @NotNull
    public String getDescription() {
      return "SWF file type";
    }

    @NotNull
    public String getDefaultExtension() {
      return "swf";
    }

    public Icon getIcon() {
      return ICON;
    }

    public boolean isBinary() {
      return true;
    }

    public boolean isReadOnly() {
      return true;
    }

    public String getCharset(@NotNull final VirtualFile file, final byte[] content) {
      return null;
    }
  };

  public static final Language DECOMPILED_SWF = new Language(JavaScriptSupportLoader.ECMA_SCRIPT_L4, "Decompiled SWF") {
  };

  public static final LanguageFileType MXML = new MxmlFileType();

  @NonNls public static final String HTTP_WWW_ADOBE_COM_2006_FLEX_CONFIG = "http://www.adobe.com/2006/flex-config";

  public static final String[] AIR_VERSIONS =
    {"1.0", "1.1", "1.5", "1.5.1", "1.5.2", "1.5.3", "2.0", "2.5", "2.6", "2.7", "3.0", "3.1", "3.2", "3.3", "3.4", "3.5"};

  @NotNull
  public String getComponentName() {
    return "FlexApplicationComponent";
  }

  public void initComponent() {
    MetaDataRegistrar.getInstance().registerMetaData(
      new RootTagFilter(new NamespaceFilter(JavaScriptSupportLoader.MXML_URIS)),
      FlexMxmlNSDescriptor.class
    );
  }

  public void disposeComponent() {
  }

  public void createFileTypes(final @NotNull FileTypeConsumer consumer) {
    consumer.consume(SWF_FILE_TYPE, "swf");
    consumer.consume(MXML, "mxml;fxg");
  }

  public static class ResourceProvider implements StandardResourceProvider {

    public void registerResources(ResourceRegistrar registrar) {
      registrar.addStdResource(HTTP_WWW_ADOBE_COM_2006_FLEX_CONFIG, "/schemas/FlexCompilerSchema.xsd", FlexApplicationComponent.class);

      registrar.addStdResource(JSAttributeImpl.URN_FLEX_META, "/schemas/KnownMetaData.dtd", FlexApplicationComponent.class);

      for (String version : AIR_VERSIONS) {
        registerAirDescriptorSchema(registrar, version);
      }
    }
  }

  private static void registerAirDescriptorSchema(final ResourceRegistrar registrar, final String version) {
    registrar.addStdResource("http://ns.adobe.com/air/application/" + version,
                             "/schemas/AIR_Descriptor." + version + ".xsd",
                             FlexApplicationComponent.class);
  }
}
