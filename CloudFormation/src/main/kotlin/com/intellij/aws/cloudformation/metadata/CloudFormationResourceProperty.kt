package com.intellij.aws.cloudformation.metadata

data class CloudFormationResourceProperty(
    val name: String,
    val type: String,
    val required: Boolean,
    val url: String,
    val updateRequires: String)