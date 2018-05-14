package training.util

import org.jdom.Document
import org.jdom.Element
import org.jdom.input.SAXBuilder
import java.awt.image.BufferedImage
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO

/**
 * Created by karashevich on 31/03/15.
 */
object DataLoader {
  val DATA_PATH = "/data/"
  val IMAGES_PATH = "/img/"
  val DIALOGS_PATH = "/dialogs/"
  //Path to use for online reloading. Should contain the full path to res/ folder, like /Users/user/training/res/.
  val LIVE_DATA_PATH = ""

  val liveMode: Boolean
    get() {
      return !LIVE_DATA_PATH.isEmpty()
    }

  private fun getResourceAsStream(pathFromData: String): InputStream {
    val fullPath =
        LIVE_DATA_PATH + DATA_PATH + pathFromData
    return if (liveMode) {
      FileInputStream(fullPath)
    } else this.javaClass.getResourceAsStream(fullPath)
        ?: throw Exception("File with \"$pathFromData\" doesn't exist")
  }

  private fun getXmlDocument(pathFromData: String): Document {
    val resourceStream = getResourceAsStream(pathFromData)
    val builder = SAXBuilder()
    return builder.build(resourceStream) ?: throw Exception("Unable to get document for xml: $pathFromData")
  }

  fun getURL(fileName: String): URL = if (liveMode) {
    URL(fileName)
  } else {
    this.javaClass.getResource(fileName)
  }

  fun getDialogURL(fileName: String): URL = if (liveMode) {
    URL(LIVE_DATA_PATH + DIALOGS_PATH + fileName)
  } else {
    this.javaClass.getResource(DIALOGS_PATH + fileName)
  }

  fun getBufferedImage(path: String): BufferedImage? {
    val fullPath = if (liveMode) {
      LIVE_DATA_PATH + IMAGES_PATH + path
    } else IMAGES_PATH + path
    val resourceStream = if (liveMode) {
      FileInputStream(fullPath)
    } else this.javaClass.getResourceAsStream(fullPath)
        ?: throw Exception("Image with \"$path\" doesn't exist")
    try {
      return ImageIO.read(resourceStream)
    } catch (e: IOException) {
      e.printStackTrace()
    }
    return null
  }

  fun getXmlRootElement(pathFromData: String): Element {
    return getXmlDocument(pathFromData).rootElement
  }

}
