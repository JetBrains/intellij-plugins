// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.lang.typescript.kolar.KolarTranspiler
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.VueTestMode
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class VueKolarTranspilerTest :
  VueTestCase("kolar", VueTestMode.NO_PLUGIN) {

  override val dirModeByDefault: Boolean = true

  fun doTest() {
    doConfiguredTest(
      configureFile = false,
    ) {
      val transpiler = VueKolarTranspiler(project)
      val files = getFilesForTranspilation(transpiler)

      assert(files.isNotEmpty())
    }
  }

  private fun getFilesForTranspilation(
    transpiler: KolarTranspiler,
  ): List<VirtualFile> {
    val files = mutableListOf<VirtualFile>()

    ProjectFileIndex.getInstance(project)
      .iterateContent { file ->
        if (file.isFile && transpiler.isEnabled(file)) {
          files.add(file)
        }
        true
      }

    return files
  }

  @Test
  fun `component-with-two-scripts`() {
    doTest()
  }

  @Test
  fun `component-with-two-scripts__vapor`() {
    doTest()
  }

  @Test
  fun `define-model-basic-unassigned`() {
    doTest()
  }

  @Test
  fun `define-model-basic-unassigned__vapor`() {
    doTest()
  }

  @Test
  fun `define-model-default-and-required`() {
    doTest()
  }

  @Test
  fun `define-model-default-and-required__vapor`() {
    doTest()
  }

  @Test
  fun `define-model-multiple-named`() {
    doTest()
  }

  @Test
  fun `define-model-multiple-named__vapor`() {
    doTest()
  }

  @Test
  fun `define-model-with-modifiers`() {
    doTest()
  }

  @Test
  fun `define-model-with-modifiers__vapor`() {
    doTest()
  }

  @Test
  fun `define-slots-basic`() {
    doTest()
  }

  @Test
  fun `define-slots-basic__vapor`() {
    doTest()
  }

  @Test
  fun `emits-array`() {
    doTest()
  }

  @Test
  fun `emits-array-assigned`() {
    doTest()
  }

  @Test
  fun `emits-array-assigned__vapor`() {
    doTest()
  }

  @Test
  fun `emits-array__vapor`() {
    doTest()
  }

  @Test
  fun `emits-consumption-parent-child`() {
    doTest()
  }

  @Test
  fun `emits-consumption-parent-child__vapor`() {
    doTest()
  }

  @Test
  fun `emits-dollar-emit-only`() {
    doTest()
  }

  @Test
  fun `emits-dollar-emit-only__vapor`() {
    doTest()
  }

  @Test
  fun `emits-multi-arg-payload`() {
    doTest()
  }

  @Test
  fun `emits-multi-arg-payload__vapor`() {
    doTest()
  }

  @Test
  fun `emits-object-validated`() {
    doTest()
  }

  @Test
  fun `emits-object-validated-assigned`() {
    doTest()
  }

  @Test
  fun `emits-object-validated-assigned__vapor`() {
    doTest()
  }

  @Test
  fun `emits-object-validated__vapor`() {
    doTest()
  }

  @Test
  fun `emits-type-call-signature`() {
    doTest()
  }

  @Test
  fun `emits-type-call-signature-assigned`() {
    doTest()
  }

  @Test
  fun `emits-type-call-signature-assigned__vapor`() {
    doTest()
  }

  @Test
  fun `emits-type-call-signature__vapor`() {
    doTest()
  }

  @Test
  fun `emits-type-tuple`() {
    doTest()
  }

  @Test
  fun `emits-type-tuple-assigned`() {
    doTest()
  }

  @Test
  fun `emits-type-tuple-assigned__vapor`() {
    doTest()
  }

  @Test
  fun `emits-type-tuple__vapor`() {
    doTest()
  }

  @Test
  fun `emits-with-destructured-props`() {
    doTest()
  }

  @Test
  fun `emits-with-destructured-props-defaults`() {
    doTest()
  }

  @Test
  fun `emits-with-destructured-props-defaults__vapor`() {
    doTest()
  }

  @Test
  fun `emits-with-destructured-props__vapor`() {
    doTest()
  }

  @Test
  fun `generic-component-basic`() {
    doTest()
  }

  @Test
  fun `generic-component-basic__vapor`() {
    doTest()
  }

  @Test
  fun `generic-component-with-emits`() {
    doTest()
  }

  @Test
  fun `generic-component-with-emits__vapor`() {
    doTest()
  }

  @Test
  fun `generic-component-with-slots-and-expose`() {
    doTest()
  }

  @Test
  fun `generic-component-with-slots-and-expose__vapor`() {
    doTest()
  }

  @Test
  fun `props-array`() {
    doTest()
  }

  @Test
  fun `props-array-destructured`() {
    doTest()
  }

  @Test
  fun `props-array-destructured__vapor`() {
    doTest()
  }

  @Test
  fun `props-array__vapor`() {
    doTest()
  }

  @Test
  fun `props-dollar-props`() {
    doTest()
  }

  @Test
  fun `props-dollar-props__vapor`() {
    doTest()
  }

  @Test
  fun `props-prop-type`() {
    doTest()
  }

  @Test
  fun `props-prop-type-destructured`() {
    doTest()
  }

  @Test
  fun `props-prop-type-destructured__vapor`() {
    doTest()
  }

  @Test
  fun `props-prop-type__vapor`() {
    doTest()
  }

  @Test
  fun `props-runtime-object`() {
    doTest()
  }

  @Test
  fun `props-runtime-object__vapor`() {
    doTest()
  }

  @Test
  fun `props-type-param`() {
    doTest()
  }

  @Test
  fun `props-type-param-destructured`() {
    doTest()
  }

  @Test
  fun `props-type-param-destructured-defaults`() {
    doTest()
  }

  @Test
  fun `props-type-param-destructured-defaults__vapor`() {
    doTest()
  }

  @Test
  fun `props-type-param-destructured-rest`() {
    doTest()
  }

  @Test
  fun `props-type-param-destructured-rest__vapor`() {
    doTest()
  }

  @Test
  fun `props-type-param-destructured__vapor`() {
    doTest()
  }

  @Test
  fun `props-type-param-with-defaults`() {
    doTest()
  }

  @Test
  fun `props-type-param-with-defaults__vapor`() {
    doTest()
  }

  @Test
  fun `props-type-param__vapor`() {
    doTest()
  }

  @Test
  fun `props-unassigned`() {
    doTest()
  }

  @Test
  fun `props-unassigned__vapor`() {
    doTest()
  }

  @Test
  fun `simple-app-with-components`() {
    doTest()
  }

  @Test
  fun `simple-app-with-components__vapor`() {
    doTest()
  }

  @Test
  fun `single-file-app`() {
    doTest()
  }

  @Test
  fun `single-file-app-with-refs`() {
    doTest()
  }

  @Test
  fun `v-for-array`() {
    doTest()
  }

  @Test
  fun `v-for-array__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-component`() {
    doTest()
  }

  @Test
  fun `v-for-component-ref-array`() {
    doTest()
  }

  @Test
  fun `v-for-component-ref-array__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-component__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-destructured-array`() {
    doTest()
  }

  @Test
  fun `v-for-destructured-array__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-destructured-object`() {
    doTest()
  }

  @Test
  fun `v-for-destructured-object__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-dynamic-slot-name`() {
    doTest()
  }

  @Test
  fun `v-for-dynamic-slot-name__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-key-only-usage`() {
    doTest()
  }

  @Test
  fun `v-for-key-only-usage__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-malformed-expression`() {
    doTest()
  }

  @Test
  fun `v-for-malformed-expression__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-map`() {
    doTest()
  }

  @Test
  fun `v-for-map__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-nested`() {
    doTest()
  }

  @Test
  fun `v-for-nested-destructured`() {
    doTest()
  }

  @Test
  fun `v-for-nested-destructured__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-nested__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-nullable-source`() {
    doTest()
  }

  @Test
  fun `v-for-nullable-source__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-number`() {
    doTest()
  }

  @Test
  fun `v-for-number__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-object-key-value-index`() {
    doTest()
  }

  @Test
  fun `v-for-object-key-value-index__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-of-keyword`() {
    doTest()
  }

  @Test
  fun `v-for-of-keyword__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-ref-array`() {
    doTest()
  }

  @Test
  fun `v-for-ref-array__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-set`() {
    doTest()
  }

  @Test
  fun `v-for-set__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-string`() {
    doTest()
  }

  @Test
  fun `v-for-string__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-template-fragment-key`() {
    doTest()
  }

  @Test
  fun `v-for-template-fragment-key__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-template-with-slot`() {
    doTest()
  }

  @Test
  fun `v-for-template-with-slot__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-union-source`() {
    doTest()
  }

  @Test
  fun `v-for-union-source__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-with-index`() {
    doTest()
  }

  @Test
  fun `v-for-with-index__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-with-v-memo`() {
    doTest()
  }

  @Test
  fun `v-for-with-v-memo__vapor`() {
    doTest()
  }

  @Test
  fun `v-for-with-v-once`() {
    doTest()
  }

  @Test
  fun `v-for-with-v-once__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-condition-compound-expression`() {
    doTest()
  }

  @Test
  fun `v-if-condition-compound-expression__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-condition-computed-key`() {
    doTest()
  }

  @Test
  fun `v-if-condition-computed-key__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-condition-logical-operators`() {
    doTest()
  }

  @Test
  fun `v-if-condition-logical-operators__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-condition-optional-chaining`() {
    doTest()
  }

  @Test
  fun `v-if-condition-optional-chaining__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-condition-typeof-narrowing`() {
    doTest()
  }

  @Test
  fun `v-if-condition-typeof-narrowing__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-dynamic-component`() {
    doTest()
  }

  @Test
  fun `v-if-dynamic-component__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-else`() {
    doTest()
  }

  @Test
  fun `v-if-else-as-single-root`() {
    doTest()
  }

  @Test
  fun `v-if-else-as-single-root__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-else-different-components`() {
    doTest()
  }

  @Test
  fun `v-if-else-different-components__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-else-if-else`() {
    doTest()
  }

  @Test
  fun `v-if-else-if-else__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-else-whitespace-comments`() {
    doTest()
  }

  @Test
  fun `v-if-else-whitespace-comments__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-else__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-implicit-slot-child`() {
    doTest()
  }

  @Test
  fun `v-if-implicit-slot-child__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-inside-v-for`() {
    doTest()
  }

  @Test
  fun `v-if-inside-v-for__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-narrowing-in-event-handler`() {
    doTest()
  }

  @Test
  fun `v-if-narrowing-in-event-handler__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-narrowing-in-v-model`() {
    doTest()
  }

  @Test
  fun `v-if-narrowing-in-v-model__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-narrowing-to-child-component-event`() {
    doTest()
  }

  @Test
  fun `v-if-narrowing-to-child-component-event__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-only`() {
    doTest()
  }

  @Test
  fun `v-if-only__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-template-fragment`() {
    doTest()
  }

  @Test
  fun `v-if-template-fragment__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-template-with-key`() {
    doTest()
  }

  @Test
  fun `v-if-template-with-key__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-template-with-slot`() {
    doTest()
  }

  @Test
  fun `v-if-template-with-slot__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-with-v-for-same-element`() {
    doTest()
  }

  @Test
  fun `v-if-with-v-for-same-element__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-with-v-memo`() {
    doTest()
  }

  @Test
  fun `v-if-with-v-memo__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-with-v-model-component`() {
    doTest()
  }

  @Test
  fun `v-if-with-v-model-component__vapor`() {
    doTest()
  }

  @Test
  fun `v-if-with-v-show`() {
    doTest()
  }

  @Test
  fun `v-if-with-v-show__vapor`() {
    doTest()
  }

}
