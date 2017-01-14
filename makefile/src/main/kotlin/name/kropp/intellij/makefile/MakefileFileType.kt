package name.kropp.intellij.makefile

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.util.IconLoader
import org.jetbrains.jps.model.fileTypes.FileNameMatcherFactory

val MakefileIcon = IconLoader.getIcon("/name/kropp/intellij/makefile/makefile.png")
val MakefileTargetIcon = AllIcons.General.Run!!

object MakefileFileType : LanguageFileType(MakefileLanguage) {
  override fun getIcon() = MakefileIcon
  override fun getName() = "Makefile"
  override fun getDescription() = "GNU Makefile"
  override fun getDefaultExtension() = "mk"
}

object MakefileFileTypeFactory : FileTypeFactory() {
  override fun createFileTypes(consumer: FileTypeConsumer) {
    consumer.consume(MakefileFileType, ExactFileNameMatcher("Makefile"), ExtensionFileNameMatcher("mk"), ExactFileNameMatcher("makefile"), ExactFileNameMatcher("GNUmakefile"), FileNameMatcherFactory.getInstance().createMatcher("Makefile.*"))
  }
}