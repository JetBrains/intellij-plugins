package org.jetbrains.vuejs.language

interface VueHandledLexer {
  fun seenScript():Boolean
  fun setSeenScriptType()
  fun seenStyle():Boolean
  fun setSeenStyleType()
  fun seenTag():Boolean
}