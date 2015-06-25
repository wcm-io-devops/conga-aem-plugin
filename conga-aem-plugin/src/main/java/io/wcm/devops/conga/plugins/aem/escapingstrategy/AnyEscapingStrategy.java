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
package io.wcm.devops.conga.plugins.aem.escapingstrategy;

import io.wcm.devops.conga.generator.spi.EscapingStrategyPlugin;
import io.wcm.devops.conga.generator.util.FileUtil;

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;

/**
 * Escapes for ANY files.
 */
public class AnyEscapingStrategy implements EscapingStrategyPlugin {

  /**
   * Plugin name
   */
  public static final String NAME = "any";

  private static final String FILE_EXTENSION = "any";

  /**
   * Defines translations for strings in ANY files.
   */
  private static final CharSequenceTranslator ESCAPE_ANY =
      new AggregateTranslator(
          new LookupTranslator(
              new String[][] {
                  {
                    "\"", "\\\""
                  },
                  {
                    "\\", "\\\\"
                  }
              })
          );

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean accepts(String fileExtension) {
    return FileUtil.matchesExtension(fileExtension, FILE_EXTENSION);
  }

  @Override
  public String escape(CharSequence value) {
    return value == null ? null : ESCAPE_ANY.translate(value);
  }

}
