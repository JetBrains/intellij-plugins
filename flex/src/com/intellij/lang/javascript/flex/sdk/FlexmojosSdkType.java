package com.intellij.lang.javascript.flex.sdk;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

public class FlexmojosSdkType extends SdkType {

  public static final Icon mavenFlexIcon = IconLoader.getIcon("mavenFlex.png", FlexmojosSdkType.class);

  static final String COMPILER_POM_PATTERN = ".+/com/adobe/flex/compiler/.+/compiler-.+\\.pom";

  public FlexmojosSdkType() {
    super("Flexmojos SDK Type");
  }

  public static FlexmojosSdkType getInstance() {
    return SdkType.findInstance(FlexmojosSdkType.class);
  }

  public String suggestHomePath() {
    return null;
  }

  public boolean isValidSdkHome(final String path) {
    return path.replace('\\', '/').matches(COMPILER_POM_PATTERN);
  }

  public FileChooserDescriptor getHomeChooserDescriptor() {
    final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, false, false, false, false, false) {
      public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
        return false;
      }
    };
    descriptor.setTitle("SDK of this type can only be created automatically during Maven project import.");
    return descriptor;
  }

  public String suggestSdkName(final String currentSdkName, final String sdkHome) {
    // C:/Users/xxx/.m2/repository/com/adobe/flex/compiler/4.0.0.14159/compiler-4.0.0.14159.pom
    final int lastDash = sdkHome.lastIndexOf("-");
    final int lastDot = sdkHome.lastIndexOf(".");
    final String version = lastDot > lastDash ? sdkHome.substring(lastDash + 1, lastDot) : "";
    return "Flexmojos SDK " + version;
  }

  public AdditionalDataConfigurable createAdditionalDataConfigurable(final SdkModel sdkModel, final SdkModificator sdkModificator) {
    return new FlexmojosSdkDataConfigurable();
  }

  public SdkAdditionalData loadAdditionalData(final Element element) {
    final FlexmojosSdkAdditionalData additionalData = new FlexmojosSdkAdditionalData();
    additionalData.load(element);
    return additionalData;
  }

  public void saveAdditionalData(final SdkAdditionalData additionalData, final Element element) {
    ((FlexmojosSdkAdditionalData)additionalData).save(element);
  }

  public String getPresentableName() {
    return "Flexmojos SDK";
  }

  public String getHomeFieldLabel() {
    return "Flex Compiler POM:";
  }

  public boolean isRootTypeApplicable(final OrderRootType type) {
    return false;
  }

  public Icon getIcon() {
    return mavenFlexIcon;
  }

  @NotNull
  @Override
  public String getHelpTopic() {
    return "reference.project.structure.sdk.flexmojos";
  }

  public Icon getIconForAddAction() {
    return getIcon();
  }

  public void setupSdkPaths(final Sdk sdk) {
    final VirtualFile sdkRoot = sdk.getHomeDirectory();
    if (sdkRoot == null || !sdkRoot.isValid() || sdkRoot.isDirectory()) {
      return;
    }

    final SdkModificator modificator = sdk.getSdkModificator();
    modificator.setVersionString(readVersion(sdkRoot));

    FlexmojosSdkAdditionalData data = (FlexmojosSdkAdditionalData)sdk.getSdkAdditionalData();
    if (data == null) {
      data = new FlexmojosSdkAdditionalData();
      modificator.setSdkAdditionalData(data);
    }
    data.setup(sdkRoot);

    modificator.commitChanges();
  }

  private static String readVersion(final VirtualFile sdkRoot) {
    try {
      final String version = FlexUtils.findXMLElement(sdkRoot.getInputStream(), "<project><version>");
      return version != null ? version : FlexBundle.message("flex.sdk.version.unknown");
    }
    catch (IOException e) {
      return FlexBundle.message("flex.sdk.version.unknown");
    }
  }

}
