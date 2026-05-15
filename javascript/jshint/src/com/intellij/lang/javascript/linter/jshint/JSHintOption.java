package com.intellij.lang.javascript.linter.jshint;

import com.google.common.collect.ImmutableMap;
import com.intellij.javascript.nodejs.NodeJsConstants;
import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.lang.javascript.linter.option.Option;
import com.intellij.lang.javascript.linter.option.OptionType;
import com.intellij.lang.javascript.linter.option.OptionTypes;
import com.intellij.openapi.util.NlsContexts.DetailedDescription;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Sergey Simonchik
 */
public enum JSHintOption implements Option {

  // Enforcing Options
  BITWISE(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.bitwise.description.short")),
  CAMELCASE(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.camelcase.description.short")),
  CURLY(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.curly.description.short")),
  ENFORCEALL(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.enforceall.description.short")),
  EQEQEQ(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.eqeqeq.description.short")),
  ES3(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.es3.description.short")),
  ES5(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.es5.description.short")),
  FORIN(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.forin.description.short")),
  FREEZE(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.freeze.description.short")),
  IMMED(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.immed.description.short")),
  NEWCAP(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.newcap.description.short")),
  NOARG(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.noarg.description.short")),
  NOCOMMA(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.nocomma.description.short")),
  NOEMPTY(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.noempty.description.short")),
  NONBSP(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.nonbsp.description.short")),
  NONEW(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.nonew.description.short")),
  PLUSPLUS(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.plusplus.description.short")),
  UNDEF(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.undef.description.short")),
  VARSTMT(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.varstmt.description.short")),
  ESVERSION(OptionTypes.INTEGER, null, JSHintBundle.messagePointer("jshint.option.esversion.description.short")),
  STRICT(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.strict.description.short")),
  TRAILING(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.trailing.description.short")),
  LATEDEF(JSHintUtil.LATEDEF_TYPE, false, JSHintBundle.messagePointer("jshint.option.latedef.description.short")),
  UNUSED(JSHintUtil.UNUSED_TYPE, false, JSHintBundle.messagePointer("jshint.option.unused.description.short")),
  INDENT(OptionTypes.INTEGER, null, JSHintBundle.messagePointer("jshint.option.indent.description.short")),
  QUOTMARK(JSHintUtil.QUOTMARK_TYPE, false, JSHintBundle.messagePointer("jshint.option.quotmark.description.short")),
  MAXPARAMS(OptionTypes.INTEGER, null, JSHintBundle.messagePointer("jshint.option.maxparams.description.short")),
  MAXDEPTH(OptionTypes.INTEGER, null, JSHintBundle.messagePointer("jshint.option.maxdepth.description.short")),
  MAXSTATEMENTS(OptionTypes.INTEGER, null, JSHintBundle.messagePointer("jshint.option.maxstatements.description.short")),
  MAXCOMPLEXITY(OptionTypes.INTEGER, null, JSHintBundle.messagePointer("jshint.option.maxcomplexity.description.short")),
  MAXLEN(OptionTypes.INTEGER, null, JSHintBundle.messagePointer("jshint.option.maxlen.description.short")),

  // Relaxing Options
  ASI(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.asi.description.short")),
  BOSS(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.boss.description.short")),
  DEBUG(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.debug.description.short")),
  ELISION(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.elision.description.short")),
  EQNULL(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.eqnull.description.short")),
  ESNEXT(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.esnext.description.short")),
  EVIL(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.evil.description.short")),
  EXPR(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.expr.description.short")),
  FUNCSCOPE(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.funcscope.description.short")),
  FUTUREHOSTILE(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.futurehostile.description.short")),
  GCL(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.gcl.description.short")),
  GLOBALSTRICT(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.globalstrict.description.short")),
  ITERATOR(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.iterator.description.short")),
  LASTSEMIC(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.lastsemic.description.short")),
  LAXBREAK(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.laxbreak.description.short")),
  LAXCOMMA(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.laxcomma.description.short")),
  LOOPFUNC(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.loopfunc.description.short")),
  MOZ(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.moz.description.short")),
  MULTISTR(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.multistr.description.short")),
  NOTYPEOF(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.notypeof.description.short")),
  PROTO(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.proto.description.short")),
  SCRIPTURL(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.scripturl.description.short")),
  SMARTTABS(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.smarttabs.description.short")),
  SHADOW(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.shadow.description.short")),
  SINGLEGROUPS("singleGroups", OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.singlegroups.description.short")),
  SUB(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.sub.description.short")),
  SUPERNEW(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.supernew.description.short")),
  VALIDTHIS(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.validthis.description.short")),
  WITHSTMT(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.withstmt.description.short")),
  NOYIELD(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.noyield.description.short")),

  // Assume
  BROWSER(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.browser.description.short")),
  BROWSERIFY(OptionTypes.BOOLEAN, false, "Browserify"), //NON-NLS
  COUCH(OptionTypes.BOOLEAN, false, "CouchDB"),
  DEVEL(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.devel.description.short")),
  DOJO(OptionTypes.BOOLEAN, false, "Dojo Toolkit"), //NON-NLS
  JASMINE(OptionTypes.BOOLEAN, false, "Jasmine"), //NON-NLS
  JQUERY(OptionTypes.BOOLEAN, false, "jQuery"),
  MOCHA(OptionTypes.BOOLEAN, false, "Mocha"), //NON-NLS
  MODULE(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.module.description.short")),
  MOOTOOLS(OptionTypes.BOOLEAN, false, "MooTools"),
  NODE(OptionTypes.BOOLEAN, false, NodeJsConstants.NODE_JS),
  NONSTANDARD(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.nonstandard.description.short")),
  PHANTOM(OptionTypes.BOOLEAN, false, "PhantomJS"),
  PROTOTYPEJS(OptionTypes.BOOLEAN, false, "Prototype"), //NON-NLS
  QUNIT(OptionTypes.BOOLEAN, false, "QUnit"), //NON-NLS
  RHINO(OptionTypes.BOOLEAN, false, "Rhino"), //NON-NLS
  SHELLJS(OptionTypes.BOOLEAN, false, "ShellJS"),
  TYPED(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.typed.description.short")),
  WORKER(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.worker.description.short")),
  WSH(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.wsh.description.short")),
  YUI(OptionTypes.BOOLEAN, false, "YUI"), //NON-NLS

  // Legacy
  NOMEN(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.nomen.description.short")),
  ONEVAR(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.onevar.description.short")),
  PASSFAIL(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.passfail.description.short")),
  WHITE(OptionTypes.BOOLEAN, false, JSHintBundle.messagePointer("jshint.option.white.description.short")),


  MAXERR(OptionTypes.INTEGER, 50, JSHintBundle.messagePointer("jshint.option.maxerr.description.short")),
  PREDEF(OptionTypes.STRING, null, JSHintBundle.messagePointer("jshint.option.predef.description.short"), "globals");

  private static final ImmutableMap<String, JSHintOption> OPTION_BY_KEY_MAP;

  static {
    ImmutableMap.Builder<String, JSHintOption> builder = ImmutableMap.builder();
    for (JSHintOption option : values()) {
      builder.put(option.getKey(), option);
      String keyAlias = option.getKeyAlias();
      if (keyAlias != null) {
        builder.put(keyAlias, option);
      }
    }
    OPTION_BY_KEY_MAP = builder.build();
  }

  private final OptionType<Object> myType;
  private final Object myDefaultValue;
  private final String myKey;
  private final Supplier<@DetailedDescription String> myRawShortDescription;
  private final String myKeyAlias;

  JSHintOption(@NotNull OptionType<?> type, @Nullable Object defaultValue, @DetailedDescription String rawShortDescription) {
    this(type, defaultValue, () -> rawShortDescription, null);
  }

  JSHintOption(@NotNull OptionType<?> type, @Nullable Object defaultValue, @NotNull Supplier<@DetailedDescription String> rawShortDescription) {
    this(type, defaultValue, rawShortDescription, null);
  }

  JSHintOption(@NotNull String key, @NotNull OptionType<?> type, @Nullable Object defaultValue, @NotNull Supplier<@DetailedDescription String> rawShortDescription) {
    this(key, type, defaultValue, rawShortDescription, null);
  }

  JSHintOption(@NotNull OptionType<?> type, @Nullable Object defaultValue, @NotNull Supplier<@DetailedDescription String> rawShortDescription, @Nullable String keyAlias) {
    this(null, type, defaultValue, rawShortDescription, keyAlias);
  }

  JSHintOption(@Nullable String key, @NotNull OptionType<?> type, @Nullable Object defaultValue, @NotNull Supplier<@DetailedDescription String> rawShortDescription, @Nullable String keyAlias) {
    //noinspection unchecked
    myType = (OptionType<Object>) type;
    myDefaultValue = defaultValue;
    myKey = key != null ? key : StringUtil.toLowerCase(name());
    myKeyAlias = keyAlias;

    myRawShortDescription = rawShortDescription;

    if (defaultValue != null && !myType.isValidValue(defaultValue)) {
      throw new RuntimeException("Illegal default value '" + defaultValue + "' for key '" + myKey + "'");
    }
  }

  @Override
  public @NotNull @NlsSafe String getKey() {
    return myKey;
  }

  @Override
  public @NotNull OptionType<Object> getType() {
    return myType;
  }

  public @Nullable Object fromString(@Nullable String valueStr) {
    Object parsed = null;
    if (valueStr != null) {
      parsed = myType.fromString(valueStr);
    }
    return parsed != null ? parsed : myDefaultValue;
  }

  public @NotNull @DetailedDescription String getShortDescription() {
    return join(getShortDescriptionFragments());
  }

  public @Nullable String getKeyAlias() {
    return myKeyAlias;
  }

  public @NotNull List<Fragment> getShortDescriptionFragments() {
    return parseTextFragments(myRawShortDescription.get());
  }

  public static @Nullable JSHintOption findByName(@NotNull String optionName) {
    return OPTION_BY_KEY_MAP.get(optionName);
  }

  @Override
  public String toString() {
    return name() + ", type: " + getType();
  }

  public @Nullable Object getDefaultValue() {
    return myDefaultValue;
  }

  private static @NotNull List<Fragment> parseTextFragments(@DetailedDescription @NotNull String text) {
    List<Fragment> fragments = new ArrayList<>();
    int unprocessedIndex = 0;
    String startTag = "<code>", endTag = "</code>";
    while (unprocessedIndex < text.length()) {
      int startIndex = text.indexOf(startTag, unprocessedIndex);
      boolean hasCodeFragment = false;
      if (startIndex >= 0) {
        int endIndex = text.indexOf(endTag, startIndex);
        if (endIndex >= 0) {
          fragments.add(new Fragment(text.substring(unprocessedIndex, startIndex), false));
          fragments.add(new Fragment(text.substring(startIndex + startTag.length(), endIndex), true));
          unprocessedIndex = endIndex + endTag.length();
          hasCodeFragment = true;
        }
      }
      if (!hasCodeFragment) {
        fragments.add(new Fragment(text.substring(unprocessedIndex), false));
        unprocessedIndex = text.length();
      }
    }
    return fragments;
  }

  private static @Nls String join(@NotNull List<Fragment> fragments) {
    @Nls StringBuilder out = new StringBuilder();
    for (Fragment fragment : fragments) {
      out.append(fragment.getText());
    }
    return out.toString();
  }

  public static final class Fragment {
    private final @Nls String myText;
    private final boolean myCode;

    private Fragment(@Nls @NotNull String text, boolean code) {
      myText = text;
      myCode = code;
    }

    public @NotNull @Nls String getText() {
      return myText;
    }

    public boolean isCode() {
      return myCode;
    }
  }

}
