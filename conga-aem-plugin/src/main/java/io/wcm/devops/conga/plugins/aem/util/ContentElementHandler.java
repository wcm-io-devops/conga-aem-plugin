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
package io.wcm.devops.conga.plugins.aem.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.sling.contentparser.api.ContentHandler;

import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElement;
import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElementImpl;

/**
 * {@link ContentHandler} implementation that produces a tree of {@link ContentElement} items.
 */
final class ContentElementHandler implements ContentHandler {

  private ContentElement root;

  @SuppressWarnings("java:S5998") // analyzed paths should be safe
  private static final Pattern PATH_PATTERN = Pattern.compile("^((/[^/]+)*)(/([^/]+))$");

  @Override
  public void resource(String path, Map<String, Object> properties) {
    if (Strings.CS.equals(path, "/")) {
      root = new ContentElementImpl(null, properties);
    }
    else {
      if (root == null) {
        throw new IllegalArgumentException("Root resource not set.");
      }
      Matcher matcher = PATH_PATTERN.matcher(path);
      if (!matcher.matches()) {
        throw new IllegalArgumentException("Unexpected path:" + path);
      }
      String relativeParentPath = StringUtils.stripStart(matcher.group(1), "/");
      String name = matcher.group(4);
      ContentElement parent;
      if (StringUtils.isEmpty(relativeParentPath)) {
        parent = root;
      }
      else {
        parent = root.getChild(relativeParentPath);
      }
      if (parent == null) {
        throw new IllegalArgumentException("Parent '" + relativeParentPath + "' does not exist.");
      }
      parent.getChildren().put(name, new ContentElementImpl(name, properties));
    }
  }

  public ContentElement getRoot() {
    return root;
  }

}
