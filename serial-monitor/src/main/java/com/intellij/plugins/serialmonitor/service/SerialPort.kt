package com.intellij.plugins.serialmonitor.service

import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.SerialPortProfile
import org.jetbrains.annotations.Nls

class SerialPortException(message: @Nls String) : Exception(message)

interface SerialPort {

  fun getSystemName(): @NlsSafe String
  fun getDescriptiveName(): @Nls String

  @Throws(SerialPortException::class)
  fun connect(profile: SerialPortProfile, listener: SerialPortListener, rts: Boolean, dtr: Boolean)

  @Throws(SerialPortException::class)
  fun disconnect()

  @Throws(SerialPortException::class)
  fun write(data: ByteArray): Int

  @Throws(SerialPortException::class)
  fun setRTS(value: Boolean)

  @Throws(SerialPortException::class)
  fun setDTR(value: Boolean)

  @Throws(SerialPortException::class)
  fun getCTS(): Boolean

  @Throws(SerialPortException::class)
  fun getDSR(): Boolean

  interface SerialPortListener {
    fun onDataReceived(data: ByteArray) {}
    fun onCTSChanged(state: Boolean) {}
    fun onDSRChanged(state: Boolean) {}
  }
}