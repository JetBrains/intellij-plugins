package com.intellij.plugins.serialmonitor

import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.SerialProfileService.*
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XMap
import java.nio.charset.StandardCharsets

@Tag("serial-profile")
data class SerialPortProfile(

  @NlsSafe
  @Attribute
  var portName: String = "",

  @Attribute
  var baudRate: Int = 0,

  @Attribute
  var bits: Int = 8,

  @Attribute
  var stopBits: StopBits = StopBits.BITS_1,

  @Attribute
  var parity: Parity = Parity.NONE,

  @Attribute("new-line")
  var newLine: NewLine = NewLine.CR,

  @Attribute
  var encoding: String = StandardCharsets.US_ASCII.name()
)

@Tag("serial-connections")
data class SerialProfilesState(
  @Tag("default")
  var defaultProfile: SerialPortProfile = SerialPortProfile(baudRate = 115200),
  @XMap(propertyElementName = "serial-profiles",
        entryTagName = "serial-profile",
        keyAttributeName = "name"
  )
  var profiles: MutableMap<String, SerialPortProfile> = mutableMapOf()
)