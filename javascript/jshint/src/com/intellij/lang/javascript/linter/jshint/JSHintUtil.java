package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.lang.javascript.linter.option.OptionEnumType;
import com.intellij.lang.javascript.linter.option.OptionEnumVariant;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * @author Sergey Simonchik
 */
public final class JSHintUtil {

  private JSHintUtil() {}

  public static final OptionEnumType QUOTMARK_TYPE = new OptionEnumType(Arrays.asList(
    new OptionEnumVariant(false, JSHintBundle.messagePointer("jshint.quotmark.false.text")),
    new OptionEnumVariant(true, JSHintBundle.messagePointer("jshint.quotmark.true.text")),
    new OptionEnumVariant("single", JSHintBundle.messagePointer("jshint.quotmark.single.text")),
    new OptionEnumVariant("double", JSHintBundle.messagePointer("jshint.quotmark.double.text"))
  ));

  public static final OptionEnumType UNUSED_TYPE = new OptionEnumType(Arrays.asList(
    new OptionEnumVariant(false, JSHintBundle.messagePointer("jshint.unused.false.text")),
    new OptionEnumVariant(true, JSHintBundle.messagePointer("jshint.unused.true.text")),
    new OptionEnumVariant("vars", JSHintBundle.messagePointer("jshint.unused.vars.text")),
    new OptionEnumVariant("strict", JSHintBundle.messagePointer("jshint.unused.strict.text"))
  ));

  public static final OptionEnumType LATEDEF_TYPE = new OptionEnumType(Arrays.asList(
    new OptionEnumVariant(false, JSHintBundle.messagePointer("jshint.latedef.false.text")),
    new OptionEnumVariant(true, JSHintBundle.messagePointer("jshint.latedef.true.text")),
    new OptionEnumVariant("nofunc", JSHintBundle.messagePointer("jshint.latedef.nofunc.text"))
  ));

  public static @Nullable <T> T cast(@Nullable Object obj, @NotNull Class<T> clazz) {
    if (obj == null) {
      return null;
    }
    if (clazz.isInstance(obj)) {
      return clazz.cast(obj);
    }
    throw new ClassCastException("Expected type: " + clazz.getName()
                                 + ", actual: " + obj.getClass().getName());
  }

  public static @Nullable Integer castToInteger(@Nullable Object obj) {
    return cast(obj, Integer.class);
  }

  public static @Nullable String castToString(@Nullable Object obj) {
    return cast(obj, String.class);
  }

  /**
   * Creates readonly {@code JEditorPane} instance that scrolls immediately
   * on Up/Down/Left/Right arrows pressed, like a browser.
   *
   * @param type mime type of the given text
   * @param text the text to initialize with
   * @return {@code JEditorPane} instance
   */
  public static JEditorPane createReadonlyBrowser(@NotNull String type, @NotNull @Nls String text) {
    final JEditorPane browser = new JEditorPane(type, text);
    browser.setEditable(false);
    UIUtil.doNotScrollToCaret(browser);
    browser.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_DOWN) {
          handleArrow(browser, true, true);
        }
        else if (keyCode == KeyEvent.VK_UP) {
          handleArrow(browser, true, false);
        }
        else if (keyCode == KeyEvent.VK_LEFT) {
          handleArrow(browser, false, false);
        }
        else if (keyCode == KeyEvent.VK_RIGHT) {
          handleArrow(browser, false, true);
        }
      }
    });
    return browser;
  }

  private static void handleArrow(@NotNull JEditorPane browser, boolean vertical, boolean downRight) {
    Rectangle visibleRect = browser.getVisibleRect();
    int direction = downRight ? 1 : -1;
    int unit = browser.getScrollableUnitIncrement(
      visibleRect, vertical ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL, direction
    );
    Rectangle newVisibleRect = new Rectangle(visibleRect);
    if (vertical) {
      newVisibleRect.y += unit * direction;
    }
    else {
      newVisibleRect.x += unit * direction;
    }
    browser.scrollRectToVisible(newVisibleRect);
  }

  public static @NotNull JComponent createIOExceptionBalloonComponent(@NotNull String text,
                                                                      final @NotNull Runnable retryCallback,
                                                                      boolean addConfigureHttpProxyLink) {
    JTextPane messageComponent = new JTextPane();
    messageComponent.setFont(StartupUiUtil.getLabelFont());
    final HTMLEditorKit editorKit = new HTMLEditorKit();
    editorKit.getStyleSheet().addRule(UIUtil.displayPropertiesToCSS(StartupUiUtil.getLabelFont(), UIUtil.getLabelForeground()));
    messageComponent.setEditorKit(editorKit);
    messageComponent.setContentType(UIUtil.HTML_MIME);
    final String retry = "retry";
    final String proxy = "proxy";
    messageComponent.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      public void hyperlinkActivated(@NotNull HyperlinkEvent e) {
        if (retry.equals(e.getDescription())) {
          retryCallback.run();
        }
        else if (proxy.equals(e.getDescription())) {
          HttpConfigurable.editConfigurable(null);
        }
      }
    });
    @Nls StringBuilder html = new StringBuilder("<html><body>").append(text).append("<br/>");
    if (addConfigureHttpProxyLink) {
      html.append(JSHintBundle.message("jshint.exception.balloon.action.configure.proxy.or.retry", proxy, retry));
    } else {
      html.append(JSHintBundle.message("jshint.exception.balloon.action.retry", retry));
    }
    html.append("</body></html>");
    messageComponent.setText(html.toString());
    messageComponent.setEditable(false);
    if (messageComponent.getCaret() != null) {
      messageComponent.setCaretPosition(0);
    }

    messageComponent.setOpaque(false);
    messageComponent.setBackground(UIUtil.TRANSPARENT_COLOR);

    messageComponent.setForeground(UIUtil.getLabelForeground());
    return messageComponent;
  }

}
