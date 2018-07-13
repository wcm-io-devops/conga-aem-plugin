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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElement;

public class JsonContentLoaderTest {

  private JsonContentLoader underTest;
  private ContentElement content;

  @BeforeEach
  public void setUp() throws Exception {
    underTest = new JsonContentLoader();
    try (InputStream is = getClass().getResourceAsStream("/json/content.json")) {
      content = underTest.load(is);
    }
  }

  @Test
  public void testPageJcrPrimaryType() {
    assertEquals("cq:Page", content.getProperties().get("jcr:primaryType"));
  }

  @Test
  public void testPageContentProperties() {
    ContentElement element = content.getChild("toolbar/profiles/jcr:content");
    Map<String, Object> props = element.getProperties();
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
    ContentElement element = content.getChild("jcr:content/header");
    assertEquals("/content/dam/sample/header.png", element.getProperties().get("imageReference"));
  }

  @Test
  public void testCalendarEcmaFormat() {
    ContentElement element = content.getChild("jcr:content");

    Calendar calendar = (Calendar)element.getProperties().get("cq:lastModified");
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
    ContentElement element = content.getChild("jcr:content");

    assertEquals("äöüß€", element.getProperties().get("utf8Property"));
  }

}
