// This is a generated file. Not intended for manual editing.
package org.jetbrains.webstorm.lang.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.webstorm.lang.psi.impl.*;

public interface WebAssemblyTypes {

  IElementType ALIGNEQ = new WebAssemblyElementType("ALIGNEQ");
  IElementType BLOCKINSTR = new WebAssemblyElementType("BLOCKINSTR");
  IElementType BLOCKTYPE = new WebAssemblyElementType("BLOCKTYPE");
  IElementType CALL_INDIRECT_INSTR = new WebAssemblyElementType("CALL_INDIRECT_INSTR");
  IElementType CALL_INSTR = new WebAssemblyElementType("CALL_INSTR");
  IElementType COMMENT = new WebAssemblyElementType("COMMENT");
  IElementType DATA = new WebAssemblyElementType("DATA");
  IElementType ELEM = new WebAssemblyElementType("ELEM");
  IElementType ELEMLIST = new WebAssemblyElementType("ELEMLIST");
  IElementType ELEM_DROP_INSTR = new WebAssemblyElementType("ELEM_DROP_INSTR");
  IElementType EXPORT = new WebAssemblyElementType("EXPORT");
  IElementType EXPORTDESC = new WebAssemblyElementType("EXPORTDESC");
  IElementType FOLDEINSTR = new WebAssemblyElementType("FOLDEINSTR");
  IElementType FUNC = new WebAssemblyElementType("FUNC");
  IElementType FUNCTYPE = new WebAssemblyElementType("FUNCTYPE");
  IElementType GLOBAL = new WebAssemblyElementType("GLOBAL");
  IElementType GLOBALTYPE = new WebAssemblyElementType("GLOBALTYPE");
  IElementType GLOBAL_INSTR = new WebAssemblyElementType("GLOBAL_INSTR");
  IElementType IDX = new WebAssemblyElementType("IDX");
  IElementType IMPORT = new WebAssemblyElementType("IMPORT");
  IElementType IMPORTDESC = new WebAssemblyElementType("IMPORTDESC");
  IElementType INLINE_DATA = new WebAssemblyElementType("INLINE_DATA");
  IElementType INLINE_ELEM = new WebAssemblyElementType("INLINE_ELEM");
  IElementType INLINE_EXPORT = new WebAssemblyElementType("INLINE_EXPORT");
  IElementType INLINE_IMPORT = new WebAssemblyElementType("INLINE_IMPORT");
  IElementType INSTR = new WebAssemblyElementType("INSTR");
  IElementType LEXER_TOKENS = new WebAssemblyElementType("LEXER_TOKENS");
  IElementType LOCAL = new WebAssemblyElementType("LOCAL");
  IElementType LOCAL_INSTR = new WebAssemblyElementType("LOCAL_INSTR");
  IElementType MEM = new WebAssemblyElementType("MEM");
  IElementType MEMORY_IDX_INSTR = new WebAssemblyElementType("MEMORY_IDX_INSTR");
  IElementType MEMTYPE = new WebAssemblyElementType("MEMTYPE");
  IElementType MODULE = new WebAssemblyElementType("MODULE");
  IElementType MODULEFIELD = new WebAssemblyElementType("MODULEFIELD");
  IElementType OFFSETEQ = new WebAssemblyElementType("OFFSETEQ");
  IElementType PARAM = new WebAssemblyElementType("PARAM");
  IElementType PLAININSTR = new WebAssemblyElementType("PLAININSTR");
  IElementType REF_FUNC_INSTR = new WebAssemblyElementType("REF_FUNC_INSTR");
  IElementType RESULT = new WebAssemblyElementType("RESULT");
  IElementType START = new WebAssemblyElementType("START");
  IElementType TABLE = new WebAssemblyElementType("TABLE");
  IElementType TABLETYPE = new WebAssemblyElementType("TABLETYPE");
  IElementType TABLE_COPY_INSTR = new WebAssemblyElementType("TABLE_COPY_INSTR");
  IElementType TABLE_IDX_INSTR = new WebAssemblyElementType("TABLE_IDX_INSTR");
  IElementType TABLE_INIT_INSTR = new WebAssemblyElementType("TABLE_INIT_INSTR");
  IElementType TYPE = new WebAssemblyElementType("TYPE");
  IElementType TYPEUSE = new WebAssemblyElementType("TYPEUSE");
  IElementType TYPEUSE_TYPEREF = new WebAssemblyElementType("TYPEUSE_TYPEREF");
  IElementType VALTYPE = new WebAssemblyElementType("VALTYPE");

  IElementType ALIGNEQKEY = new WebAssemblyTokenType("ALIGNEQKEY");
  IElementType BAD_TOKEN = new WebAssemblyTokenType("BAD_TOKEN");
  IElementType BLOCKKEY = new WebAssemblyTokenType("BLOCKKEY");
  IElementType BLOCK_COMMENT = new WebAssemblyTokenType("BLOCK_COMMENT");
  IElementType BRTABLEINSTR = new WebAssemblyTokenType("BRTABLEINSTR");
  IElementType CALLINDIRECTINSTR = new WebAssemblyTokenType("CALLINDIRECTINSTR");
  IElementType CALLINSTR = new WebAssemblyTokenType("CALLINSTR");
  IElementType CONTROLINSTR = new WebAssemblyTokenType("CONTROLINSTR");
  IElementType CONTROLINSTR_IDX = new WebAssemblyTokenType("CONTROLINSTR_IDX");
  IElementType DATAKEY = new WebAssemblyTokenType("DATAKEY");
  IElementType DECLAREKEY = new WebAssemblyTokenType("DECLAREKEY");
  IElementType ELEMDROPINSTR = new WebAssemblyTokenType("ELEMDROPINSTR");
  IElementType ELEMKEY = new WebAssemblyTokenType("ELEMKEY");
  IElementType ELSEKEY = new WebAssemblyTokenType("ELSEKEY");
  IElementType ENDKEY = new WebAssemblyTokenType("ENDKEY");
  IElementType EXPORTKEY = new WebAssemblyTokenType("EXPORTKEY");
  IElementType EXTERNKEY = new WebAssemblyTokenType("EXTERNKEY");
  IElementType FCONST = new WebAssemblyTokenType("FCONST");
  IElementType FLOAT = new WebAssemblyTokenType("FLOAT");
  IElementType FUNCKEY = new WebAssemblyTokenType("FUNCKEY");
  IElementType GLOBALINSTR = new WebAssemblyTokenType("GLOBALINSTR");
  IElementType GLOBALKEY = new WebAssemblyTokenType("GLOBALKEY");
  IElementType ICONST = new WebAssemblyTokenType("ICONST");
  IElementType IDENTIFIER = new WebAssemblyTokenType("IDENTIFIER");
  IElementType IFKEY = new WebAssemblyTokenType("IFKEY");
  IElementType IMPORTKEY = new WebAssemblyTokenType("IMPORTKEY");
  IElementType ITEMKEY = new WebAssemblyTokenType("ITEMKEY");
  IElementType LINE_COMMENT = new WebAssemblyTokenType("LINE_COMMENT");
  IElementType LOCALINSTR = new WebAssemblyTokenType("LOCALINSTR");
  IElementType LOCALKEY = new WebAssemblyTokenType("LOCALKEY");
  IElementType LOOPKEY = new WebAssemblyTokenType("LOOPKEY");
  IElementType LPAR = new WebAssemblyTokenType("LPAR");
  IElementType MEMORYINSTR = new WebAssemblyTokenType("MEMORYINSTR");
  IElementType MEMORYINSTR_IDX = new WebAssemblyTokenType("MEMORYINSTR_IDX");
  IElementType MEMORYINSTR_MEMARG = new WebAssemblyTokenType("MEMORYINSTR_MEMARG");
  IElementType MEMORYKEY = new WebAssemblyTokenType("MEMORYKEY");
  IElementType MODULEKEY = new WebAssemblyTokenType("MODULEKEY");
  IElementType MUTKEY = new WebAssemblyTokenType("MUTKEY");
  IElementType NUMERICINSTR = new WebAssemblyTokenType("NUMERICINSTR");
  IElementType NUMTYPE = new WebAssemblyTokenType("NUMTYPE");
  IElementType OFFSETEQKEY = new WebAssemblyTokenType("OFFSETEQKEY");
  IElementType OFFSETKEY = new WebAssemblyTokenType("OFFSETKEY");
  IElementType PARAMETRICINSTR = new WebAssemblyTokenType("PARAMETRICINSTR");
  IElementType PARAMKEY = new WebAssemblyTokenType("PARAMKEY");
  IElementType REFFUNCINSTR = new WebAssemblyTokenType("REFFUNCINSTR");
  IElementType REFISNULLINST = new WebAssemblyTokenType("REFISNULLINST");
  IElementType REFNULLINSTR = new WebAssemblyTokenType("REFNULLINSTR");
  IElementType REFTYPE = new WebAssemblyTokenType("REFTYPE");
  IElementType RESULTKEY = new WebAssemblyTokenType("RESULTKEY");
  IElementType RPAR = new WebAssemblyTokenType("RPAR");
  IElementType SIGNED = new WebAssemblyTokenType("SIGNED");
  IElementType STARTKEY = new WebAssemblyTokenType("STARTKEY");
  IElementType STRING = new WebAssemblyTokenType("STRING");
  IElementType TABLECOPYINSTR = new WebAssemblyTokenType("TABLECOPYINSTR");
  IElementType TABLEINITINSTR = new WebAssemblyTokenType("TABLEINITINSTR");
  IElementType TABLEINSTR_IDX = new WebAssemblyTokenType("TABLEINSTR_IDX");
  IElementType TABLEKEY = new WebAssemblyTokenType("TABLEKEY");
  IElementType THENKEY = new WebAssemblyTokenType("THENKEY");
  IElementType TYPEKEY = new WebAssemblyTokenType("TYPEKEY");
  IElementType UNSIGNED = new WebAssemblyTokenType("UNSIGNED");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ALIGNEQ) {
        return new WebAssemblyAligneqImpl(node);
      }
      else if (type == BLOCKINSTR) {
        return new WebAssemblyBlockinstrImpl(node);
      }
      else if (type == BLOCKTYPE) {
        return new WebAssemblyBlocktypeImpl(node);
      }
      else if (type == CALL_INDIRECT_INSTR) {
        return new WebAssemblyCallIndirectInstrImpl(node);
      }
      else if (type == CALL_INSTR) {
        return new WebAssemblyCallInstrImpl(node);
      }
      else if (type == COMMENT) {
        return new WebAssemblyCommentImpl(node);
      }
      else if (type == DATA) {
        return new WebAssemblyDataImpl(node);
      }
      else if (type == ELEM) {
        return new WebAssemblyElemImpl(node);
      }
      else if (type == ELEMLIST) {
        return new WebAssemblyElemlistImpl(node);
      }
      else if (type == ELEM_DROP_INSTR) {
        return new WebAssemblyElemDropInstrImpl(node);
      }
      else if (type == EXPORT) {
        return new WebAssemblyExportImpl(node);
      }
      else if (type == EXPORTDESC) {
        return new WebAssemblyExportdescImpl(node);
      }
      else if (type == FOLDEINSTR) {
        return new WebAssemblyFoldeinstrImpl(node);
      }
      else if (type == FUNC) {
        return new WebAssemblyFuncImpl(node);
      }
      else if (type == FUNCTYPE) {
        return new WebAssemblyFunctypeImpl(node);
      }
      else if (type == GLOBAL) {
        return new WebAssemblyGlobalImpl(node);
      }
      else if (type == GLOBALTYPE) {
        return new WebAssemblyGlobaltypeImpl(node);
      }
      else if (type == GLOBAL_INSTR) {
        return new WebAssemblyGlobalInstrImpl(node);
      }
      else if (type == IDX) {
        return new WebAssemblyIdxImpl(node);
      }
      else if (type == IMPORT) {
        return new WebAssemblyImportImpl(node);
      }
      else if (type == IMPORTDESC) {
        return new WebAssemblyImportdescImpl(node);
      }
      else if (type == INLINE_DATA) {
        return new WebAssemblyInlineDataImpl(node);
      }
      else if (type == INLINE_ELEM) {
        return new WebAssemblyInlineElemImpl(node);
      }
      else if (type == INLINE_EXPORT) {
        return new WebAssemblyInlineExportImpl(node);
      }
      else if (type == INLINE_IMPORT) {
        return new WebAssemblyInlineImportImpl(node);
      }
      else if (type == INSTR) {
        return new WebAssemblyInstrImpl(node);
      }
      else if (type == LEXER_TOKENS) {
        return new WebAssemblyLexerTokensImpl(node);
      }
      else if (type == LOCAL) {
        return new WebAssemblyLocalImpl(node);
      }
      else if (type == LOCAL_INSTR) {
        return new WebAssemblyLocalInstrImpl(node);
      }
      else if (type == MEM) {
        return new WebAssemblyMemImpl(node);
      }
      else if (type == MEMORY_IDX_INSTR) {
        return new WebAssemblyMemoryIdxInstrImpl(node);
      }
      else if (type == MEMTYPE) {
        return new WebAssemblyMemtypeImpl(node);
      }
      else if (type == MODULE) {
        return new WebAssemblyModuleImpl(node);
      }
      else if (type == MODULEFIELD) {
        return new WebAssemblyModulefieldImpl(node);
      }
      else if (type == OFFSETEQ) {
        return new WebAssemblyOffseteqImpl(node);
      }
      else if (type == PARAM) {
        return new WebAssemblyParamImpl(node);
      }
      else if (type == PLAININSTR) {
        return new WebAssemblyPlaininstrImpl(node);
      }
      else if (type == REF_FUNC_INSTR) {
        return new WebAssemblyRefFuncInstrImpl(node);
      }
      else if (type == RESULT) {
        return new WebAssemblyResultImpl(node);
      }
      else if (type == START) {
        return new WebAssemblyStartImpl(node);
      }
      else if (type == TABLE) {
        return new WebAssemblyTableImpl(node);
      }
      else if (type == TABLETYPE) {
        return new WebAssemblyTabletypeImpl(node);
      }
      else if (type == TABLE_COPY_INSTR) {
        return new WebAssemblyTableCopyInstrImpl(node);
      }
      else if (type == TABLE_IDX_INSTR) {
        return new WebAssemblyTableIdxInstrImpl(node);
      }
      else if (type == TABLE_INIT_INSTR) {
        return new WebAssemblyTableInitInstrImpl(node);
      }
      else if (type == TYPE) {
        return new WebAssemblyTypeImpl(node);
      }
      else if (type == TYPEUSE) {
        return new WebAssemblyTypeuseImpl(node);
      }
      else if (type == TYPEUSE_TYPEREF) {
        return new WebAssemblyTypeuseTyperefImpl(node);
      }
      else if (type == VALTYPE) {
        return new WebAssemblyValtypeImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
