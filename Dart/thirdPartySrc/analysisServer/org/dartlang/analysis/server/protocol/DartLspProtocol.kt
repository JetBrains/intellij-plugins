package org.dartlang.analysis.server.protocol

data class DartLspApplyWorkspaceEditParams(val workspaceEdit: DartLspWorkspaceEdit, val label: String? = null)

data class DartLspApplyWorkspaceEditResult(val applied: Boolean)

data class DartLspWorkspaceEdit(val changes: Map<String, List<DartLspTextEdit>>?)

data class DartLspTextEdit(val range: DartLspRange, val newText: String)

data class DartLspRange(val start: DartLspPosition, val end: DartLspPosition)

data class DartLspPosition(val line: Int, val character: Int)
