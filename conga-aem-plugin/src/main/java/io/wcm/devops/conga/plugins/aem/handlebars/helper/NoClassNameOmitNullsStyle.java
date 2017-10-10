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

import org.apache.commons.lang3.builder.ToStringStyle;

final class NoClassNameOmitNullsStyle extends ToStringStyle {
  private static final long serialVersionUID = 1L;

  /**
   * Compact ToString style without class name ignoring null values.
   */
  public static final ToStringStyle TOSTRING_STYLE = new NoClassNameOmitNullsStyle();

  private NoClassNameOmitNullsStyle() {
    this.setUseClassName(false);
    this.setUseIdentityHashCode(false);
    this.setContentStart("");
    this.setContentEnd("");
    this.setFieldSeparator(", ");
  }

  @Override
  public void append(final StringBuffer buffer, final String fieldName, final Object value, final Boolean fullDetail) {
    // omit null values
    if (value != null) {
      super.append(buffer, fieldName, value, fullDetail);
    }
  }

}
