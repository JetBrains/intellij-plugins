/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.osgi.maven

import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase
import org.assertj.core.api.Assertions.assertThat
import org.jdom.Element
import org.osmorc.facet.OsmorcFacet
import org.osmorc.facet.OsmorcFacetConfiguration

class OsgiFacetConfigurationTest : JavaCodeInsightFixtureTestCase() {
  private lateinit var config: OsmorcFacetConfiguration

  override fun setUp() {
    super.setUp()
    config = OsmorcFacet(module).configuration
  }

  fun testResourceSerialization() {
    config.setAdditionalProperties("Include-Resource=r1=/p1/r1,r2=@/p2/r2,{ /p3/r3 }")
    val text = JDOMUtil.writeElement(serialize())
    assertThat(text).contains("<property key=\"Include-Resource\" value=\"r1=/p1/r1,r2=@/p2/r2,{ /p3/r3 }\" />")
  }

  fun testResourceSerializationPerformance() {
    config.setAdditionalProperties("Include-Resource=warm-up")
    serialize()
    val resources = StringBuilder("Include-Resource=")
    for (i in 1..5000) resources.append(if (i > 1) "," else "").append("images/image$i.png=/src/images/image$i.png")
    config.setAdditionalProperties(resources.toString())
    PlatformTestUtil.startPerformanceTest("OSGi Facet Serialization", 10000, { (1..5).forEach { serialize() } }).assertTiming()
  }

  private fun serialize(): Element {
    val element = Element("test")
    @Suppress("DEPRECATION") config.writeExternal(element)
    return element
  }
}