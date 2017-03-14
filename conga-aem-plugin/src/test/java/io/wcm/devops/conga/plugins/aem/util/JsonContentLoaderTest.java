/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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
package io.wcm.devops.conga.plugins.aem.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class JsonContentLoaderTest {

  private JsonContentLoader underTest;
  private Map<String, Object> content;

  @Before
  public void setUp() throws Exception {
    underTest = new JsonContentLoader();
    try (InputStream is = getClass().getResourceAsStream("/json/content.json")) {
      content = underTest.load(is);
    }
  }

  @Test
  public void testPageJcrPrimaryType() {
    assertEquals("cq:Page", content.get("jcr:primaryType"));
  }

  @Test
  public void testPageContentProperties() {
    Map<String, Object> props = getDeep(content, "toolbar/profiles/jcr:content");
    assertEquals(true, props.get("hideInNav"));

    assertEquals(1234567890123L, props.get("longProp"));
    assertEquals(new BigDecimal("1.2345"), props.get("decimalProp"));
    assertEquals(true, props.get("booleanProp"));

    assertArrayEquals(new Long[] {
        1234567890123L, 55L
    }, (Long[])props.get("longPropMulti"));
    assertArrayEquals(new BigDecimal[] {
        new BigDecimal("1.2345"), new BigDecimal("1.1")
    }, (BigDecimal[])props.get("decimalPropMulti"));
    assertArrayEquals(new Boolean[] {
        true, false
    }, (Boolean[])props.get("booleanPropMulti"));
  }

  @Test
  public void testContentProperties() {
    Map<String, Object> props = getDeep(content, "jcr:content/header");
    assertEquals("/content/dam/sample/header.png", props.get("imageReference"));
  }

  @Test
  public void testCalendarEcmaFormat() {
    Map<String, Object> props = getDeep(content, "jcr:content");

    Calendar calendar = (Calendar)props.get("cq:lastModified");
    assertNotNull(calendar);

    calendar.setTimeZone(TimeZone.getTimeZone("GMT+2"));

    assertEquals(2014, calendar.get(Calendar.YEAR));
    assertEquals(4, calendar.get(Calendar.MONTH) + 1);
    assertEquals(22, calendar.get(Calendar.DAY_OF_MONTH));

    assertEquals(15, calendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(11, calendar.get(Calendar.MINUTE));
    assertEquals(24, calendar.get(Calendar.SECOND));
  }

  @Test
  public void testUTF8Chars() {
    Map<String, Object> props = getDeep(content, "jcr:content");

    assertEquals("äöüß€", props.get("utf8Property"));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getDeep(Map<String, Object> map, String path) {
    String name = StringUtils.substringBefore(path, "/");
    Object object = map.get(name);
    if (object == null || !(object instanceof Map)) {
      return null;
    }
    String remainingPath = StringUtils.substringAfter(path, "/");
    Map<String, Object> childMap = (Map<String, Object>)object;
    if (StringUtils.isEmpty(remainingPath)) {
      return childMap;
    }
    else {
      return getDeep(childMap, remainingPath);
    }
  }

}
