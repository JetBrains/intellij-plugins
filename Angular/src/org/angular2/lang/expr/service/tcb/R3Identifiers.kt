package org.angular2.lang.expr.service.tcb

import org.angular2.lang.Angular2LangUtil

const val CORE: String = Angular2LangUtil.ANGULAR_CORE_PACKAGE;

@Suppress("NonAsciiCharacters")
object R3Identifiers {

  val InputSignalBrandWriteType: ExternalReference = ExternalReference(name = "ɵINPUT_SIGNAL_BRAND_WRITE_TYPE", moduleName = CORE)
  val UnwrapDirectiveSignalInputs: ExternalReference = ExternalReference(name = "ɵUnwrapDirectiveSignalInputs", moduleName = CORE)
  val unwrapWritableSignal: ExternalReference = ExternalReference(name = "ɵunwrapWritableSignal", moduleName = CORE)

  data class ExternalReference(
    val name: String,
    val moduleName: String,
  )
}