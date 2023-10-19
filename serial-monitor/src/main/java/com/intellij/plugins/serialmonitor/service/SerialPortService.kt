package com.intellij.plugins.serialmonitor.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.Parity
import com.intellij.plugins.serialmonitor.SerialMonitorConnectCollector
import com.intellij.plugins.serialmonitor.SerialMonitorException
import com.intellij.plugins.serialmonitor.StopBits
import com.intellij.plugins.serialmonitor.service.SerialPortsListener.Companion.SERIAL_PORTS_TOPIC
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle
import com.intellij.util.ConcurrencyUtil
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import com.intellij.util.containers.ContainerUtil
import jssc.SerialPort
import jssc.SerialPort.*
import jssc.SerialPortEventListener
import jssc.SerialPortException
import jssc.SerialPortList
import org.jetbrains.annotations.Contract
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import kotlin.Comparator

@Service
class SerialPortService : Disposable {

  private val portWatcher: ScheduledExecutorService = ConcurrencyUtil.newSingleScheduledThreadExecutor("Serial Port Watcher")
    .apply { scheduleWithFixedDelay({ rescanPorts() }, 0, 500, TimeUnit.MILLISECONDS) }

  private var ports: Map<String, List<SerialConnection>> = emptyMap()

  private val NAME_COMPARATOR = object : Comparator<String> {

    private fun splitName(s: String): Pair<String, Int> {
      var digitIdx = s.length - 1
      var num = 0
      var mul = 1
      while (digitIdx >= 0 && s[digitIdx].isDigit()) {
        num += mul * (s[digitIdx] - '0')
        mul *= 10
        digitIdx--
      }
      return s.substring(0, digitIdx + 1) to num
    }

    override fun compare(name1: String, name2: String): Int {
      val (base1, idx1) = splitName(name1)
      val (base2, idx2) = splitName(name2)
      val result = String.CASE_INSENSITIVE_ORDER.compare(base1, base2)
      return if (result == 0) idx1 - idx2 else result
    }
  }

  private fun rescanPorts() {

    val newPortMap = TreeMap<String, List<SerialConnection>>(NAME_COMPARATOR)

    val portList = mutableSetOf(*SerialPortList.getPortNames())
    var changeDetected = false
    for (port in ports) {
      if (portList.remove(port.key)) {
        val connections = port.value.filter { it.getStatus() != PortStatus.RELEASED }
        newPortMap[port.key] = connections
      }
      else {
        changeDetected = true
      }
    }
    if (portList.isNotEmpty()) {
      changeDetected = true
      portList.forEach { newPortMap[it] = ContainerUtil.createConcurrentList() }
    }
    ports = newPortMap
    if (changeDetected) {
      portMessageTopic().portsStatusChanged()
    }
  }

  private fun portMessageTopic(): SerialPortsListener =
    ApplicationManager.getApplication().messageBus.syncPublisher(SERIAL_PORTS_TOPIC)

  fun getPortsNames(): Set<String> = ports.keys
  fun getPortsStatus(): Map<String, PortStatus> = ports.mapValues { portStatus(it.value) }

  fun connection(portName: String): SerialConnection = SerialConnection(portName)

  fun portStatus(portConnections: List<SerialConnection>?): PortStatus {
    if (portConnections == null) return PortStatus.MISSING
    var result = PortStatus.DISCONNECTED
    for (connection in portConnections) {
      when (connection.getStatus()) {
        PortStatus.BUSY -> return PortStatus.BUSY
        PortStatus.MISSING -> return PortStatus.MISSING
        PortStatus.CONNECTING -> result = PortStatus.CONNECTING
        PortStatus.CONNECTED -> return PortStatus.CONNECTED
        else -> Unit
      }
    }
    return result
  }

  fun portStatus(name: String?): PortStatus {
    if (name == null) return PortStatus.MISSING
    val v = ports[name]
    return portStatus(v)
  }

  inner class SerialConnection(val portName: @NlsSafe String) : Disposable {

    var dataListener: Consumer<ByteArray>? = null
    var connListener: SerialPortsListener? = null

    private var port: SerialPort = SerialPort(portName)

    @Volatile
    private var status: PortStatus = PortStatus.DISCONNECTED

    override fun dispose() {
      try {
        close()
      } catch (_: Throwable) {}
      status = PortStatus.RELEASED
    }

    fun getStatus(): PortStatus = status

    @Throws(SerialMonitorException::class)
    fun close() {
      port.apply {
        try {
          closePort()
        }
        catch (e: SerialPortException) {
          throw SerialMonitorException(e.localizedMessage)
        }
        catch (e: Throwable) {
          throw SerialMonitorException(portName, e)
        }
        finally {
          status = PortStatus.DISCONNECTED
          portMessageTopic().portsStatusChanged()
        }
      }
    }

    private val listener: SerialPortEventListener = SerialPortEventListener { event ->
      if (event.eventType and MASK_RXCHAR != 0) {
        val readBytes = port.readBytes()
        if (readBytes.isNotEmpty()) {
          dataListener?.accept(readBytes)
        }
      }
    }

    @RequiresBackgroundThread
    @Throws(SerialMonitorException::class)
    fun connect(baudRate: Int, bits: Int, stopBits: StopBits, parity: Parity) {
      status = PortStatus.CONNECTING
      portMessageTopic().portsStatusChanged()
      try {
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
        port.openPort()
        port.addEventListener(listener, MASK_RXCHAR or MASK_ERR)

        if (!port.setParams(baudRate, bits, portStopBits, portParity)) {
          SerialMonitorConnectCollector.logConnect(baudRate, false)
          throw SerialMonitorException(SerialMonitorBundle.message("serial.port.parameters.wrong"))
        }
        port.setFlowControlMode(FLOWCONTROL_NONE)
        port.setRTS(false)
        SerialMonitorConnectCollector.logConnect(baudRate, true)
        status = PortStatus.CONNECTED
        portMessageTopic().portsStatusChanged()
      }
      catch (e: Throwable) {
        SerialMonitorConnectCollector.logConnect(baudRate, false)
        try {
          port.closePort()
        }
        catch (_: Throwable) {
        }
        status = PortStatus.DISCONNECTED
        portMessageTopic().portsStatusChanged()
        if (e is SerialPortException) {
          throw SerialMonitorException(e.localizedMessage)
        }
        else {
          throw SerialMonitorException(port.portName, e)
        }
      }
    }

    fun write(data: ByteArray) {
      port.writeBytes(data)
    }
  }

  override fun dispose() {
    portWatcher.shutdown()
  }

  @Contract("null -> false")
  fun portUsable(portName: String?): Boolean = portUsable(portStatus(portName))

  companion object {
    @Contract("null -> false")
    fun portUsable(portStatus: PortStatus?): Boolean = portStatus !in listOf(null, PortStatus.CONNECTED, PortStatus.CONNECTING)
  }

  //todo CR/LF settings do not affect local echo
}

