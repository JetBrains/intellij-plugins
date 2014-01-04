import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CrawlerUtils {
  public static void copyStream(InputStream is, OutputStream os) throws IOException {
    byte[] buffer = new byte[1024];
    int len = is.read(buffer);
    while (len != -1) {
      os.write(buffer, 0, len);
      len = is.read(buffer);
    }
  }
}
