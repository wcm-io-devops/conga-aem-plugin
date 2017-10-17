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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class HttpdFilterTest {

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    new HttpdFilter(ImmutableMap.of());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOnlyType() {
    new HttpdFilter(ImmutableMap.of("type", "allow"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTypeMissing() {
    new HttpdFilter(ImmutableMap.of("location", "/abc"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalType() {
    new HttpdFilter(ImmutableMap.of("location", "/abc", "type", "this_is_not_a_valid_value"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalRegexp() {
    new HttpdFilter(ImmutableMap.of("locationMatch", "(abc", "type", "allow"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidParam() {
    new HttpdFilter(ImmutableMap.of("location", "/abc", "type", "allow", "invalidParam", "value"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMoreThanOneTargetParam() {
    new HttpdFilter(ImmutableMap.of("location", "/abc", "locationMatch", "/abc(/.*)?", "type", "allow"));
  }

  @Test
  public void testLocation() {
    HttpdFilter underTest = new HttpdFilter(ImmutableMap.of("location", "/abc", "type", "allow"));
    assertEquals(HttpdFilterType.ALLOW, underTest.getType());
    assertEquals("/abc", underTest.getLocation());
    assertNull(underTest.getLocationMatch());
    assertEquals("type=allow, location=/abc", underTest.toString());
  }

  @Test
  public void testLocationMatch() {
    HttpdFilter underTest = new HttpdFilter(ImmutableMap.of("locationMatch", "/abc(/.*)?", "type", "allow"));
    assertEquals(HttpdFilterType.ALLOW, underTest.getType());
    assertNull(underTest.getLocation());
    assertEquals("/abc(/.*)?", underTest.getLocationMatch());
    assertEquals("type=allow, locationMatch=/abc(/.*)?", underTest.toString());
  }

}
