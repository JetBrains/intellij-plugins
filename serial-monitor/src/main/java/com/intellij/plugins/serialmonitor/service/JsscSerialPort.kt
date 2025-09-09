package com.intellij.plugins.serialmonitor.service

import com.intellij.openapi.diagnostic.getOrHandleException
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.Parity
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.StopBits
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle
import jssc.SerialPort.*
import jssc.SerialPortList
import org.jetbrains.annotations.Nls
import java.nio.file.Files
import java.util.regex.Pattern
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.jvm.Throws

class JsscSerialPort : SerialPort {

  private constructor(port: jssc.SerialPort) {
    this.port = port
  }

  private val port: jssc.SerialPort

  override fun getSystemName(): @NlsSafe String = port.portName
  override fun getDescriptiveName(): @Nls String = port.portName

  override fun connect(profile: SerialPortProfile, listener: SerialPort.SerialPortListener, rts: Boolean, dtr: Boolean) {
    runWithExceptionWrapping(SerialMonitorBundle.message("serial.port.open.failed")) {
      port.openPort()
    }
    with(profile) {
      val portStopBits = when (stopBits) {
        StopBits.BITS_2 -> STOPBITS_2
        StopBits.BITS_1_5 -> STOPBITS_1_5
        else -> STOPBITS_1
      }
      val portParity = when (parity) {
        Parity.EVEN -> PARITY_EVEN
        Parity.ODD -> PARITY_ODD
        else -> PARITY_NONE
      }
      runWithExceptionWrapping(SerialMonitorBundle.message("serial.port.parameters.wrong")) {
        port.setParams(baudRate, bits, portStopBits, portParity, rts, dtr)
      }
    }
    runWithExceptionWrapping(SerialMonitorBundle.message("serial.port.listener.failed")) {
      port.addEventListener({ event ->
        bitFlagSwitch(event.eventType) {
          case(MASK_RXCHAR) {
            val readBytes = port.readBytes()
            if (readBytes?.isNotEmpty() == true) {
              listener.onDataReceived(readBytes)
            }
          }
          case(MASK_CTS) {
            listener.onCTSChanged(event.eventValue != 0)
          }
          case(MASK_DSR) {
            listener.onDSRChanged(event.eventValue != 0)
          }
        }
      }, MASK_RXCHAR or MASK_CTS or MASK_DSR)
    }
  }

  override fun disconnect() {
    port.closePort()
  }

  override fun write(data: ByteArray): Int {
    if (port.writeBytes(data)) {
      return data.size
    }
    return -1
  }

  override fun setRTS(value: Boolean): Unit = runWithExceptionWrapping(SerialMonitorBundle.message("serial.port.rts.update.failed")) {
    port.setRTS(value)
  }

  override fun setDTR(value: Boolean): Unit = runWithExceptionWrapping(SerialMonitorBundle.message("serial.port.dtr.update.failed")){
    port.setDTR(value)
  }

  override fun getCTS(): Boolean = runWithExceptionWrapping(SerialMonitorBundle.message("serial.port.cts.read.failed")) {
    port.isCTS
  }

  override fun getDSR(): Boolean = runWithExceptionWrapping(SerialMonitorBundle.message("serial.port.dsr.read.failed")) {
    port.isDSR
  }

  object Provider : SerialPort.SerialPortProvider {

    private val MATCH_ALL_PATTERN = Pattern.compile(".*")

    override fun scanAvailablePorts(): List<String> = buildList {
      addAll(SerialPortList.getPortNames())

      val symLinks = SerialPortList.getPortNames(MATCH_ALL_PATTERN).filter {
        runCatching {
          val path = Path(it)
          if (!Files.isSymbolicLink(path)) return@filter false
          this.contains(path.toRealPath().pathString)
        }.getOrDefault(false)
      }
      addAll(symLinks)
    }
    override fun createPort(portName: String): SerialPort = JsscSerialPort(jssc.SerialPort(portName))
  }
}

@Throws(SerialPortException::class)
private fun <T> runWithExceptionWrapping(errorMessage: @Nls String = "", action: () -> T): T = runCatching {
  action()
}.getOrHandleException { e->
  logger<JsscSerialPort>().debug(e)
  throw SerialPortException(errorMessage)
}!!

private class BitFlagSwitchContext(private val value: Int) {
  fun case(bitMask: Int, action: ()->Unit) {
    if (value and bitMask != 0) action()
  }
}

private fun bitFlagSwitch(value: Int, cases: BitFlagSwitchContext.()->Unit){
  BitFlagSwitchContext(value).cases()
}