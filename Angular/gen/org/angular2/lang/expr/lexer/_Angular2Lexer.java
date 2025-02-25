// Generated by JFlex 1.9.1 http://jflex.de/  (tweaked for IntelliJ platform)
// source: Angular2.flex

package org.angular2.lang.expr.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;

import org.angular2.codeInsight.blocks.Angular2HtmlBlockUtilsKt;

import static com.intellij.lang.javascript.JSTokenTypes.*;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.angular2.lang.expr.lexer.Angular2TokenTypes.*;


class _Angular2Lexer implements FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int YYEXPRESSION = 2;
  public static final int YYSTRING = 4;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = {
     0,  0,  1,  1,  2, 2
  };

  /**
   * Top-level table for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_TOP = zzUnpackcmap_top();

  private static final String ZZ_CMAP_TOP_PACKED_0 =
    "\1\0\1\u0100\1\u0200\1\u0300\1\u0400\1\u0500\1\u0600\1\u0700"+
    "\1\u0800\1\u0900\1\u0a00\1\u0b00\1\u0c00\1\u0d00\1\u0e00\1\u0f00"+
    "\1\u1000\1\u0100\1\u1100\1\u1200\1\u1300\1\u0100\1\u1400\1\u1500"+
    "\1\u1600\1\u1700\1\u1800\1\u1900\1\u1a00\1\u1b00\1\u0100\1\u1c00"+
    "\1\u1d00\1\u1e00\12\u1f00\1\u2000\1\u2100\1\u2200\1\u1f00\1\u2300"+
    "\1\u2400\2\u1f00\31\u0100\1\u2500\121\u0100\1\u2600\4\u0100\1\u2700"+
    "\1\u0100\1\u2800\1\u2900\1\u2a00\1\u2b00\1\u2c00\1\u2d00\53\u0100"+
    "\1\u2e00\41\u1f00\1\u0100\1\u2f00\1\u3000\1\u0100\1\u3100\1\u3200"+
    "\1\u3300\1\u3400\1\u1f00\1\u3500\1\u3600\1\u3700\1\u3800\1\u0100"+
    "\1\u3900\1\u3a00\1\u3b00\1\u3c00\1\u3d00\1\u3e00\1\u3f00\1\u1f00"+
    "\1\u4000\1\u4100\1\u4200\1\u4300\1\u4400\1\u4500\1\u4600\1\u4700"+
    "\1\u4800\1\u4900\1\u4a00\1\u4b00\1\u1f00\1\u4c00\1\u4d00\1\u4e00"+
    "\1\u1f00\3\u0100\1\u4f00\1\u5000\1\u5100\12\u1f00\4\u0100\1\u5200"+
    "\17\u1f00\2\u0100\1\u5300\41\u1f00\2\u0100\1\u5400\1\u5500\2\u1f00"+
    "\1\u5600\1\u5700\27\u0100\1\u5800\2\u0100\1\u5900\45\u1f00\1\u0100"+
    "\1\u5a00\1\u5b00\11\u1f00\1\u5c00\27\u1f00\1\u5d00\1\u5e00\1\u5f00"+
    "\1\u6000\11\u1f00\1\u6100\1\u6200\5\u1f00\1\u6300\1\u6400\4\u1f00"+
    "\1\u6500\21\u1f00\246\u0100\1\u6600\20\u0100\1\u6700\1\u6800\25\u0100"+
    "\1\u6900\34\u0100\1\u6a00\14\u1f00\2\u0100\1\u6b00\u0e05\u1f00";

  private static int [] zzUnpackcmap_top() {
    int [] result = new int[4352];
    int offset = 0;
    offset = zzUnpackcmap_top(ZZ_CMAP_TOP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_top(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Second-level tables for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_BLOCKS = zzUnpackcmap_blocks();

  private static final String ZZ_CMAP_BLOCKS_PACKED_0 =
    "\11\0\1\1\1\2\1\1\1\0\1\3\22\0\1\1"+
    "\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13"+
    "\1\14\1\15\1\16\1\17\1\20\1\21\1\22\12\23"+
    "\1\24\1\25\1\26\1\27\1\30\1\31\1\0\4\32"+
    "\1\33\1\32\21\34\1\35\2\34\1\36\1\37\1\40"+
    "\1\41\1\34\1\0\1\42\1\32\1\43\1\44\1\45"+
    "\1\46\1\34\1\47\1\50\2\34\1\51\1\34\1\52"+
    "\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62"+
    "\1\34\1\35\1\63\1\34\1\64\1\65\1\66\42\0"+
    "\1\1\11\0\1\67\12\0\1\67\4\0\1\67\5\0"+
    "\27\67\1\0\37\67\1\0\u01ca\67\4\0\14\67\16\0"+
    "\5\67\7\0\1\67\1\0\1\67\201\0\5\67\1\0"+
    "\2\67\2\0\4\67\1\0\1\67\6\0\1\67\1\0"+
    "\3\67\1\0\1\67\1\0\24\67\1\0\123\67\1\0"+
    "\213\67\10\0\246\67\1\0\46\67\2\0\1\67\6\0"+
    "\51\67\107\0\33\67\4\0\4\67\55\0\53\67\43\0"+
    "\2\67\1\0\143\67\1\0\1\67\17\0\2\67\7\0"+
    "\2\67\12\0\3\67\2\0\1\67\20\0\1\67\1\0"+
    "\36\67\35\0\131\67\13\0\1\67\30\0\41\67\11\0"+
    "\2\67\4\0\1\67\5\0\26\67\4\0\1\67\11\0"+
    "\1\67\3\0\1\67\27\0\31\67\7\0\13\67\65\0"+
    "\25\67\1\0\10\67\106\0\66\67\3\0\1\67\22\0"+
    "\1\67\7\0\12\67\17\0\20\67\4\0\10\67\2\0"+
    "\2\67\2\0\26\67\1\0\7\67\1\0\1\67\3\0"+
    "\4\67\3\0\1\67\20\0\1\67\15\0\2\67\1\0"+
    "\3\67\16\0\2\67\12\0\1\67\10\0\6\67\4\0"+
    "\2\67\2\0\26\67\1\0\7\67\1\0\2\67\1\0"+
    "\2\67\1\0\2\67\37\0\4\67\1\0\1\67\23\0"+
    "\3\67\20\0\11\67\1\0\3\67\1\0\26\67\1\0"+
    "\7\67\1\0\2\67\1\0\5\67\3\0\1\67\22\0"+
    "\1\67\17\0\2\67\27\0\1\67\13\0\10\67\2\0"+
    "\2\67\2\0\26\67\1\0\7\67\1\0\2\67\1\0"+
    "\5\67\3\0\1\67\36\0\2\67\1\0\3\67\17\0"+
    "\1\67\21\0\1\67\1\0\6\67\3\0\3\67\1\0"+
    "\4\67\3\0\2\67\1\0\1\67\1\0\2\67\3\0"+
    "\2\67\3\0\3\67\3\0\14\67\26\0\1\67\64\0"+
    "\10\67\1\0\3\67\1\0\27\67\1\0\20\67\3\0"+
    "\1\67\32\0\3\67\5\0\2\67\36\0\1\67\4\0"+
    "\10\67\1\0\3\67\1\0\27\67\1\0\12\67\1\0"+
    "\5\67\3\0\1\67\40\0\1\67\1\0\2\67\17\0"+
    "\2\67\22\0\10\67\1\0\3\67\1\0\51\67\2\0"+
    "\1\67\20\0\1\67\5\0\3\67\10\0\3\67\30\0"+
    "\6\67\5\0\22\67\3\0\30\67\1\0\11\67\1\0"+
    "\1\67\2\0\7\67\72\0\60\67\1\0\2\67\14\0"+
    "\7\67\72\0\2\67\1\0\1\67\1\0\5\67\1\0"+
    "\30\67\1\0\1\67\1\0\12\67\1\0\2\67\11\0"+
    "\1\67\2\0\5\67\1\0\1\67\25\0\4\67\40\0"+
    "\1\67\77\0\10\67\1\0\44\67\33\0\5\67\163\0"+
    "\53\67\24\0\1\67\20\0\6\67\4\0\4\67\3\0"+
    "\1\67\3\0\2\67\7\0\3\67\4\0\15\67\14\0"+
    "\1\67\21\0\46\67\1\0\1\67\5\0\1\67\2\0"+
    "\53\67\1\0\115\67\1\0\4\67\2\0\7\67\1\0"+
    "\1\67\1\0\4\67\2\0\51\67\1\0\4\67\2\0"+
    "\41\67\1\0\4\67\2\0\7\67\1\0\1\67\1\0"+
    "\4\67\2\0\17\67\1\0\71\67\1\0\4\67\2\0"+
    "\103\67\45\0\20\67\20\0\126\67\2\0\6\67\3\0"+
    "\u016c\67\2\0\21\67\1\0\32\67\5\0\113\67\6\0"+
    "\10\67\7\0\15\67\1\0\4\67\16\0\22\67\16\0"+
    "\22\67\16\0\15\67\1\0\3\67\17\0\64\67\43\0"+
    "\1\67\4\0\1\67\103\0\131\67\7\0\5\67\2\0"+
    "\42\67\1\0\1\67\5\0\106\67\12\0\37\67\61\0"+
    "\36\67\2\0\5\67\13\0\54\67\4\0\32\67\66\0"+
    "\27\67\11\0\65\67\122\0\1\67\135\0\57\67\21\0"+
    "\7\67\67\0\36\67\15\0\2\67\12\0\54\67\32\0"+
    "\44\67\51\0\3\67\12\0\44\67\2\0\11\67\7\0"+
    "\53\67\2\0\3\67\51\0\4\67\1\0\6\67\1\0"+
    "\2\67\3\0\1\67\5\0\300\67\100\0\26\67\2\0"+
    "\6\67\2\0\46\67\2\0\6\67\2\0\10\67\1\0"+
    "\1\67\1\0\1\67\1\0\1\67\1\0\37\67\2\0"+
    "\65\67\1\0\7\67\1\0\1\67\3\0\3\67\1\0"+
    "\7\67\3\0\4\67\2\0\6\67\4\0\15\67\5\0"+
    "\3\67\1\0\7\67\164\0\1\67\15\0\1\67\20\0"+
    "\15\67\145\0\1\67\4\0\1\67\2\0\12\67\1\0"+
    "\1\67\3\0\5\67\6\0\1\67\1\0\1\67\1\0"+
    "\1\67\1\0\4\67\1\0\13\67\2\0\4\67\5\0"+
    "\5\67\4\0\1\67\64\0\2\67\u017b\0\57\67\1\0"+
    "\57\67\1\0\205\67\6\0\4\67\3\0\2\67\14\0"+
    "\46\67\1\0\1\67\5\0\1\67\2\0\70\67\7\0"+
    "\1\67\20\0\27\67\11\0\7\67\1\0\7\67\1\0"+
    "\7\67\1\0\7\67\1\0\7\67\1\0\7\67\1\0"+
    "\7\67\1\0\7\67\120\0\1\67\325\0\2\67\52\0"+
    "\5\67\5\0\2\67\4\0\126\67\6\0\3\67\1\0"+
    "\132\67\1\0\4\67\5\0\53\67\1\0\136\67\21\0"+
    "\33\67\65\0\306\67\112\0\360\67\20\0\215\67\103\0"+
    "\56\67\2\0\15\67\3\0\20\67\12\0\2\67\24\0"+
    "\57\67\20\0\37\67\2\0\106\67\61\0\11\67\2\0"+
    "\147\67\2\0\65\67\2\0\5\67\60\0\13\67\1\0"+
    "\3\67\1\0\4\67\1\0\27\67\35\0\64\67\16\0"+
    "\62\67\76\0\6\67\3\0\1\67\1\0\2\67\13\0"+
    "\34\67\12\0\27\67\31\0\35\67\7\0\57\67\34\0"+
    "\1\67\20\0\5\67\1\0\12\67\12\0\5\67\1\0"+
    "\51\67\27\0\3\67\1\0\10\67\24\0\27\67\3\0"+
    "\1\67\3\0\62\67\1\0\1\67\3\0\2\67\2\0"+
    "\5\67\2\0\1\67\1\0\1\67\30\0\3\67\2\0"+
    "\13\67\7\0\3\67\14\0\6\67\2\0\6\67\2\0"+
    "\6\67\11\0\7\67\1\0\7\67\1\0\53\67\1\0"+
    "\14\67\10\0\163\67\35\0\244\67\14\0\27\67\4\0"+
    "\61\67\4\0\156\67\2\0\152\67\46\0\7\67\14\0"+
    "\5\67\5\0\1\67\1\0\12\67\1\0\15\67\1\0"+
    "\5\67\1\0\1\67\1\0\2\67\1\0\2\67\1\0"+
    "\154\67\41\0\153\67\22\0\100\67\2\0\66\67\50\0"+
    "\14\67\164\0\5\67\1\0\207\67\44\0\32\67\6\0"+
    "\32\67\13\0\131\67\3\0\6\67\2\0\6\67\2\0"+
    "\6\67\2\0\3\67\43\0\14\67\1\0\32\67\1\0"+
    "\23\67\1\0\2\67\1\0\17\67\2\0\16\67\42\0"+
    "\173\67\205\0\35\67\3\0\61\67\57\0\40\67\15\0"+
    "\24\67\1\0\10\67\6\0\46\67\12\0\36\67\2\0"+
    "\44\67\4\0\10\67\60\0\236\67\22\0\44\67\4\0"+
    "\44\67\4\0\50\67\10\0\64\67\234\0\67\67\11\0"+
    "\26\67\12\0\10\67\230\0\6\67\2\0\1\67\1\0"+
    "\54\67\1\0\2\67\3\0\1\67\2\0\27\67\12\0"+
    "\27\67\11\0\37\67\101\0\23\67\1\0\2\67\12\0"+
    "\26\67\12\0\32\67\106\0\70\67\6\0\2\67\100\0"+
    "\1\67\17\0\4\67\1\0\3\67\1\0\35\67\52\0"+
    "\35\67\3\0\35\67\43\0\10\67\1\0\34\67\33\0"+
    "\66\67\12\0\26\67\12\0\23\67\15\0\22\67\156\0"+
    "\111\67\67\0\63\67\15\0\63\67\15\0\44\67\334\0"+
    "\35\67\12\0\1\67\10\0\26\67\232\0\27\67\14\0"+
    "\65\67\113\0\55\67\40\0\31\67\32\0\44\67\35\0"+
    "\1\67\13\0\43\67\3\0\1\67\14\0\60\67\16\0"+
    "\4\67\25\0\1\67\1\0\1\67\43\0\22\67\1\0"+
    "\31\67\124\0\7\67\1\0\1\67\1\0\4\67\1\0"+
    "\17\67\1\0\12\67\7\0\57\67\46\0\10\67\2\0"+
    "\2\67\2\0\26\67\1\0\7\67\1\0\2\67\1\0"+
    "\5\67\3\0\1\67\22\0\1\67\14\0\5\67\236\0"+
    "\65\67\22\0\4\67\24\0\1\67\40\0\60\67\24\0"+
    "\2\67\1\0\1\67\270\0\57\67\51\0\4\67\44\0"+
    "\60\67\24\0\1\67\73\0\53\67\15\0\1\67\107\0"+
    "\33\67\345\0\54\67\164\0\100\67\37\0\1\67\240\0"+
    "\10\67\2\0\47\67\20\0\1\67\1\0\1\67\34\0"+
    "\1\67\12\0\50\67\7\0\1\67\25\0\1\67\13\0"+
    "\56\67\23\0\1\67\42\0\71\67\7\0\11\67\1\0"+
    "\45\67\21\0\1\67\61\0\36\67\160\0\7\67\1\0"+
    "\2\67\1\0\46\67\25\0\1\67\31\0\6\67\1\0"+
    "\2\67\1\0\40\67\16\0\1\67\u0147\0\23\67\15\0"+
    "\232\67\346\0\304\67\274\0\57\67\321\0\107\67\271\0"+
    "\71\67\7\0\37\67\161\0\36\67\22\0\60\67\20\0"+
    "\4\67\37\0\25\67\5\0\23\67\260\0\100\67\200\0"+
    "\113\67\5\0\1\67\102\0\15\67\100\0\2\67\1\0"+
    "\1\67\34\0\370\67\10\0\363\67\15\0\37\67\61\0"+
    "\3\67\21\0\4\67\10\0\u018c\67\4\0\153\67\5\0"+
    "\15\67\3\0\11\67\7\0\12\67\146\0\125\67\1\0"+
    "\107\67\1\0\2\67\2\0\1\67\2\0\2\67\2\0"+
    "\4\67\1\0\14\67\1\0\1\67\1\0\7\67\1\0"+
    "\101\67\1\0\4\67\2\0\10\67\1\0\7\67\1\0"+
    "\34\67\1\0\4\67\1\0\5\67\1\0\1\67\3\0"+
    "\7\67\1\0\u0154\67\2\0\31\67\1\0\31\67\1\0"+
    "\37\67\1\0\31\67\1\0\37\67\1\0\31\67\1\0"+
    "\37\67\1\0\31\67\1\0\37\67\1\0\31\67\1\0"+
    "\10\67\64\0\55\67\12\0\7\67\20\0\1\67\u0171\0"+
    "\54\67\24\0\305\67\73\0\104\67\7\0\1\67\264\0"+
    "\4\67\1\0\33\67\1\0\2\67\1\0\1\67\2\0"+
    "\1\67\1\0\12\67\1\0\4\67\1\0\1\67\1\0"+
    "\1\67\6\0\1\67\4\0\1\67\1\0\1\67\1\0"+
    "\1\67\1\0\3\67\1\0\2\67\1\0\1\67\2\0"+
    "\1\67\1\0\1\67\1\0\1\67\1\0\1\67\1\0"+
    "\1\67\1\0\2\67\1\0\1\67\2\0\4\67\1\0"+
    "\7\67\1\0\4\67\1\0\4\67\1\0\1\67\1\0"+
    "\12\67\1\0\21\67\5\0\3\67\1\0\5\67\1\0"+
    "\21\67\104\0\327\67\51\0\65\67\13\0\336\67\2\0"+
    "\u0182\67\16\0\u0131\67\37\0\36\67\342\0";

  private static int [] zzUnpackcmap_blocks() {
    int [] result = new int[27648];
    int offset = 0;
    offset = zzUnpackcmap_blocks(ZZ_CMAP_BLOCKS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_blocks(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /**
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\3\0\1\1\1\2\1\3\1\1\2\3\1\4\1\5"+
    "\1\6\1\7\1\10\1\11\1\12\1\13\1\14\1\15"+
    "\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25"+
    "\1\26\1\27\1\30\1\31\1\32\1\33\1\4\1\34"+
    "\1\35\11\10\1\36\1\37\1\40\1\41\1\42\1\43"+
    "\1\41\1\44\1\42\1\0\2\3\1\45\1\46\2\0"+
    "\1\24\1\47\1\24\1\50\1\51\1\52\1\53\1\54"+
    "\1\55\2\10\1\56\7\10\1\57\4\0\2\60\1\0"+
    "\2\3\1\61\2\0\1\24\1\62\1\63\2\10\1\64"+
    "\5\10\1\65\2\0\1\66\4\0\1\67\1\70\2\3"+
    "\2\0\1\71\1\10\1\72\1\73\1\74\2\10\5\0"+
    "\1\70\2\3\2\0\1\75\2\10\3\0\1\70\2\3"+
    "\1\76\1\77\1\100\1\10\1\101\1\102\1\103\1\3"+
    "\2\10\1\104";

  private static int [] zzUnpackAction() {
    int [] result = new int[155];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\70\0\160\0\250\0\340\0\u0118\0\u0150\0\u0188"+
    "\0\u01c0\0\250\0\u01f8\0\250\0\250\0\u0230\0\250\0\u0268"+
    "\0\250\0\250\0\250\0\250\0\250\0\250\0\250\0\u02a0"+
    "\0\u02d8\0\u0310\0\250\0\250\0\u0348\0\u0380\0\u03b8\0\u03f0"+
    "\0\250\0\u0150\0\250\0\250\0\u0428\0\u0460\0\u0498\0\u04d0"+
    "\0\u0508\0\u0540\0\u0578\0\u05b0\0\u05e8\0\250\0\u0620\0\250"+
    "\0\u0658\0\250\0\250\0\u0690\0\250\0\u06c8\0\u0150\0\u0700"+
    "\0\u0738\0\u0770\0\250\0\u07a8\0\u07e0\0\u0818\0\u0850\0\u0888"+
    "\0\250\0\u08c0\0\250\0\250\0\250\0\u08f8\0\u0930\0\u0968"+
    "\0\u0230\0\u09a0\0\u09d8\0\u0a10\0\u0a48\0\u0a80\0\u0ab8\0\u0af0"+
    "\0\250\0\u0b28\0\u0b60\0\u0b98\0\u0bd0\0\250\0\u0c08\0\u0c40"+
    "\0\u0c78\0\u0cb0\0\250\0\u0ce8\0\u0d20\0\u0d58\0\250\0\250"+
    "\0\u0d90\0\u0dc8\0\u0230\0\u0e00\0\u0e38\0\u0e70\0\u0ea8\0\u0ee0"+
    "\0\u0230\0\u0f18\0\u0f50\0\250\0\u0f88\0\u0fc0\0\u0ff8\0\u1030"+
    "\0\250\0\u1068\0\u10a0\0\u10d8\0\u1110\0\u1148\0\u0230\0\u1180"+
    "\0\u0230\0\u0230\0\u0230\0\u11b8\0\u11f0\0\u1228\0\u1260\0\u1298"+
    "\0\u12d0\0\u1308\0\u1340\0\u1378\0\u13b0\0\u13e8\0\u1420\0\u0230"+
    "\0\u1458\0\u1490\0\u14c8\0\u1500\0\u1538\0\u1570\0\u15a8\0\u15e0"+
    "\0\250\0\250\0\u0230\0\u1618\0\250\0\250\0\u0118\0\u1650"+
    "\0\u1688\0\u16c0\0\u0230";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[155];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length() - 1;
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /**
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpacktrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\4\3\5\26\4\4\6\1\4\1\7\2\4\5\6"+
    "\1\10\4\6\1\11\7\6\4\4\1\12\3\5\1\13"+
    "\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23"+
    "\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33"+
    "\1\34\1\35\1\36\1\37\1\40\4\16\1\41\1\42"+
    "\1\43\1\44\1\45\2\16\1\46\1\47\1\16\1\50"+
    "\1\51\1\52\5\16\1\53\1\54\1\55\1\16\1\56"+
    "\1\57\1\60\1\12\2\61\2\62\1\61\1\63\3\61"+
    "\1\64\1\65\24\61\1\66\30\61\71\0\3\5\33\0"+
    "\1\67\62\0\4\6\4\0\22\6\6\0\1\5\117\0"+
    "\4\6\4\0\21\6\1\70\36\0\4\6\4\0\14\6"+
    "\1\71\5\6\33\0\1\72\47\0\1\16\13\0\1\16"+
    "\6\0\4\16\4\0\22\16\15\0\1\73\30\0\1\74"+
    "\12\0\1\75\35\0\1\76\66\0\1\77\66\0\1\76"+
    "\1\0\1\32\7\0\1\100\11\0\1\100\51\0\1\101"+
    "\67\0\1\102\67\0\1\103\61\0\1\104\7\0\1\105"+
    "\45\0\1\16\13\0\1\16\6\0\4\16\4\0\15\16"+
    "\1\106\4\16\13\0\1\16\13\0\1\16\6\0\4\16"+
    "\4\0\7\16\1\107\12\16\13\0\1\16\13\0\1\16"+
    "\6\0\4\16\4\0\1\110\21\16\13\0\1\16\13\0"+
    "\1\16\6\0\4\16\4\0\4\16\1\111\15\16\13\0"+
    "\1\16\13\0\1\16\6\0\4\16\4\0\3\16\1\112"+
    "\16\16\13\0\1\16\13\0\1\16\6\0\4\16\4\0"+
    "\17\16\1\113\2\16\13\0\1\16\13\0\1\16\6\0"+
    "\4\16\4\0\5\16\1\114\6\16\1\115\4\16\1\116"+
    "\13\0\1\16\13\0\1\16\6\0\4\16\4\0\10\16"+
    "\1\117\11\16\13\0\1\16\13\0\1\16\6\0\4\16"+
    "\4\0\1\120\21\16\71\0\1\121\2\0\2\61\2\0"+
    "\1\61\1\0\3\61\2\0\24\61\1\0\30\61\6\0"+
    "\1\122\15\0\1\123\5\0\4\123\4\0\1\124\12\123"+
    "\1\125\6\123\3\0\1\123\2\126\2\0\5\126\1\127"+
    "\47\126\1\130\6\126\32\0\4\6\4\0\2\6\1\131"+
    "\17\6\36\0\4\6\4\0\3\6\1\132\16\6\33\0"+
    "\1\133\114\0\1\134\74\0\1\135\31\0\1\76\7\0"+
    "\1\100\11\0\1\100\22\0\70\77\16\0\1\136\1\0"+
    "\1\136\2\0\1\136\73\0\1\137\47\0\1\16\11\0"+
    "\1\140\1\0\1\16\6\0\4\16\4\0\22\16\13\0"+
    "\1\16\13\0\1\16\6\0\4\16\4\0\15\16\1\141"+
    "\4\16\13\0\1\16\13\0\1\16\6\0\4\16\4\0"+
    "\7\16\1\142\12\16\13\0\1\16\13\0\1\16\6\0"+
    "\4\16\4\0\16\16\1\143\3\16\13\0\1\16\13\0"+
    "\1\16\6\0\4\16\4\0\7\16\1\144\12\16\13\0"+
    "\1\16\13\0\1\16\6\0\4\16\4\0\6\16\1\145"+
    "\13\16\13\0\1\16\13\0\1\16\6\0\4\16\4\0"+
    "\17\16\1\146\2\16\13\0\1\16\13\0\1\16\6\0"+
    "\4\16\4\0\12\16\1\147\7\16\13\0\1\16\13\0"+
    "\1\16\6\0\4\16\4\0\2\16\1\150\17\16\13\0"+
    "\1\16\13\0\1\16\6\0\4\16\4\0\14\16\1\151"+
    "\5\16\27\0\1\152\11\0\1\153\52\0\2\123\1\0"+
    "\2\123\1\154\4\0\4\123\4\0\22\123\3\0\1\123"+
    "\20\0\2\123\1\0\2\123\1\154\4\0\4\123\4\0"+
    "\12\123\1\155\7\123\3\0\1\123\20\0\2\123\1\0"+
    "\2\123\1\154\4\0\4\123\4\0\17\123\1\156\2\123"+
    "\3\0\1\123\6\0\1\157\15\0\1\160\5\0\4\160"+
    "\4\0\22\160\3\0\1\160\23\161\1\162\6\161\2\162"+
    "\6\161\5\162\21\161\32\0\4\6\4\0\14\6\1\163"+
    "\5\6\36\0\4\6\4\0\4\6\1\164\15\6\57\0"+
    "\1\165\67\0\1\166\37\0\1\136\53\0\1\16\13\0"+
    "\1\16\6\0\4\16\4\0\3\16\1\167\16\16\13\0"+
    "\1\16\13\0\1\16\6\0\4\16\4\0\15\16\1\170"+
    "\4\16\13\0\1\16\13\0\1\16\6\0\4\16\4\0"+
    "\7\16\1\171\12\16\13\0\1\16\13\0\1\16\6\0"+
    "\4\16\4\0\15\16\1\172\4\16\13\0\1\16\13\0"+
    "\1\16\6\0\4\16\4\0\3\16\1\173\16\16\13\0"+
    "\1\16\13\0\1\16\6\0\4\16\4\0\3\16\1\174"+
    "\16\16\13\0\1\16\13\0\1\16\6\0\4\16\4\0"+
    "\3\16\1\175\16\16\27\0\1\152\1\0\1\154\65\0"+
    "\1\176\6\0\2\176\6\0\5\176\41\0\2\123\1\0"+
    "\2\123\1\154\4\0\4\123\4\0\11\123\1\177\10\123"+
    "\3\0\1\123\20\0\2\123\1\0\2\123\1\154\4\0"+
    "\4\123\4\0\11\123\1\200\10\123\3\0\1\123\23\0"+
    "\1\201\11\0\1\202\52\0\2\160\1\0\2\160\1\126"+
    "\4\0\4\160\4\0\22\160\3\0\1\160\23\0\1\203"+
    "\6\0\2\203\6\0\5\203\53\0\4\6\4\0\1\204"+
    "\21\6\36\0\4\6\4\0\3\6\1\205\16\6\63\0"+
    "\1\206\70\0\1\207\16\0\1\16\13\0\1\16\6\0"+
    "\4\16\4\0\3\16\1\210\16\16\13\0\1\16\13\0"+
    "\1\16\6\0\4\16\4\0\11\16\1\211\10\16\13\0"+
    "\1\16\13\0\1\16\6\0\4\16\4\0\4\16\1\212"+
    "\15\16\27\0\1\176\1\0\1\154\4\0\2\176\6\0"+
    "\5\176\41\0\2\123\1\0\2\123\1\154\4\0\4\123"+
    "\4\0\15\123\1\213\4\123\3\0\1\123\20\0\2\123"+
    "\1\0\2\123\1\154\4\0\4\123\4\0\16\123\1\214"+
    "\3\123\3\0\1\123\23\0\1\201\1\0\1\126\65\0"+
    "\1\215\6\0\2\215\6\0\5\215\44\0\1\216\6\0"+
    "\2\216\6\0\5\216\53\0\4\6\4\0\16\6\1\217"+
    "\3\6\36\0\4\6\4\0\16\6\1\220\3\6\31\0"+
    "\1\221\67\0\1\222\51\0\1\16\13\0\1\16\6\0"+
    "\4\16\4\0\4\16\1\223\15\16\13\0\1\16\13\0"+
    "\1\16\6\0\4\16\4\0\6\16\1\224\13\16\24\0"+
    "\2\123\1\0\2\123\1\225\4\0\4\123\4\0\22\123"+
    "\3\0\1\123\20\0\2\123\1\0\2\123\1\226\4\0"+
    "\4\123\4\0\22\123\3\0\1\123\23\0\1\215\1\0"+
    "\1\126\4\0\2\215\6\0\5\215\44\0\1\126\6\0"+
    "\2\126\6\0\5\126\53\0\4\6\4\0\3\6\1\227"+
    "\16\6\36\0\4\6\4\0\1\6\1\230\20\6\13\0"+
    "\1\16\13\0\1\16\6\0\4\16\4\0\10\16\1\231"+
    "\11\16\36\0\4\6\4\0\5\6\1\227\14\6\13\0"+
    "\1\16\13\0\1\16\6\0\4\16\4\0\3\16\1\232"+
    "\16\16\13\0\1\16\13\0\1\16\6\0\4\16\4\0"+
    "\2\16\1\233\17\16\4\0";

  private static int [] zzUnpacktrans() {
    int [] result = new int[5880];
    int offset = 0;
    offset = zzUnpacktrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpacktrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String[] ZZ_ERROR_MSG = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state {@code aState}
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\3\0\1\11\5\1\1\11\1\1\2\11\1\1\1\11"+
    "\1\1\7\11\3\1\2\11\4\1\1\11\1\1\2\11"+
    "\11\1\1\11\1\1\1\11\1\1\2\11\1\1\1\11"+
    "\1\1\1\0\3\1\1\11\2\0\3\1\1\11\1\1"+
    "\3\11\13\1\1\11\4\0\1\11\1\1\1\0\2\1"+
    "\1\11\2\0\1\1\2\11\11\1\2\0\1\11\4\0"+
    "\1\11\3\1\2\0\7\1\5\0\3\1\2\0\3\1"+
    "\3\0\3\1\2\11\2\1\2\11\5\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[155];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** Number of newlines encountered up to the start of the matched text. */
  @SuppressWarnings("unused")
  private int yyline;

  /** Number of characters from the last newline up to the start of the matched text. */
  @SuppressWarnings("unused")
  protected int yycolumn;

  /** Number of characters up to the start of the matched text. */
  @SuppressWarnings("unused")
  private long yychar;

  /** Whether the scanner is currently at the beginning of a line. */
  @SuppressWarnings("unused")
  private boolean zzAtBOL = true;

  /** Whether the user-EOF-code has already been executed. */
  @SuppressWarnings("unused")
  private boolean zzEOFDone;

  /* user code: */
  private char quote;

  private String blockName;
  private int blockParamIndex;

  public _Angular2Lexer(Angular2Lexer.Config config) {
    this((java.io.Reader)null);
    if (config instanceof Angular2Lexer.BlockParameter blockParameter) {
      blockName = blockParameter.getName();
      blockParamIndex = blockParameter.getIndex();
    }
  }

  private boolean shouldStartWithParameter() {
    return blockName != null && (blockParamIndex > 0 || !Angular2HtmlBlockUtilsKt.getBLOCKS_WITH_PRIMARY_EXPRESSION().contains(blockName));
  }



  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  _Angular2Lexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** Returns the maximum size of the scanner buffer, which limits the size of tokens. */
  private int zzMaxBufferLen() {
    return Integer.MAX_VALUE;
  }

  /**  Whether the scanner buffer can grow to accommodate a larger token. */
  private boolean zzCanGrow() {
    return true;
  }

  /**
   * Translates raw input code points to DFA table row
   */
  private static int zzCMap(int input) {
    int offset = input & 255;
    return offset == input ? ZZ_CMAP_BLOCKS[offset] : ZZ_CMAP_BLOCKS[ZZ_CMAP_TOP[input >> 8] | offset];
  }

  public final int getTokenStart() {
    return zzStartRead;
  }

  public final int getTokenEnd() {
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end, int initialState) {
    zzBuffer = buffer;
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position <tt>pos</tt> from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException
  {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMap(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
        return null;
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1:
            { yypushback(1); yybegin(YYEXPRESSION);
            }
          // fall through
          case 69: break;
          case 2:
            { return WHITE_SPACE;
            }
          // fall through
          case 70: break;
          case 3:
            { yybegin(YYEXPRESSION); if (shouldStartWithParameter()) return BLOCK_PARAMETER_NAME; else yypushback(yylength());
            }
          // fall through
          case 71: break;
          case 4:
            { return BAD_CHARACTER;
            }
          // fall through
          case 72: break;
          case 5:
            { return EXCL;
            }
          // fall through
          case 73: break;
          case 6:
            { yybegin(YYSTRING); quote = '"'; return STRING_LITERAL_PART;
            }
          // fall through
          case 74: break;
          case 7:
            { return SHARP;
            }
          // fall through
          case 75: break;
          case 8:
            { return IDENTIFIER;
            }
          // fall through
          case 76: break;
          case 9:
            { return PERC;
            }
          // fall through
          case 77: break;
          case 10:
            { return AND;
            }
          // fall through
          case 78: break;
          case 11:
            { yybegin(YYSTRING); quote = '\''; return STRING_LITERAL_PART;
            }
          // fall through
          case 79: break;
          case 12:
            { return LPAR;
            }
          // fall through
          case 80: break;
          case 13:
            { return RPAR;
            }
          // fall through
          case 81: break;
          case 14:
            { return MULT;
            }
          // fall through
          case 82: break;
          case 15:
            { return PLUS;
            }
          // fall through
          case 83: break;
          case 16:
            { return COMMA;
            }
          // fall through
          case 84: break;
          case 17:
            { return MINUS;
            }
          // fall through
          case 85: break;
          case 18:
            { return DOT;
            }
          // fall through
          case 86: break;
          case 19:
            { return DIV;
            }
          // fall through
          case 87: break;
          case 20:
            { return NUMERIC_LITERAL;
            }
          // fall through
          case 88: break;
          case 21:
            { return COLON;
            }
          // fall through
          case 89: break;
          case 22:
            { return SEMICOLON;
            }
          // fall through
          case 90: break;
          case 23:
            { return LT;
            }
          // fall through
          case 91: break;
          case 24:
            { return EQ;
            }
          // fall through
          case 92: break;
          case 25:
            { return GT;
            }
          // fall through
          case 93: break;
          case 26:
            { return QUEST;
            }
          // fall through
          case 94: break;
          case 27:
            { return LBRACKET;
            }
          // fall through
          case 95: break;
          case 28:
            { return RBRACKET;
            }
          // fall through
          case 96: break;
          case 29:
            { return XOR;
            }
          // fall through
          case 97: break;
          case 30:
            { return LBRACE;
            }
          // fall through
          case 98: break;
          case 31:
            { return OR;
            }
          // fall through
          case 99: break;
          case 32:
            { return RBRACE;
            }
          // fall through
          case 100: break;
          case 33:
            { return STRING_LITERAL_PART;
            }
          // fall through
          case 101: break;
          case 34:
            { yypushback(yytext().length()); yybegin(YYEXPRESSION);
            }
          // fall through
          case 102: break;
          case 35:
            { if (quote == '"') yybegin(YYEXPRESSION); return STRING_LITERAL_PART;
            }
          // fall through
          case 103: break;
          case 36:
            { if (quote == '\'') yybegin(YYEXPRESSION); return STRING_LITERAL_PART;
            }
          // fall through
          case 104: break;
          case 37:
            { return NE;
            }
          // fall through
          case 105: break;
          case 38:
            { return ANDAND;
            }
          // fall through
          case 106: break;
          case 39:
            { return C_STYLE_COMMENT;
            }
          // fall through
          case 107: break;
          case 40:
            { return LE;
            }
          // fall through
          case 108: break;
          case 41:
            { return EQEQ;
            }
          // fall through
          case 109: break;
          case 42:
            { return GE;
            }
          // fall through
          case 110: break;
          case 43:
            { return ELVIS;
            }
          // fall through
          case 111: break;
          case 44:
            { return QUEST_QUEST;
            }
          // fall through
          case 112: break;
          case 45:
            { return AS_KEYWORD;
            }
          // fall through
          case 113: break;
          case 46:
            { return IF_KEYWORD;
            }
          // fall through
          case 114: break;
          case 47:
            { return OROR;
            }
          // fall through
          case 115: break;
          case 48:
            { return ESCAPE_SEQUENCE;
            }
          // fall through
          case 116: break;
          case 49:
            { return NEQEQ;
            }
          // fall through
          case 117: break;
          case 50:
            { return EQEQEQ;
            }
          // fall through
          case 118: break;
          case 51:
            // lookahead expression with fixed base length
            zzMarkedPos = Character.offsetByCodePoints
                (zzBufferL, zzStartRead, 2);
            { return IDENTIFIER;
            }
          // fall through
          case 119: break;
          case 52:
            { return LET_KEYWORD;
            }
          // fall through
          case 120: break;
          case 53:
            { return VAR_KEYWORD;
            }
          // fall through
          case 121: break;
          case 54:
            { return XML_CHAR_ENTITY_REF;
            }
          // fall through
          case 122: break;
          case 55:
            { yypushback(1); return INVALID_ESCAPE_SEQUENCE;
            }
          // fall through
          case 123: break;
          case 56:
            { return INVALID_ESCAPE_SEQUENCE;
            }
          // fall through
          case 124: break;
          case 57:
            { return ELSE_KEYWORD;
            }
          // fall through
          case 125: break;
          case 58:
            { return NULL_KEYWORD;
            }
          // fall through
          case 126: break;
          case 59:
            { return THIS_KEYWORD;
            }
          // fall through
          case 127: break;
          case 60:
            { return TRUE_KEYWORD;
            }
          // fall through
          case 128: break;
          case 61:
            { return FALSE_KEYWORD;
            }
          // fall through
          case 129: break;
          case 62:
            { yybegin(YYSTRING); quote = '\''; return XML_CHAR_ENTITY_REF;
            }
          // fall through
          case 130: break;
          case 63:
            { yybegin(YYSTRING); quote = '"'; return XML_CHAR_ENTITY_REF;
            }
          // fall through
          case 131: break;
          case 64:
            { return TYPEOF_KEYWORD;
            }
          // fall through
          case 132: break;
          case 65:
            { if (quote == '\'') yybegin(YYEXPRESSION); return XML_CHAR_ENTITY_REF;
            }
          // fall through
          case 133: break;
          case 66:
            { if (quote == '"') yybegin(YYEXPRESSION); return XML_CHAR_ENTITY_REF;
            }
          // fall through
          case 134: break;
          case 67:
            { if (shouldStartWithParameter()) return BLOCK_PARAMETER_NAME; else { yybegin(YYEXPRESSION); yypushback(yylength());}
            }
          // fall through
          case 135: break;
          case 68:
            { return UNDEFINED_KEYWORD;
            }
          // fall through
          case 136: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
