package org.jetbrains.plugins.ruby.motion;

import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.xcode.plist.PListLanguageSubstitutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.erb.ERbFileType;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionPListLanguageSubstitutor extends PListLanguageSubstitutor {
  @Override
  public Language getLanguage(@NotNull VirtualFile file, @NotNull Project project) {
    // see RUBY-14113
    // quite safe to assume that plist with ERb is XML, because it's very strange to inject into binary files
    if (ERbFileType.getERBExtension().equals(file.getExtension())) {
      return XMLLanguage.INSTANCE;
    }
    return super.getLanguage(file, project);
  }
}
