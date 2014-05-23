package org.jetbrains.appcode.reveal;

import junit.framework.TestCase;

import static org.jetbrains.appcode.reveal.RevealRunConfigurationExtension.readFingerprint;

public class CodesignTest extends TestCase {
  public void testReadingFingerprint() throws Exception {
    assertEquals("6339CA59E1D63E9B0D40AB6E64B8EF104D4D4E4B",
                 readFingerprint("SHA1 Fingerprint=63:39:CA:59:E1:D6:3E:9B:0D:40:AB:6E:64:B8:EF:10:4D:4D:4E:4B"));
    assertEquals("6339CA59E1D63E9B0D40AB6E64B8EF104D4D4E4B",
                 readFingerprint("blah Fingerprint=63:39:CA:59:E1:D6:3E:9B:0D:40:AB:6E:64:B8:EF:10:4D:4D:4E:4B"));

    assertEquals("6339CA59E1", readFingerprint("SHA1 Fingerprint=63:39:CA:59:E1"));
    
    assertEquals(null, readFingerprint("blah=63:39:CA:59:E1:D6:3E:9B:0D:40:AB:6E:64:B8:EF:10:4D:4D:4E:4B"));
    assertEquals(null, readFingerprint("SHA1 Fingerprint=63:39:CA:59:E1:"));
    assertEquals(null, readFingerprint("SHA1 Fingerprint=63"));
    assertEquals(null, readFingerprint("SHA1 Fingerprint=:63"));
    assertEquals(null, readFingerprint("SHA1 Fingerprint=63:"));
    assertEquals(null, readFingerprint("SHA1 Fingerprint="));
    assertEquals(null, readFingerprint("SHA1 Fingerprint=asdada"));
  }
}