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

import org.apache.commons.lang3.StringUtils;

/**
 * Converts byte array to crypto string and back in the same fashion as it is done by the Adobe Granite Crypto
 * implementation.
 */
public final class CryptoString {

  private CryptoString() {
    // static methods only
  }

  /**
   * Checks if the given string contains an encrypted string.
   * @param text String to check
   * @return true if string is assumed to be encrypted
   */
  public static boolean isCryptoString(final String text) {
    // very simplified check - just check for start and end of curly braces.
    return text != null && text.length() > 2
        && StringUtils.startsWith(text, "{") && StringUtils.endsWith(text, "}");
  }

  /**
   * Converts byte array to hex string enclosed in curly braces.
   * @param data Binary data
   * @return Binary data string
   */
  public static String toString(byte[] data) {
    if (data == null || data.length == 0) {
      throw new IllegalArgumentException("Data is null or empty.");
    }
    StringBuilder text = new StringBuilder(data.length * 2 + 2);
    text.append("{");
    for (int i = 0; i < data.length; i++) {
      String hex = Integer.toHexString(data[i] & 0xff);
      if (hex.length() < 2) {
        text.append("0");
      }
      text.append(hex);
    }
    text.append("}");
    return text.toString();
  }

  /**
   * Converts hex string enclosed in curly braces back to byte array.
   * @param text Binary data string
   * @return Binary data
   */
  public static byte[] toByteArray(String text) {
    if (!isCryptoString(text)) {
      throw new IllegalArgumentException("Text does not seem to be encrypted.");
    }
    String rawText = text.substring(1, text.length() - 1);
    byte[] data = new byte[rawText.length() / 2];
    for (int i = 0; i < data.length; i++) {
      int pos = i * 2;
      String hex = rawText.substring(pos, pos + 2);
      int val = Integer.parseInt(hex, 16);
      data[i] = (byte)val;
    }
    return data;
  }

}
