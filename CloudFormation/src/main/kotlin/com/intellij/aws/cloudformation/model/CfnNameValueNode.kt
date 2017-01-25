package com.intellij.aws.cloudformation.model

open class CfnNameValueNode(name: CfnStringValueNode?, val value: CfnScalarValueNode?) : CfnNamedNode(name)