/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.devops.conga.plugins.aem.crypto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CryptoStringTest {

  @Test
  public void testIsCryptoString() {
    assertTrue(CryptoString.isCryptoString("{abc}"));

    assertFalse(CryptoString.isCryptoString(null));
    assertFalse(CryptoString.isCryptoString(""));
    assertFalse(CryptoString.isCryptoString("abc"));
    assertFalse(CryptoString.isCryptoString("{abc"));
    assertFalse(CryptoString.isCryptoString("abc}"));
    assertFalse(CryptoString.isCryptoString("{abc} "));
  }

  @Test
  public void testConversion() {
    byte[] input = new byte[] { 0x01, 0x02, 0x03, 0x04, (byte)0x99, (byte)0xFF };

    String encrypted = CryptoString.toString(input);
    assertTrue(CryptoString.isCryptoString(encrypted));
    byte[] output = CryptoString.toByteArray(encrypted);

    assertArrayEquals(input, output);
  }


  @Test
  public void testToByteArrayNull() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      CryptoString.toByteArray(null);
    });
  }

  @Test
  public void testToByteArrayIllegalString() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      CryptoString.toByteArray("abc");
    });
  }

  @Test
  public void testToStringNull() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      CryptoString.toString(null);
    });
  }

  @Test
  public void testToStringEmptyArray() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> {
      CryptoString.toString(new byte[0]);
    });
  }

}
