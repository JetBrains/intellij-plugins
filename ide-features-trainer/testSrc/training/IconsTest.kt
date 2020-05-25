// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training

import com.intellij.testFramework.LightPlatformTestCase
import training.ui.Message

class IconsTest : LightPlatformTestCase() {
  fun testIcons() {
    //Removed icons
    assertNull(Message("AllIcons.General.Gear", Message.MessageType.ICON).toIcon())
    assertNull(Message("AllIcons.Actions.Down", Message.MessageType.ICON).toIcon())
    assertNull(Message("AllIcons.Actions.UP", Message.MessageType.ICON).toIcon())

    //Actual icons
    assertNotNull(Message("AllIcons.General.GearPlain", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.General.Filter", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.General.ArrowDown", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.General.ArrowUp", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.RunConfigurations.ShowPassed", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.RunConfigurations.RerunFailedTests", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.RunConfigurations.SortbyDuration", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.Vcs.History", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.General.InspectionsOK", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.RunConfigurations.TestState.Run", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.Actions.StartDebugger", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.Actions.Resume", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.Actions.Suspend", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.General.Add", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.RunConfigurations.TestState.Run", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.RunConfigurations.TestState.Red2", Message.MessageType.ICON).toIcon())
    assertNotNull(Message("AllIcons.General.RunWithCoverage", Message.MessageType.ICON).toIcon())
  }
}