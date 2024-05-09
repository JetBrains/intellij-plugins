package org.angular2.lang.html.tcb

import org.angular2.lang.Angular2LangUtil

const val CORE = Angular2LangUtil.ANGULAR_CORE_PACKAGE;

@Suppress("NonAsciiCharacters")
object R3Identifiers {

  val InputSignalBrandWriteType = ExternalReference(name = "ɵINPUT_SIGNAL_BRAND_WRITE_TYPE", moduleName = CORE)
  val UnwrapDirectiveSignalInputs = ExternalReference(name = "ɵUnwrapDirectiveSignalInputs", moduleName = CORE)
  val unwrapWritableSignal = ExternalReference(name = "ɵunwrapWritableSignal", moduleName = CORE)

  data class ExternalReference(
    val name: String,
    val moduleName: String,
  )
}