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
package io.wcm.devops.conga.plugins.aem.validator;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.day.any.BaseHandler;
import com.day.any.Parser;
import com.day.any.ResourceExpander;

import io.wcm.devops.conga.generator.spi.ValidationException;
import io.wcm.devops.conga.generator.spi.ValidatorPlugin;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.ValidatorContext;
import io.wcm.devops.conga.generator.util.FileUtil;

/**
 * Validates Day ANY files.
 */
public class AnyValidator implements ValidatorPlugin {

  /**
   * Plugin name
   */
  public static final String NAME = "any";

  private static final String FILE_EXTENSION = "any";

  private static final Pattern TICK_PROPERTY = Pattern.compile("/(\\S+)\\s+'([^']*)'");

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean accepts(FileContext file, ValidatorContext context) {
    return FileUtil.matchesExtension(file, FILE_EXTENSION);
  }

  @Override
  public Void apply(FileContext file, ValidatorContext context) throws ValidationException {
    Parser parser = new Parser(new BaseHandler());

    // set resource expander and entity resolver that do not resolve anything
    // just make sure they are in place to allow any parser parsing files with include directives
    parser.setResourceExpander(new ResourceExpander() {
      @Override
      public String[] expand(String arg0) {
        return new String[0];
      }
    });
    parser.setEnitiyResolver(new EntityResolver() {
      @Override
      public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return null;
      }
    });

    // read any file
    try {
      String anyFileContent = FileUtils.readFileToString(file.getFile(), file.getCharset());
      anyFileContent = replaceTicks(anyFileContent);
      try (Reader reader = new StringReader(anyFileContent)) {
        parser.parse(new InputSource(reader));
      }
    }
    /*CHECKSTYLE:OFF*/ catch (Exception ex) { /*CHECKSTYLE:ON*/
      throw new ValidationException("ANY file is not valid: " + ex.getMessage(), ex);
    }
    return null;
  }

  /**
   * Replace ticks (') for properties with quotes (") because the old java ANY file parser implementation
   * does not support them.
   * @param anyFileContent Any file content
   * @return Content with ticks replaces
   */
  static String replaceTicks(String anyFileContent) {
    StringBuffer result = new StringBuffer();
    Matcher matcher = TICK_PROPERTY.matcher(anyFileContent);
    while (matcher.find()) {
      matcher.appendReplacement(result, "/" + Matcher.quoteReplacement(matcher.group(1)) + " \"" + Matcher.quoteReplacement(matcher.group(2)) + "\"");
    }
    matcher.appendTail(result);
    return result.toString();
  }

}
