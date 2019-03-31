package com.intellij.aws.cloudformation.metadata

data class CloudFormationLimits(val maxParameters: Int, val maxOutputs: Int, val maxMappings: Int)