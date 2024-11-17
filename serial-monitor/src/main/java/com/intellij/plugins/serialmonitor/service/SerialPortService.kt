package com.intellij.plugins.serialmonitor.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.NlsSafe
import com.intellij.plugins.serialmonitor.Parity
import com.intellij.plugins.serialmonitor.SerialMonitorCollector
import com.intellij.plugins.serialmonitor.SerialMonitorException
import com.intellij.plugins.serialmonitor.StopBits
import com.intellij.plugins.serialmonitor.service.SerialPortService.HardwareLinesStatus.Companion.hardwareLinesStatus
import com.intellij.plugins.serialmonitor.service.SerialPortsListener.Companion.SERIAL_PORTS_TOPIC
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle
import com.intellij.util.ConcurrencyUtil
import com.intellij.util.application
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import jssc.SerialPort
import jssc.SerialPort.*
import jssc.SerialPortEvent
import jssc.SerialPortEventListener
import jssc.SerialPortException
import jssc.SerialPortList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.regex.Pattern
import kotlin.io.path.Path
import kotlin.io.path.pathString

@Service
class SerialPortService(val cs: CoroutineScope) : Disposable {

  private val portWatcher: ScheduledExecutorService = ConcurrencyUtil.newSingleScheduledThreadExecutor("Serial Port Watcher")
    .apply { scheduleWithFixedDelay({ rescanPorts() }, 0, 500, TimeUnit.MILLISECONDS) }

  @Volatile
  private var portNames: Set<String> = emptySet()
  private val connections: MutableMap<String, SerialConnection> = ConcurrentHashMap()

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

  private val MATCH_ALL_PATTERN = Pattern.compile(".*")

  private fun scanPorts(): Set<String> =
    TreeSet(NAME_COMPARATOR).apply {
      // Get devices which SerialPortList recognizes as serial ports
      addAll(SerialPortList.getPortNames())
      // Include symlinks to known ports
      addAll(SerialPortList.getPortNames(MATCH_ALL_PATTERN)
        .filter {
          runCatching {
            val path = Path(it)
            if (!Files.isSymbolicLink(path)) return@filter false
            this.contains(path.toRealPath().pathString)
          }.getOrDefault(false)
        }
      )
    }

  private fun rescanPorts() {
    val portList = scanPorts()
    var changeDetected = portList != portNames
    if (changeDetected) {
      for (name in portNames) {
        if (!portList.contains(name)) {
          //port disappeared
          connections[name]?.closeSilently(false)
        }
      }
    }
    for (name in portList) {
      if (connections[name]?.getStatus() == PortStatus.UNAVAILABLE_DISCONNECTED) {
        connections[name]?.setStatus(PortStatus.DISCONNECTED)
        changeDetected = true
      }
    }
    portNames = portList
    if (changeDetected) {
      portMessageTopic().portsStatusChanged()
    }
  }

  private fun portMessageTopic(): SerialPortsListener =
    ApplicationManager.getApplication().messageBus.syncPublisher(SERIAL_PORTS_TOPIC)

  fun getPortsNames(): Set<String> = portNames

  fun newConnection(portName: String): SerialConnection {
    connections[portName]?.also { Disposer.dispose(it) }
    val serialConnection = SerialConnection(portName)
    connections[portName] = serialConnection
    return serialConnection
  }

  fun portStatus(name: String?): PortStatus {
    if (!portNames.contains(name)) {
      return if (connections.containsKey(name)) PortStatus.UNAVAILABLE_DISCONNECTED else PortStatus.UNAVAILABLE
    }
    return connections[name]?.getStatus() ?: PortStatus.READY
  }

  data class HardwareLinesStatus(val cts: Boolean, val dsr: Boolean) {
    companion object {
      val EMPTY = HardwareLinesStatus(false, false)
      fun SerialPort.hardwareLinesStatus(): HardwareLinesStatus {
        val lines = this.linesStatus
        return HardwareLinesStatus(
          lines[0] == 1,
          lines[1] == 1
        )
      }
    }
  }

  inner class SerialConnection(val portName: @NlsSafe String) : Disposable {

    var dataListener: Consumer<ByteArray>? = null

    var eventListener: ((SerialPortEvent)->Unit)? = null

    @Volatile
    private var port: SerialPort? = null

    @Volatile
    private var status: PortStatus = PortStatus.DISCONNECTED

    private var localEcho: Boolean = false

    var rts = false
      @Throws(SerialMonitorException::class)
      set(value) {
        runWithExceptionWrapping(value) {
          port?.setRTS(value)
          field = value
        }
      }

    var dtr = false
      @Throws(SerialMonitorException::class)
      set(value) {
        runWithExceptionWrapping(value) {
          port?.setDTR(value)
          field = value
        }
      }

    val hardwareLinesStatus: HardwareLinesStatus
      get() = port?.takeIf{ status == PortStatus.CONNECTED }?.hardwareLinesStatus() ?: HardwareLinesStatus.EMPTY

    @Throws(SerialMonitorException::class)
    private fun <T> runWithExceptionWrapping(value: T, func: (T)->Unit) {
      try {
        func(value)
      }
      catch (e: SerialPortException) {
        throw SerialMonitorException(SerialMonitorBundle.message("port.modify.error", e.port.portName, e.exceptionType))
      }
      catch (e: Exception) {
        throw SerialMonitorException(portName, e)
      }
    }

    override fun dispose() {
      closeSilently(true)
      connections.remove(portName, this)
      application.executeOnPooledThread(::rescanPorts)
    }

    fun getStatus(): PortStatus = status
    internal fun setStatus(value: PortStatus) {
      status = value
    }

    @Throws(SerialMonitorException::class)
    fun close(portAvailable: Boolean) {
      try {
        port?.closePort()
      }
      catch (e: SerialPortException) {
        throw SerialMonitorException(SerialMonitorBundle.message("port.close.error", e.port.portName, e.exceptionType))
      }
      catch (e: Throwable) {
        throw SerialMonitorException(portName, e)
      }
      finally {
        status = if (portAvailable) PortStatus.DISCONNECTED else PortStatus.UNAVAILABLE_DISCONNECTED
        port = null
        portMessageTopic().portsStatusChanged()
      }
    }

    fun closeSilently(portAvailable: Boolean) {
      try {
        close(portAvailable)
      }
      catch (_: Throwable) {
      }
    }

    private val listener: SerialPortEventListener = SerialPortEventListener { event ->
      if (event.eventType and MASK_RXCHAR != 0) {
        val readBytes = port?.readBytes()
        if (readBytes?.isNotEmpty() == true) {
          dataListener?.accept(readBytes)
        }
      }
      this@SerialPortService.cs.launch(Dispatchers.EDT) {
        eventListener?.invoke(event)
      }
    }

    @RequiresBackgroundThread
    @Throws(SerialMonitorException::class)
    fun connect(baudRate: Int, bits: Int, stopBits: StopBits, parity: Parity, localEcho: Boolean) {
      this.status = PortStatus.CONNECTING
      this.localEcho = localEcho
      val newPort = SerialPort(portName)
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
        newPort.openPort()
        newPort.addEventListener(listener, MASK_RXCHAR or MASK_ERR or MASK_CTS or MASK_DSR)

        if (!newPort.setParams(baudRate, bits, portStopBits, portParity, rts, dtr)) {
          SerialMonitorCollector.logConnect(baudRate, false)
          throw SerialMonitorException(SerialMonitorBundle.message("serial.port.parameters.wrong"))
        }
        port = newPort
        SerialMonitorCollector.logConnect(baudRate, true)
        status = PortStatus.CONNECTED
        portMessageTopic().portsStatusChanged()
      }
      catch (e: Throwable) {
        SerialMonitorCollector.logConnect(baudRate, false)
        try {
          newPort.closePort()
        }
        catch (_: Throwable) {
        }
        status = PortStatus.UNAVAILABLE_DISCONNECTED
        portMessageTopic().portsStatusChanged()
        if (e is SerialPortException) {
          throw SerialMonitorException(SerialMonitorBundle.message("port.connect.error", e.port.portName, e.exceptionType))
        }
        else {
          throw SerialMonitorException(newPort.portName, e)
        }
      }
    }

    fun write(data: ByteArray) {
      port?.writeBytes(data)
      if (localEcho) {
        dataListener?.accept(data)
      }
    }
  }

  override fun dispose() {
    portWatcher.shutdown()
  }

}

