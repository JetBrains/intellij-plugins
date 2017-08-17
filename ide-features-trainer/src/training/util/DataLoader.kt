package training.util

import org.jdom.Document
import org.jdom.Element
import org.jdom.input.SAXBuilder
import java.awt.image.BufferedImage
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

  private fun getResourceAsStream(pathFromData: String): InputStream {
    val fullPath = DATA_PATH + pathFromData
    return this.javaClass.getResourceAsStream(fullPath) ?: throw Exception("File with \"$pathFromData\" doesn't exist")
  }

  private fun getXmlDocument(pathFromData: String): Document {
    val resourceStream = getResourceAsStream(pathFromData)
    val builder = SAXBuilder()
    return builder.build(resourceStream) ?: throw Exception("Unable to get document for xml: $pathFromData")
  }

  fun getURL(fileName: String): URL
    = this.javaClass.getResource(fileName)

  fun getDialogURL(fileName: String): URL
    = this.javaClass.getResource(DIALOGS_PATH + fileName)

  fun getBufferedImage(path: String): BufferedImage? {
    val resourceStream = this.javaClass.getResourceAsStream(IMAGES_PATH + path)
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
