package com.jetbrains.plugins.jade.lexer;

import com.intellij.embedding.EmbeddedLazyParseableElementType;
import com.intellij.html.embedding.HtmlEmbeddedContentSupport;
import com.intellij.html.embedding.HtmlEmbedmentInfo;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lexer.DummyLexer;
import com.intellij.lexer.EmbeddedTokenTypesProvider;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPointListener;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighterLanguageFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.css.impl.util.CssStylesheetLazyElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import com.jetbrains.plugins.jade.highlighter.JadeSyntaxHighlighter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.intellij.psi.xml.XmlElementType.HTML_EMBEDDED_CONTENT;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.STYLE_BLOCK;

public final class JadeEmbeddingUtil {
  private static final Logger LOG = Logger.getInstance(JadeEmbeddingUtil.class);

  private static final IElementType XML_DATA_CHARACTERS_LAZY_TYPE =
    new DummyEmbeddedType("XML_DATA_CHARACTERS", XmlTokenType.XML_DATA_CHARACTERS);

  public static @NotNull IElementType getEmbeddedTokenWrapperType(@NotNull IElementType embeddedTokenType) {
    return ApplicationManager.getApplication().getService(WrappersForEmbeddedTypesService.class)
      .getEmbeddedTokenWrapperType(embeddedTokenType);
  }

  public static @NotNull SyntaxHighlighter getHighlighterForLanguage(@NotNull Language language) {
    return SyntaxHighlighterLanguageFactory.EP_NAME.computeIfAbsent(language, JadeEmbeddingUtil.class, it -> {
      return SyntaxHighlighterFactory.getSyntaxHighlighter(it, null, null);
    });
  }

  private static IElementType createWrapper(IElementType token) {
    if (token instanceof CssStylesheetLazyElementType) {
      return new JadeEmbeddedTokenTypesWrapperForCssStylesheet(((CssStylesheetLazyElementType)token));
    }
    else {
      return new JadeEmbeddedTokenTypesWrapper(token);
    }
  }

  public static @Nullable IElementType getElementToEmbedForATag(@Nullable String tagName, @Nullable Map<String, String> attributes) {
    if (!StringUtil.equalsIgnoreCase(tagName, "script")) {
      return null;
    }

    if (attributes == null || !attributes.containsKey("type")) {
      return null;
    }

    final String mimeType = sanitizeAttrValue(attributes.get("type"));
    if (StringUtil.isEmpty(mimeType)) {
      return null;
    }

    /*
      copy-pasted from {@link com.intellij.lexer.BaseHtmlLexer}
     */
    Collection<Language> instancesByMimeType = Language.findInstancesByMimeType(mimeType.trim());
    if (instancesByMimeType.isEmpty() && mimeType.contains("template")) {
      instancesByMimeType = Collections.singletonList(HTMLLanguage.INSTANCE);
    }
    IElementType result = null;
    for (Language language : instancesByMimeType) {
      HtmlEmbedmentInfo embedmentInfo = HtmlEmbeddedContentSupport.getScriptTagEmbedmentInfo(language);
      if (embedmentInfo != null) {
        if (result != null) {
          Logger.getInstance(JadeEmbeddingUtil.class).warn("Multiple script content providers for a type: " + mimeType);
        }

        result = embedmentInfo.getElementType();
      }
    }

    // Fallback if nothing helps. Better than JS, though.
    if (result == null) {
      result = XML_DATA_CHARACTERS_LAZY_TYPE;
    }

    return result;
  }

  public static @Nullable IElementType getElementToEmbedForFilterName(String filterName) {
    for (EmbeddedTokenTypesProvider embeddedTokenTypesProvider : EmbeddedTokenTypesProvider.getProviders()) {
      if (filterName.startsWith(embeddedTokenTypesProvider.getName())) {
        return embeddedTokenTypesProvider.getElementType();
      }
    }

    return null;
  }

  @Contract("!null -> !null")
  private static @Nullable String sanitizeAttrValue(@Nullable String value) {
    if (value == null) {
      return null;
    }

    value = value.trim();
    if (value.length() < 2) {
      return value;
    }

    if (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'")) {
      value = value.substring(1, value.length() - 1);
    }

    return value;
  }

  @Service
  private static final class WrappersForEmbeddedTypesService implements Disposable {

    private final Map<IElementType, IElementType> myWrappersForEmbeddedTypes = new ConcurrentHashMap<>();

    private WrappersForEmbeddedTypesService() {
      getOrCreateEmbeddedTokenWrapperType(XML_DATA_CHARACTERS_LAZY_TYPE);
      getOrCreateEmbeddedTokenWrapperType(HTML_EMBEDDED_CONTENT);
      getOrCreateEmbeddedTokenWrapperType(STYLE_BLOCK);
      EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME.getPoint().addExtensionPointListener(
        new ExtensionPointListener<>() {
          @Override
          public void extensionAdded(@NotNull EmbeddedTokenTypesProvider provider, @NotNull PluginDescriptor pluginDescriptor) {
            JadeSyntaxHighlighter.registerEmbeddedToken(getOrCreateEmbeddedTokenWrapperType(provider.getElementType()));
          }

          @Override
          public void extensionRemoved(@NotNull EmbeddedTokenTypesProvider provider, @NotNull PluginDescriptor pluginDescriptor) {
            final IElementType wrapper = myWrappersForEmbeddedTypes.remove(provider.getElementType());
            if (wrapper != null) {
              JadeSyntaxHighlighter.unregisterEmbeddedToken(wrapper);
            }
          }
        }, true, this
      );
    }


    public @NotNull IElementType getEmbeddedTokenWrapperType(@NotNull IElementType embeddedTokenType) {
      IElementType wrapper = myWrappersForEmbeddedTypes.get(embeddedTokenType);
      if (wrapper != null) {
        return wrapper;
      }
      LOG.error("Embedded token type " + embeddedTokenType + " has not been registered through extension point.");
      return getOrCreateEmbeddedTokenWrapperType(embeddedTokenType);
    }

    private @NotNull IElementType getOrCreateEmbeddedTokenWrapperType(@NotNull IElementType embeddedTokenType) {
      return myWrappersForEmbeddedTypes.computeIfAbsent(embeddedTokenType, JadeEmbeddingUtil::createWrapper);
    }

    @Override
    public void dispose() {
    }
  }

  private static final class DummyEmbeddedType extends EmbeddedLazyParseableElementType {
    private final @NotNull IElementType myTokenType;

    private DummyEmbeddedType(@NotNull @NonNls String debugName, @NotNull IElementType containingTokenType) {
      super(debugName, containingTokenType.getLanguage());
      myTokenType = containingTokenType;
    }

    @Override
    public Lexer createLexer(@NotNull ASTNode chameleon, @NotNull Project project) {
      return new DummyLexer(myTokenType);
    }

    @Override
    public ASTNode parseAndGetTree(@NotNull PsiBuilder builder) {
      final PsiBuilder.Marker marker = builder.mark();
      while (!builder.eof()) {
        builder.advanceLexer();
      }
      marker.done(this);

      return builder.getTreeBuilt();
    }
  }
}
