/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
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
package io.wcm.devops.conga.plugins.aem.handlebars.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

class HttpdFilterTest {

  @Test
  void testEmpty() {
    assertThrows(IllegalArgumentException.class, () -> {
      new HttpdFilter(Map.of());
    });
  }

  @Test
  void testOnlyType() {
    assertThrows(IllegalArgumentException.class, () -> {
      new HttpdFilter(Map.of("type", "allow"));
    });
  }

  @Test
  void testTypeMissing() {
    assertThrows(IllegalArgumentException.class, () -> {
      new HttpdFilter(Map.of("location", "/abc"));
    });
  }

  @Test
  void testIllegalType() {
    assertThrows(IllegalArgumentException.class, () -> {
      new HttpdFilter(Map.of("location", "/abc", "type", "this_is_not_a_valid_value"));
    });
  }

  @Test
  void testIllegalRegexp() {
    assertThrows(IllegalArgumentException.class, () -> {
      new HttpdFilter(Map.of("locationMatch", "(abc", "type", "allow"));
    });
  }

  @Test
  void testInvalidParam() {
    assertThrows(IllegalArgumentException.class, () -> {
      new HttpdFilter(Map.of("location", "/abc", "type", "allow", "invalidParam", "value"));
    });
  }

  @Test
  void testMoreThanOneTargetParam() {
    assertThrows(IllegalArgumentException.class, () -> {
      new HttpdFilter(Map.of("location", "/abc", "locationMatch", "/abc(/.*)?", "type", "allow"));
    });
  }

  @Test
  void testLocation() {
    HttpdFilter underTest = new HttpdFilter(Map.of("location", "/abc", "type", "allow"));
    assertEquals(HttpdFilterType.ALLOW, underTest.getType());
    assertEquals("/abc", underTest.getLocation());
    assertNull(underTest.getLocationMatch());
    assertEquals("location=/abc, type=allow", underTest.toString());
  }

  @Test
  void testLocationMatch() {
    HttpdFilter underTest = new HttpdFilter(Map.of("locationMatch", "/abc(/.*)?", "type", "allow"));
    assertEquals(HttpdFilterType.ALLOW, underTest.getType());
    assertNull(underTest.getLocation());
    assertEquals("/abc(/.*)?", underTest.getLocationMatch());
    assertEquals("locationMatch=/abc(/.*)?, type=allow", underTest.toString());
  }

}
