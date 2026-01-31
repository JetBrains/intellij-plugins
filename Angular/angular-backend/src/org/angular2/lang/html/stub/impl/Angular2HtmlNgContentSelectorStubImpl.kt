// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.stub.impl

import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector
import org.angular2.lang.html.psi.impl.Angular2HtmlNgContentSelectorImpl
import org.angular2.lang.html.stub.Angular2HtmlNgContentSelectorStub
import java.io.IOException

class Angular2HtmlNgContentSelectorStubImpl : StubBase<Angular2HtmlNgContentSelector>, Angular2HtmlNgContentSelectorStub {
  private val mySelector: StringRef?

  constructor(parent: StubElement<*>?,
              dataStream: StubInputStream) : super(parent, Angular2HtmlElementTypes.NG_CONTENT_SELECTOR) {
    mySelector = dataStream.readName()
  }

  constructor(psi: Angular2HtmlNgContentSelector,
              parent: StubElement<*>?) : super(parent, Angular2HtmlElementTypes.NG_CONTENT_SELECTOR) {
    mySelector = StringRef.fromString(psi.text)
  }

  @Throws(IOException::class)
  fun serialize(stream: StubOutputStream) {
    stream.writeName(StringRef.toString(mySelector))
  }

  fun index(sink: IndexSink) {}
  fun createPsi(): Angular2HtmlNgContentSelector {
    return Angular2HtmlNgContentSelectorImpl(this, Angular2HtmlElementTypes.NG_CONTENT_SELECTOR)
  }

  override val selector: String?
    get() = StringRef.toString(mySelector)
}