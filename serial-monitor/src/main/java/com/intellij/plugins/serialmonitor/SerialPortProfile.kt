package com.intellij.plugins.serialmonitor

import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.SerialProfileService.*
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import com.intellij.util.xmlb.annotations.XMap
import org.jetbrains.annotations.PropertyKey
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
  var newLine: NewLine = NewLine.CRLF,

  @Attribute
  var encoding: String = StandardCharsets.US_ASCII.name(),

  @Attribute
  var localEcho: Boolean = false,

  @Attribute
  var showHardwareControls: Boolean = false,

  ) {
  fun defaultName(): @NlsSafe String {
    return if (bits != 8 || stopBits != StopBits.BITS_1 || parity != Parity.NONE) {
      "$portName-$baudRate-$bits${parity.shortName()}$stopBits"
    }
    else {
      "$portName-$baudRate"
    }
  }
}

val StandardBauds: List<Int> = listOf(300, 600, 1200, 2400, 4800, 9600, 19200, 28800, 38400, 57600, 76800, 115200, 128000,
                                      230400, 256000, 460800, 576000, 921600, 1024000)

val SerialBits: List<Int> = listOf(8, 7, 6, 5)

enum class Parity(private val displayKey: @PropertyKey(resourceBundle = SerialMonitorBundle.BUNDLE) String) {
  ODD("uart.parity.odd"), EVEN("uart.parity.even"), NONE("uart.parity.none");

  override fun toString() = SerialMonitorBundle.message(displayKey)

  fun shortName(): String = toString().substring(0, 1).uppercase()
}

enum class StopBits(private val displayKey: String) {
  BITS_1("uart.stopbits.1"), BITS_2("uart.stopbits.2"), BITS_1_5("uart.stopbits.1.5");

  override fun toString() = SerialMonitorBundle.message(displayKey)
}

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