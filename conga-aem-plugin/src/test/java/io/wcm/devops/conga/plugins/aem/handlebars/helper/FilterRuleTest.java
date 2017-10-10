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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class FilterRuleTest {

  @Test(expected = IllegalArgumentException.class)
  public void testEmpty() {
    new FilterRule(ImmutableMap.of());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOnlyType() {
    new FilterRule(ImmutableMap.of("type", "allow"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTypeMissing() {
    new FilterRule(ImmutableMap.of("glob", "abc"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalType() {
    new FilterRule(ImmutableMap.of("glob", "abc", "type", "this_is_not_a_valid_value"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalRegexp() {
    new FilterRule(ImmutableMap.of("glob", "(abc", "type", "allow"));
  }

  @Test
  public void testSimple() {
    FilterRule underTest = new FilterRule(ImmutableMap.of("glob", "abc", "type", "allow"));
    assertEquals(FilterType.ALLOW, underTest.getType());
    assertNull(underTest.getMethod());
    assertNull(underTest.getUrl());
    assertNull(underTest.getQuery());
    assertNull(underTest.getProtocol());
    assertNull(underTest.getPath());
    assertNull(underTest.getSelectors());
    assertNull(underTest.getExtension());
    assertNull(underTest.getSuffix());
    assertEquals("abc", underTest.getGlob());
  }

  @Test
  public void testAll() {
    Map<String, Object> map = new HashMap<>();
    map.put("type", "allow");
    map.put("method", "method1");
    map.put("url", "url1");
    map.put("query", "query1");
    map.put("protocol", "protocol1");
    map.put("path", "path1");
    map.put("selectors", "selector1");
    map.put("extension", "extension1");
    map.put("suffix", "suffix1");
    map.put("glob", "glob1");
    FilterRule underTest = new FilterRule(map);
    assertEquals(FilterType.ALLOW, underTest.getType());
    assertEquals("method1", underTest.getMethod());
    assertEquals("url1", underTest.getUrl());
    assertEquals("query1", underTest.getQuery());
    assertEquals("protocol1", underTest.getProtocol());
    assertEquals("path1", underTest.getPath());
    assertEquals("selector1", underTest.getSelectors());
    assertEquals("extension1", underTest.getExtension());
    assertEquals("suffix1", underTest.getSuffix());
    assertEquals("glob1", underTest.getGlob());
  }

}
