// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.codeInsight

import com.intellij.flex.FlexTestOption
import com.intellij.flex.FlexTestOptions
import com.intellij.flex.util.FlexTestUtils
import com.intellij.lang.actionscript.arrangement.ActionScriptRearranger
import com.intellij.lang.javascript.ActionScriptFileType
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.codeStyle.arrangement.AbstractRearrangerTest
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens
import org.jetbrains.annotations.NonNls

import java.lang.reflect.Field
import java.lang.reflect.Modifier

class ActionScriptRearrangerTest extends AbstractRearrangerTest {

  ActionScriptRearrangerTest() {
    fileType = ActionScriptFileType.INSTANCE
    language = JavaScriptSupportLoader.ECMA_SCRIPT_L4
  }

  protected void setUp() {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "")
    super.setUp()

    def sdk = FlexTestUtils.getSdk(new JSTestUtils.TestDescriptor(this), myFixture.getProjectDisposable())

    ApplicationManager.application.runWriteAction(new Runnable() {
      void run() {
        def model = ModuleRootManager.getInstance(module).getModifiableModel()
        model.setSdk(sdk)
        model.commit()
      }
    })
  }

  @Override
  protected void tearDown() {
    super.tearDown()
    clearDeclaredFields(ActionScriptRearrangerTest.class)
  }

  static void clearDeclaredFields(Class aClass) throws IllegalAccessException {
    for (final Field field : aClass.getDeclaredFields()) {
      @NonNls final String name = field.getDeclaringClass().getName()
      if (!name.startsWith("junit.framework.") && !name.startsWith("com.intellij.testFramework.")) {
        final int modifiers = field.getModifiers()
        if ((modifiers & Modifier.FINAL) == 0 && (modifiers & Modifier.STATIC) != 0 && !field.getType().isPrimitive()) {
          field.setAccessible(true)
          field.set(null, null)
        }
      }
    }
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  void testComplex() {

    commonSettings.BLANK_LINES_AROUND_METHOD = 0
    commonSettings.BLANK_LINES_AROUND_CLASS = 0

    doTest(
      initial: '''\
package {
import flash.events.Event;
import flash.events.MouseEvent;

public class Test {
    protected final override function protectedFinalOverrideFunction2():void {}
    public override function set publicOverrideSetter(i:int):void{}
    internal static var internalStaticVar2;
    public static function publicStaticFunction1():void {}
    public override function overriddenPublicEventHandler(e:Event):void {}
    protected final function protectedFinalFunction2():void {}
    public override function publicOverrideFunction1():void {}
    public function set publicSetterNoGetter(i:int):void{}
    internal static var internalStaticVar1;
    private final function privateFinalFunction2():void {}
    protected function protectedFunction1():void {}
    private static var privateStaticVar1;
    protected final override function protectedFinalOverrideFunction1():void {}
    final function packageLocalFinalFunction1():void {}
    public static function get publicStaticProperty():int {return 0}
    public static function publicStaticFunction2():void {}
    protected var
    protectedVar1;
    public function get publicGetterNoSetter():int {return 0}
    function packageLocalEventHandler(e:MouseEvent):void {}
    public function publicFunction1():void {}
    public static var publicStaticVar2;
    static function packageLocalStaticFunction2():void {}
    protected override function overriddenProtectedEventHandler(e:MouseEvent):void {}
    function packageLocalFunction2():void {}
    protected static
    function protectedStaticFunction1():void {}
    public final function publicFinalFunction2():void {}
    protected override function protectedOverrideFunction1():void {}
    private function privateEventHandler(e:Event):void {}
    override function packageLocalOverrideFunction2():void {}
    static function packageLocalStaticFunction1():void {}
    internal override function internalOverrideFunction2():void {}
    const const2;
    public static function get publicStaticGetterNoSetter():int {return 0}
    protected function protectedFunction2():void {}
    internal static function internalStaticFunction1():void {}
    public final override function publicFinalOverrideFunction2():void {}
    [Foo]
    public var publicVar1;
    internal function internalFunction2():void {}
    final override function packageLocalFinalOverrideFunction1():void {}
    function packageLocalFunction1():void {}
    final override function packageLocalFinalOverrideFunction2():void {}
    internal final override function internalFinalOverrideFunction1():void {}
    public var publicVar2;
    public override function publicOverrideFunction2():void {}
    public function Test() {}
    public override function get publicOverrideGetterNoSetter():int {return 0}
    final function packageLocalFinalFunction2():void {}
    internal final function internalFinalFunction1():void {}
    public static function set publicStaticProperty(i:int):void{}
    internal var internalVar2;
    public static function set publicStaticSetterNoGetter(i:int):void{}
    { /*static init 1*/ }
    protected override function protectedOverrideFunction2():void {}
    override function packageLocalOverrideFunction1():void {}
    protected final function protectedFinalFunction1():void {}
    public static const const1;
    { /*static init 2*/ }
    public function publicFunction2():void {}
    internal var internalVar1;
    private static function privateStaticFunction1():void {}
    public function set publicProperty(i:int):void{}
    public static var publicStaticVar1;
    private static function privateStaticFunction2():void {}
    internal final override function internalFinalOverrideFunction2():void {}
    protected var protectedVar2;
    var packageLocalVar1;
    private function privateFunction1():void {}
    internal override function internalOverrideFunction1():void {}
    public final function publicFinalFunction1():void {}
    public function get publicProperty():int {return 0}
    internal final function internalFinalFunction2():void {}
    public override function set publicOverrideSetterNoGetter(i:int):void{}
    var packageLocalVar2;
    private final function privateFinalFunction1():void {}
    private var privateVar2;
    public override function get publicOverrideGetter():int {return 0}
    protected static var protectedStaticVar1;
    public final override function publicFinalOverrideFunction1():void {}
    static var packageLocalStaticVar1;
    [Foo]
    internal function internalFunction1():void {}
    static var packageLocalStaticVar2;
    internal static function internalStaticFunction2():void {}
    private var privateVar1;
    protected static function protectedStaticFunction2():void {}
    private function privateFunction2():void {}
    private static var privateStaticVar2;
    protected static var protectedStaticVar2;
    private var _publicProperty;
}
}
''',
      expected: '''\
package {
import flash.events.Event;
import flash.events.MouseEvent;

public class Test {
    { /*static init 1*/ }
    { /*static init 2*/ }
    const const2;
    public static const const1;
    public static var publicStaticVar2;
    public static var publicStaticVar1;
    protected static var protectedStaticVar1;
    protected static var protectedStaticVar2;
    internal static var internalStaticVar2;
    internal static var internalStaticVar1;
    static var packageLocalStaticVar1;
    static var packageLocalStaticVar2;
    private static var privateStaticVar1;
    private static var privateStaticVar2;
    public static function get publicStaticProperty():int {return 0}
    public static function set publicStaticProperty(i:int):void{}
    public static function get publicStaticGetterNoSetter():int {return 0}
    public static function set publicStaticSetterNoGetter(i:int):void{}
    public static function publicStaticFunction1():void {}
    public static function publicStaticFunction2():void {}
    protected static
    function protectedStaticFunction1():void {}
    protected static function protectedStaticFunction2():void {}
    static function packageLocalStaticFunction2():void {}
    static function packageLocalStaticFunction1():void {}
    internal static function internalStaticFunction1():void {}
    internal static function internalStaticFunction2():void {}
    private static function privateStaticFunction1():void {}
    private static function privateStaticFunction2():void {}
    public function Test() {}
    [Foo]
    public var publicVar1;
    public var publicVar2;
    protected var
    protectedVar1;
    protected var protectedVar2;
    internal var internalVar2;
    internal var internalVar1;
    var packageLocalVar1;
    var packageLocalVar2;
    private var privateVar2;
    private var privateVar1;
    private var _publicProperty;
    public override function set publicOverrideSetter(i:int):void{}
    public override function get publicOverrideGetterNoSetter():int {return 0}
    public override function set publicOverrideSetterNoGetter(i:int):void{}
    public override function get publicOverrideGetter():int {return 0}
    public function set publicSetterNoGetter(i:int):void{}
    public function get publicGetterNoSetter():int {return 0}
    public function get publicProperty():int {return 0}
    public function set publicProperty(i:int):void{}
    protected final override function protectedFinalOverrideFunction2():void {}
    public override function publicOverrideFunction1():void {}
    protected final override function protectedFinalOverrideFunction1():void {}
    protected override function protectedOverrideFunction1():void {}
    override function packageLocalOverrideFunction2():void {}
    internal override function internalOverrideFunction2():void {}
    public final override function publicFinalOverrideFunction2():void {}
    final override function packageLocalFinalOverrideFunction1():void {}
    final override function packageLocalFinalOverrideFunction2():void {}
    internal final override function internalFinalOverrideFunction1():void {}
    public override function publicOverrideFunction2():void {}
    protected override function protectedOverrideFunction2():void {}
    override function packageLocalOverrideFunction1():void {}
    internal final override function internalFinalOverrideFunction2():void {}
    internal override function internalOverrideFunction1():void {}
    public final override function publicFinalOverrideFunction1():void {}
    public function publicFunction1():void {}
    public final function publicFinalFunction2():void {}
    public function publicFunction2():void {}
    public final function publicFinalFunction1():void {}
    protected final function protectedFinalFunction2():void {}
    protected function protectedFunction1():void {}
    protected function protectedFunction2():void {}
    protected final function protectedFinalFunction1():void {}
    final function packageLocalFinalFunction1():void {}
    function packageLocalFunction2():void {}
    internal function internalFunction2():void {}
    function packageLocalFunction1():void {}
    final function packageLocalFinalFunction2():void {}
    internal final function internalFinalFunction1():void {}
    internal final function internalFinalFunction2():void {}
    [Foo]
    internal function internalFunction1():void {}
    private final function privateFinalFunction2():void {}
    private function privateFunction1():void {}
    private final function privateFinalFunction1():void {}
    private function privateFunction2():void {}
    public override function overriddenPublicEventHandler(e:Event):void {}
    protected override function overriddenProtectedEventHandler(e:MouseEvent):void {}
    function packageLocalEventHandler(e:MouseEvent):void {}
    private function privateEventHandler(e:Event):void {}
}
}
''',
      rules: ActionScriptRearranger.getDefaultMatchRules()
    )
  }

  @FlexTestOptions(FlexTestOption.WithGumboSdk)
  void testGroupPropertyFieldWithGetterSetter() {

    commonSettings.BLANK_LINES_AROUND_METHOD = 0
    commonSettings.BLANK_LINES_AROUND_CLASS = 0

    doTest(
      initial: '''\
package {
public class Test {
    public function set property(i:int):void{}
    public function get property():int {return 0}
    public function get getterNoSetter():int {return 0}
    public function set setterNoGetter(i:int):void{}
    private var _property;
    private var _notAProperty;
    private var _getterNoSetter;
    private var _setterNoGetter;
}
}
''',
      expected: '''\
package {
public class Test {
    private var _notAProperty;
    private var _property;
    public function get property():int {return 0}
    public function set property(i:int):void{}
    private var _getterNoSetter;
    public function get getterNoSetter():int {return 0}
    private var _setterNoGetter;
    public function set setterNoGetter(i:int):void{}
}
}
''',
      groups: [group(StdArrangementTokens.Grouping.GROUP_PROPERTY_FIELD_WITH_GETTER_SETTER)],
      rules: ActionScriptRearranger.getDefaultMatchRules()
    )
  }
}
