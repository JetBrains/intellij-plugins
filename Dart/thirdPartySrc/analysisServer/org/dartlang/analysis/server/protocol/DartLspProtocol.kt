package org.dartlang.analysis.server.protocol

class DartLspApplyWorkspaceEditParams(val workspaceEdit: DartLspWorkspaceEdit, val label: String? = null)

class DartLspApplyWorkspaceEditResult(val applied: Boolean)

class DartLspWorkspaceEdit(val changes: Map<String, List<DartLspTextEdit>>?, val documentChanges: List<DartLspDocumentChange>?)

class DartLspTextEdit(val range: DartLspRange, val newText: String)

interface DartLspDocumentChange

class DartLspTextDocumentEdit() : DartLspDocumentChange

class DartLspCreateFile() : DartLspDocumentChange

class DartLspRenameFile() : DartLspDocumentChange

class DartLspDeleteFile() : DartLspDocumentChange

class DartLspRange(val start: DartLspPosition, val end: DartLspPosition)

class DartLspPosition(val line: Int, val character: Int)
