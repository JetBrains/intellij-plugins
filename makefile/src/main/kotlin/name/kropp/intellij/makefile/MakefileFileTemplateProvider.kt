package name.kropp.intellij.makefile

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory

class MakefileFileTemplateProvider : FileTemplateGroupDescriptorFactory {
  override fun getFileTemplatesDescriptor() = FileTemplateGroupDescriptor("Makefile", MakefileIcon, FileTemplateDescriptor("Makefile", MakefileIcon))
}