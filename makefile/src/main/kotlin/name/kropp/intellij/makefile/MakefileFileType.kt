package name.kropp.intellij.makefile

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader

val MakefileIcon = IconLoader.getIcon("/name/kropp/intellij/makefile/makefile.png")

object MakefileFileType : LanguageFileType(MakefileLanguage) {
  override fun getIcon() = MakefileIcon
  override fun getName() = "Makefile"
  override fun getDescription() = "GNU Makefile"
  override fun getDefaultExtension() = "mk"
}

object MakefileFileTypeFactory : FileTypeFactory() {
  override fun createFileTypes(consumer: FileTypeConsumer) {
    consumer.consume(MakefileFileType, "Makefile")
  }
}