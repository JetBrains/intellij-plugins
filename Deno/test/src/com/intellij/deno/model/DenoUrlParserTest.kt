package com.intellij.deno.model

import com.intellij.deno.DenoTestBase

class DenoUrlParserTest : DenoTestBase() {

  fun testDenoLandStd() {
    val url = parseDenoUrl("https://deno.land/std@0.187.0/path/_constants.ts")
    assertNotNull(url!!)

    assertEquals("https", url.schema)
    assertEquals("deno.land", url.namespace)
    assertEquals("", url.subNamespace)
    assertEquals("std", url.packageName)
    assertEquals("0.187.0", url.version)
    assertEquals("path/_constants.ts", url.subPath)
    assertEquals("https://deno.land/std@0.187.0/path/_constants.ts", url.toString())
  }

  fun testDenoLandStdExact() {
    val url = parseDenoUrl("https://deno.land/std@0.187.0")
    assertNotNull(url!!)

    assertEquals("https", url.schema)
    assertEquals("deno.land", url.namespace)
    assertEquals("", url.subNamespace)
    assertEquals("std", url.packageName)
    assertEquals("0.187.0", url.version)
    assertEquals(null, url.subPath)
    assertEquals("https://deno.land/std@0.187.0", url.toString())
  }


  fun testDenoLandX() {
    val url = parseDenoUrl("https://deno.land/x/cly@v0.25.6/mod.ts")
    assertNotNull(url!!)

    assertEquals("https", url.schema)
    assertEquals("deno.land", url.namespace)
    assertEquals("x", url.subNamespace)
    assertEquals("cly", url.packageName)
    assertEquals("v0.25.6", url.version)
    assertEquals("mod.ts", url.subPath)
    assertEquals("https://deno.land/x/cly@v0.25.6/mod.ts", url.toString())
  }

  fun testJsrScoped() {
    val url = parseDenoUrl("https://jsr.io/@std/assert/1.0.5/unimplemented.ts")
    assertNotNull(url!!)

    assertEquals("https", url.schema)
    assertEquals("jsr.io", url.namespace)
    assertEquals("", url.subNamespace)
    assertEquals("@std/assert", url.packageName)
    assertEquals("1.0.5", url.version)
    assertEquals("unimplemented.ts", url.subPath)
    assertEquals("https://jsr.io/@std/assert/1.0.5/unimplemented.ts", url.toString())
  }

  fun testJsrMeta() {
    val url = parseDenoUrl("https://jsr.io/@std/internal/meta.json")
    assertNotNull(url!!)

    assertEquals("https", url.schema)
    assertEquals("jsr.io", url.namespace)
    assertEquals("", url.subNamespace)
    assertEquals("@std/internal", url.packageName)
    assertNull(url.version)
    assertEquals("meta.json", url.subPath)
    assertEquals("https://jsr.io/@std/internal/meta.json", url.toString())
  }

  fun testJsrMetaWithVersion() {
    val url = parseDenoUrl("https://jsr.io/@std/json/0.213.1_meta.json")
    assertNotNull(url!!)

    assertEquals("https", url.schema)
    assertEquals("jsr.io", url.namespace)
    assertEquals("", url.subNamespace)
    assertEquals("@std/json", url.packageName)
    assertNull(url.version)
    assertEquals("0.213.1_meta.json", url.subPath)
    assertEquals("https://jsr.io/@std/json/0.213.1_meta.json", url.toString())
  }

  fun testCustomUrl() {
    val url = parseDenoUrl("https://must.cool.net/custompath/src/without.js")
    assertNotNull(url!!)

    assertEquals("https", url.schema)
    assertEquals("must.cool.net", url.namespace)
    assertEquals("", url.subNamespace)
    assertEquals("custompath", url.packageName)
    assertNull(url.version)
    assertEquals("src/without.js", url.subPath)
    assertEquals("https://must.cool.net/custompath/src/without.js", url.toString())
  }

  fun testCustomUrlWithSubNamespace() {
    val url = parseDenoUrl("https://esm.sh/v126/react-dom@18.2.0")
    assertNotNull(url!!)

    assertEquals("https", url.schema)
    assertEquals("esm.sh", url.namespace)
    assertEquals("v126", url.subNamespace)
    assertEquals("react-dom", url.packageName)
    assertEquals("18.2.0", url.version)
    assertNull(url.subPath)
    assertEquals("https://esm.sh/v126/react-dom@18.2.0", url.toString())
  }

  fun testCustomScopedUrlWithSubNamespace() {
    val url = parseDenoUrl("https://esm.sh/v126/@mdx-js/react@2.3.0")
    assertNotNull(url!!)

    assertEquals("https", url.schema)
    assertEquals("esm.sh", url.namespace)
    assertEquals("v126", url.subNamespace)
    assertEquals("@mdx-js/react", url.packageName)
    assertEquals("2.3.0", url.version)
    assertNull(url.subPath)
    assertEquals("https://esm.sh/v126/@mdx-js/react@2.3.0", url.toString())
  }

  fun testCustomScopedUrlWithSubNamespaceIncorrectVersion() {
    val url = parseDenoUrl("https://esm.sh/v128/@types/react@~18.2/index.d.ts")
    assertNotNull(url!!)

    assertEquals("https", url.schema)
    assertEquals("esm.sh", url.namespace)
    assertEquals("v128", url.subNamespace)
    assertEquals("@types/react", url.packageName)
    assertEquals("~18.2", url.version)
    assertEquals("index.d.ts", url.subPath)
    assertEquals("https://esm.sh/v128/@types/react@~18.2/index.d.ts", url.toString())
  }

  fun testJsrScopedWithModFile() {
    val url = parseDenoUrl("https://jsr.io/@luca/cases/1.0.0/mod.ts")
    assertNotNull(url!!)

    assertEquals("https", url.schema)
    assertEquals("jsr.io", url.namespace)
    assertEquals("", url.subNamespace)
    assertEquals("@luca/cases", url.packageName)
    assertEquals("1.0.0", url.version)
    assertEquals("mod.ts", url.subPath)
    assertEquals("https://jsr.io/@luca/cases/1.0.0/mod.ts", url.toString())
  }
}