package com.intellij.aws.cloudformation

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory

class CloudFormationFileTypeFactory : FileTypeFactory() {
  override fun createFileTypes(consumer: FileTypeConsumer) {
    consumer.consume(CloudFormationJsonFileType.INSTANCE)
    consumer.consume(CloudFormationYamlFileType.INSTANCE)
  }
}
