package com.intellij.protobuf.ide.settings

import com.intellij.protobuf.ide.settings.PbLanguageSettingsForm.ImportPathGroup
import com.intellij.util.ui.ListTableModel

internal fun findIndexToInsertGroup(importPathModel: ListTableModel<PbLanguageSettingsForm.ImportPath>,
                                    groupToInsert: ImportPathGroup): Int {
  return importPathModel.items.asSequence()
           .mapIndexedNotNull { index, group ->
             if (group is ImportPathGroup) index to group.order else null
           }
           .firstOrNull { (_, order) ->
             order > groupToInsert.order
           }
           ?.first
         ?: importPathModel.items.size
}