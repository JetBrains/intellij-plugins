package org.flyti.roboflest;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.*;
import java.util.StringTokenizer;

public final class Roboflest {
  private int xOffset;
  private int yOffset;
  
  public void setStageOffset(int xOffset, int yOffset) {
    this.xOffset = xOffset;
    this.yOffset = yOffset;
  }

  public void setStageOffset(DataInput dataInput) throws IOException {
    setStageOffset(dataInput.readUnsignedShort(), dataInput.readUnsignedShort());
  }
  
  public void test(File file, Assert... asserts) throws Exception {
    BufferedReader input = new BufferedReader(new FileReader(file));
    Robot robot = new Robot();
    robot.setAutoDelay(300);
    
    String line;
    int assertIndex = 0;
    while ((line = input.readLine()) != null) {
      if (line.startsWith("move")) {
        StringTokenizer tokenizer = new StringTokenizer(line, " ");
        tokenizer.nextToken();
        robot.mouseMove(Integer.parseInt(tokenizer.nextToken()) + xOffset, Integer.parseInt(tokenizer.nextToken()) + yOffset);
      }
      else if (line.equals("down")) {
        robot.mousePress(InputEvent.BUTTON1_MASK);
      }
      else if (line.startsWith("up")) {
        if (line.length() > 2) {
          robot.delay(Integer.parseInt(line.substring(3)));
        }
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
      }
      else if (line.startsWith("assert")) {
        asserts[assertIndex++].test();
      }
      else {
        throw new IllegalArgumentException(line);
      }
    }
  }

  public interface Assert {
    void test() throws Exception;
  }
}