package com.intellij.lang.javascript.linter.jshint

import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import javax.swing.JCheckBox
import javax.swing.JPanel

internal class JSHintViewContent(
  val jshintPackageField: NodePackageField,
  configFileUsedCheckBox: JCheckBox,
  cardPanel: JPanel,
) {

  val panel = panel {
    indent {
      row(JSHintBundle.message("jshint.settings.package.label")) {
        cell(jshintPackageField).align(AlignX.FILL)
      }
      row {
        cell(configFileUsedCheckBox)
      }.bottomGap(BottomGap.SMALL)
      row {
        cell(cardPanel).align(Align.FILL)
      }.resizableRow()
    }
  }
}
