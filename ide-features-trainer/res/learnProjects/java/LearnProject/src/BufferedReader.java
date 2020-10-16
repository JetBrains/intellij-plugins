import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.InputStream;
import java.util.List;

class BufferedReader {
  private InputStream inputStream;
  private int maxBufferSize = 1024;
  private byte[] buffer;

  public BufferedReader(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public BufferedReader(InputStream inputStream, int maxBufferSize) {
    this.inputStream = inputStream;
    this.maxBufferSize = maxBufferSize;
  }

  /**
   * @return one byte from input stream
   */
  public byte read() {
    throw new NotImplementedException();
  }

  /**
   * @return n bytes from input stream
   */
  public byte[] read(int n) {
    throw new NotImplementedException();
  }

  /**
   * @return one line from input stream as String
   */
  public String readLine() {
    throw new NotImplementedException();
  }

  /**
   * @return all lines from input stream as List of Strings
   */
  public List<String> lines() {
    throw new NotImplementedException();
  }
}