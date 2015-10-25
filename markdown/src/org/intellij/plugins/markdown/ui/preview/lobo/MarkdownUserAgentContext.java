package org.intellij.plugins.markdown.ui.preview.lobo;

import org.lobobrowser.context.NetworkRequestImpl;
import org.lobobrowser.html.HttpRequest;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.primary.clientlets.html.HttpRequestImpl;

import java.net.URL;
import java.security.Policy;

class MarkdownUserAgentContext implements UserAgentContext {
  @Override
  public HttpRequest createHttpRequest() {
    return new HttpRequestImpl(new NetworkRequestImpl());
  }

  @Override
  public String getAppCodeName() {
    return "";
  }

  @Override
  public String getAppName() {
    return "";
  }

  @Override
  public String getAppVersion() {
    return "";
  }

  @Override
  public String getAppMinorVersion() {
    return "";
  }

  @Override
  public String getBrowserLanguage() {
    return "";
  }

  @Override
  public boolean isCookieEnabled() {
    return false;
  }

  @Override
  public boolean isScriptingEnabled() {
    return false;
  }

  @Override
  public boolean isExternalCSSEnabled() {
    return true;
  }

  @Override
  public boolean isInternalCSSEnabled() {
    return true;
  }

  @Override
  public String getPlatform() {
    return "";
  }

  @Override
  public String getUserAgent() {
    return "";
  }

  @Override
  public String getCookie(URL url) {
    return null;
  }

  @Override
  public void setCookie(URL url, String s) {

  }

  @Override
  public Policy getSecurityPolicy() {
    return null;
  }

  @Override
  public int getScriptingOptimizationLevel() {
    return 0;
  }

  @Override
  public boolean isMedia(String s) {
    return false;
  }

  @Override
  public String getVendor() {
    return "";
  }

  @Override
  public String getProduct() {
    return "";
  }
}
