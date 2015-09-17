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

import io.wcm.devops.conga.generator.GeneratorException;
import io.wcm.devops.conga.generator.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.jcr.JsonItemWriter;

import com.google.common.collect.ImmutableSet;

/**
 * Imports JSON data and binary data into Sling resource hierarchy.
 */
public final class JsonContentLoader {

  private static final String REFERENCE = "jcr:reference:";
  private static final String PATH = "jcr:path:";

  private static final Set<String> IGNORED_NAMES = ImmutableSet.of(
      JcrConstants.JCR_PRIMARYTYPE,
      JcrConstants.JCR_UUID,
      JcrConstants.JCR_BASEVERSION,
      JcrConstants.JCR_PREDECESSORS,
      JcrConstants.JCR_SUCCESSORS,
      JcrConstants.JCR_CREATED,
      JcrConstants.JCR_VERSIONHISTORY,
      "jcr:checkedOut",
      "jcr:isCheckedOut",
      ":jcr:data");

  private final DateFormat calendarFormat;

  /**
   * Constructor
   */
  public JsonContentLoader() {
    this.calendarFormat = new SimpleDateFormat(JsonItemWriter.ECMA_DATE_FORMAT, JsonItemWriter.DATE_FORMAT_LOCALE);
  }

  /**
   * Load a JSON file and transform the contained data structured in nested maps, as supported by the
   * {@link io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder}.
   * @param jsonFile JSON file
   * @return Nested map with content data
   */
  public Map<String, Object> load(File jsonFile) {
    try (InputStream is = new FileInputStream(jsonFile)) {
      return load(is);
    }
    catch (IOException ex) {
      throw new GeneratorException("Unable to parse JSON file: " + FileUtil.getCanonicalPath(jsonFile), ex);
    }
  }

  /**
   * Loads a JSON content and transform the contained data structured in nested maps, as supported by the
   * {@link io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder}.
   * @param inputStream JSON input stream
   * @return Nested map with content data
   * @throws IOException
   */
  public Map<String, Object> load(InputStream inputStream) throws IOException {
    try {
      String jsonString = IOUtils.toString(inputStream, CharEncoding.UTF_8);
      return toMap(new JSONObject(jsonString));
    }
    catch (JSONException ex) {
      throw new IOException(ex.getMessage(), ex);
    }
  }

  private Map<String, Object> toMap(JSONObject jsonObject) throws JSONException {
    Map<String, Object> map = new HashMap<>();

    // collect all properties first
    JSONArray names = jsonObject.names();
    for (int i = 0; names != null && i < names.length(); i++) {
      final String name = names.getString(i);
      if (!IGNORED_NAMES.contains(name)) {
        Object obj = jsonObject.get(name);
        if (!(obj instanceof JSONObject)) {
          setProperty(map, name, obj);
        }
      }
    }

    // validate JCR primary type
    Object primaryTypeObj = jsonObject.opt(JcrConstants.JCR_PRIMARYTYPE);
    String primaryType = null;
    if (primaryTypeObj != null) {
      primaryType = String.valueOf(primaryTypeObj);
    }
    if (primaryType == null) {
      primaryType = JcrConstants.NT_UNSTRUCTURED;
    }
    map.put(JcrConstants.JCR_PRIMARYTYPE, primaryType);

    // add child resources
    for (int i = 0; names != null && i < names.length(); i++) {
      final String name = names.getString(i);
      if (!IGNORED_NAMES.contains(name)) {
        Object obj = jsonObject.get(name);
        if (obj instanceof JSONObject) {
          map.put(name, toMap((JSONObject)obj));
        }
      }
    }

    return map;
  }

  private void setProperty(Map<String, Object> props, String name, Object value) throws JSONException {
    if (value instanceof JSONArray) {

      // multivalue
      final JSONArray array = (JSONArray)value;
      if (array.length() > 0) {
        final Object[] values = new Object[array.length()];
        for (int i = 0; i < array.length(); i++) {
          values[i] = array.get(i);
        }

        if (values[0] instanceof Double || values[0] instanceof Float) {
          Double[] arrayValues = new Double[values.length];
          for (int i = 0; i < values.length; i++) {
            arrayValues[i] = (Double)values[i];
          }
          props.put(cleanupJsonName(name), arrayValues);
        }
        else if (values[0] instanceof Number) {
          Long[] arrayValues = new Long[values.length];
          for (int i = 0; i < values.length; i++) {
            arrayValues[i] = ((Number)values[i]).longValue();
          }
          props.put(cleanupJsonName(name), arrayValues);
        }
        else if (values[0] instanceof Boolean) {
          Boolean[] arrayValues = new Boolean[values.length];
          for (int i = 0; i < values.length; i++) {
            arrayValues[i] = (Boolean)values[i];
          }
          props.put(cleanupJsonName(name), arrayValues);
        }
        else {
          String[] arrayValues = new String[values.length];
          for (int i = 0; i < values.length; i++) {
            arrayValues[i] = values[i].toString();
          }
          props.put(cleanupJsonName(name), arrayValues);
        }
      }
      else {
        props.put(cleanupJsonName(name), new String[0]);
      }

    }
    else {

      // single value
      if (value instanceof Double || value instanceof Float) {
        props.put(cleanupJsonName(name), value);
      }
      else if (value instanceof Number) {
        props.put(cleanupJsonName(name), ((Number)value).longValue());
      }
      else if (value instanceof Boolean) {
        props.put(cleanupJsonName(name), value);
      } else {
        String stringValue = value.toString();

        // check if value is a Calendar object
        Calendar calendar = tryParseCalendarValue(stringValue);
        if (calendar != null) {
          props.put(cleanupJsonName(name), calendar);
        } else {
          props.put(cleanupJsonName(name), stringValue);
        }

      }
    }
  }

  private String cleanupJsonName(String name) {
    if (name.startsWith(REFERENCE)) {
      return name.substring(REFERENCE.length());
    }
    if (name.startsWith(PATH)) {
      return name.substring(PATH.length());
    }
    return name;
  }

  private Calendar tryParseCalendarValue(String value) {
    if (StringUtils.isNotBlank(value)) {
      synchronized (calendarFormat) {
        try {
          Date date = calendarFormat.parse(value);
          Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          return calendar;
        } catch (ParseException ex) {
          // ignore
        }
      }
    }
    return null;
  }

}
