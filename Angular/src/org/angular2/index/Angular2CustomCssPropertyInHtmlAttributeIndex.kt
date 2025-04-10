package org.angular2.index

import com.intellij.psi.stubs.StubIndexKey
import org.angular2.lang.html.psi.Angular2HtmlBoundAttribute

internal class Angular2CustomCssPropertyInHtmlAttributeIndex : Angular2IndexBase<Angular2HtmlBoundAttribute>() {

  override fun getKey(): StubIndexKey<String, Angular2HtmlBoundAttribute> = Angular2CustomCssPropertyInHtmlAttributeIndexKey

}

@JvmField
internal val Angular2CustomCssPropertyInHtmlAttributeIndexKey: StubIndexKey<String, Angular2HtmlBoundAttribute> =
  StubIndexKey.createIndexKey<String, Angular2HtmlBoundAttribute>("angular2.js.html.custom-property.index")