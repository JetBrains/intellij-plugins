package com.intellij.plugins.serialmonitor.service

import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_CTS
import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DSR
import com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_PORT_DISCONNECTED
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import com.fazecast.jSerialComm.SerialPortInvalidPortException
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.Parity
import com.intellij.plugins.serialmonitor.SerialMonitorException
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.StopBits
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle
import com.intellij.util.system.OS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.Nls

private typealias JSerialComm_SerialPort = com.fazecast.jSerialComm.SerialPort

private val JSerialComm_SerialPort.systemName: @NlsSafe String get() = if (OS.CURRENT == OS.Windows) systemPortName else systemPortPath

class JSerialCommPortProvider : SerialPortProvider {
  override suspend fun scanAvailablePorts(): List<String> = withContext(Dispatchers.IO) {
    JSerialComm_SerialPort.getCommPorts().map { it.systemName }
  }

  override fun createPort(portName: String): SerialPort {
    return JSerialCommPort.create(portName)
  }
}

class JSerialCommPort : SerialPort {

  private val serialPort: JSerialComm_SerialPort

  private constructor(port: JSerialComm_SerialPort) {
    serialPort = port
  }

  companion object {
    @Throws(SerialMonitorException::class)
    fun create(systemPortName: String): JSerialCommPort {
      try {
        return JSerialCommPort(JSerialComm_SerialPort.getCommPort(systemPortName))
      }
      catch (e: SerialPortInvalidPortException) {
        throw SerialPortException(e.message ?: "")
      }
    }
  }

  override fun getSystemName(): @NlsSafe String = serialPort.systemName
  override fun getDescriptiveName(): @Nls String = if (OS.CURRENT == OS.Windows) serialPort.portDescription else serialPort.descriptivePortName

  override fun connect(profile: SerialPortProfile, listener: SerialPort.SerialPortListener, rts: Boolean, dtr: Boolean) {
    checkSuccess(serialPort.setRTS(rts)) { SerialMonitorBundle.message("serial.port.rts.init.failed") }
    checkSuccess(serialPort.setDTR(dtr)) { SerialMonitorBundle.message("serial.port.dtr.init.failed") }
    check(addListener(listener)) { SerialMonitorBundle.message("serial.port.listener.failed") }
    with(profile) {
      val portStopBits = convertStopBits(stopBits)
      val portParity = convertParity(parity)
      checkSuccess(serialPort.setComPortParameters(baudRate, bits, portStopBits, portParity)) {
        SerialMonitorBundle.message("serial.port.parameters.wrong")
      }
    }
    checkSuccess(serialPort.openPort()) { SerialMonitorBundle.message("serial.port.open.failed") }
  }

  private fun convertParity(parity: Parity): Int = when (parity) {
    Parity.EVEN -> JSerialComm_SerialPort.EVEN_PARITY
    Parity.ODD -> JSerialComm_SerialPort.ODD_PARITY
    else -> JSerialComm_SerialPort.NO_PARITY
  }

  private fun convertStopBits(stopBits: StopBits): Int = when (stopBits) {
    StopBits.BITS_2 -> JSerialComm_SerialPort.TWO_STOP_BITS
    StopBits.BITS_1_5 -> JSerialComm_SerialPort.ONE_POINT_FIVE_STOP_BITS
    else -> JSerialComm_SerialPort.ONE_STOP_BIT
  }

  override fun disconnect() {
    checkSuccess(serialPort.closePort()) {
      SerialMonitorBundle.message("port.close.error", serialPort.systemPortName)
    }
  }

  override fun write(data: ByteArray): Int {
    return serialPort.writeBytes(data, data.size)
  }

  override fun setRTS(value: Boolean) {
    checkSuccess(serialPort.setRTS(value)) { SerialMonitorBundle.message("serial.port.rts.update.failed") }
  }

  override fun setDTR(value: Boolean) {
    checkSuccess(serialPort.setDTR(value)) { SerialMonitorBundle.message("serial.port.dtr.update.failed") }
  }

  override fun getCTS(): Boolean = serialPort.cts
  override fun getDSR(): Boolean = serialPort.dsr

  fun addListener(listener: SerialPort.SerialPortListener): Boolean {
    return serialPort.addDataListener(object : SerialPortDataListener {
      override fun getListeningEvents(): Int = LISTENING_EVENT_DATA_RECEIVED or LISTENING_EVENT_CTS or LISTENING_EVENT_DSR or LISTENING_EVENT_PORT_DISCONNECTED
      override fun serialEvent(event: SerialPortEvent) {
        bitFlagSwitch(event.eventType) {
          whenSet(LISTENING_EVENT_DATA_RECEIVED) {
            listener.onDataReceived(event.receivedData)
          }
          whenSet(LISTENING_EVENT_CTS) {
            listener.onCTSChanged(serialPort.cts)
          }
          whenSet(LISTENING_EVENT_DSR) {
            listener.onDSRChanged(serialPort.dsr)
          }
          whenSet(LISTENING_EVENT_PORT_DISCONNECTED) {
            serialPort.closePort()
          }
        }
      }
    })
  }

  private fun JSerialComm_SerialPort.setRTS(value: Boolean): Boolean = when(value) {
    true -> setRTS()
    false -> clearRTS()
  }

  private fun JSerialComm_SerialPort.setDTR(value: Boolean): Boolean = when(value) {
    true -> setDTR()
    false -> clearDTR()
  }

  @Throws(SerialPortException::class)
  private fun checkSuccess(success: Boolean, lazyMessage: @Nls ()->String) {
    if (!success){
      @NlsSafe val message = lazyMessage()
      throw SerialPortException(message)
    }
  }
}

private class BitFlagSwitchContext(private val value: Int) {
  fun whenSet(bitMask: Int, action: ()->Unit) {
    if (value and bitMask != 0) action()
  }
}

private fun bitFlagSwitch(value: Int, cases: BitFlagSwitchContext.()->Unit){
  BitFlagSwitchContext(value).cases()
}