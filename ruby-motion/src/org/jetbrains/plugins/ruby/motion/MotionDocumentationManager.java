package org.jetbrains.plugins.ruby.motion;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.cidr.CocoaDocumentationManagerImpl;
import com.jetbrains.cidr.DocSet;
import com.jetbrains.cidr.xcode.Xcode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Dennis.Ushakov
 */
public class MotionDocumentationManager extends CocoaDocumentationManagerImpl {

  private DocSet myCachedDocSet;
  private String myCachedSdkVersion;

  public MotionDocumentationManager(@NotNull Project project) {
    super(project);
  }

  @Nullable
  @Override
  public DocSet getDocSet(@NotNull PsiElement targetElement) {
    final Module module = RubyMotionUtil.getInstance().getModuleWithMotionSupport(getProject());
    if (module != null) {
      final String sdkVersion = RubyMotionUtil.getInstance().getInstance().getSdkVersion(module);
      if (sdkVersion == null) {
        return null;
      }
      if (myCachedDocSet != null && sdkVersion.equals(myCachedSdkVersion)) {
        return myCachedDocSet;
      }
      File docSet = Xcode.getSubFile("Platforms/iPhoneOS.platform/Developer/Documentation/DocSets/com.apple.adc.documentation.AppleiOS" + sdkVersion + ".iOSLibrary.docset");
      if (!docSet.exists()) {
        docSet = RubyMotionUtil.getInstance().isOSX(module) ?
                 Xcode.getSubFile("Documentation/DocSets/com.apple.adc.documentation.AppleOSX" + sdkVersion + ".CoreReference.docset") :
                 Xcode.getSubFile("Documentation/DocSets/com.apple.adc.documentation.AppleiOS" + sdkVersion + ".iOSLibrary.docset");
      }
      if (!docSet.exists()) {
        docSet = RubyMotionUtil.getInstance().isOSX(module) ?
                 Xcode.getSubFile("Documentation/DocSets/com.apple.adc.documentation.OSX.docset") :
                 Xcode.getSubFile("Documentation/DocSets/com.apple.adc.documentation.iOS.docset");
      }
      myCachedDocSet = DocSet.read(docSet);
      myCachedSdkVersion = sdkVersion;
      return myCachedDocSet;
    }

    return null;
  }
}
