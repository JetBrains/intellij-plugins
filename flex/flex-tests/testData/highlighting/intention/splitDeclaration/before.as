// "Split into multiple declarations" "true"
class DocumentWindow {
  private static const DEFAULT_INIT_OPTIONS:NativeWindowInitOptions = new NativeWindowInitOptions();

  public function DocumentWindow(initOptions:NativeWindowInitOptions = null) {
      var a: * = 4<caret>, b:* = 6, c = 5, d: int, e;
  }
}
