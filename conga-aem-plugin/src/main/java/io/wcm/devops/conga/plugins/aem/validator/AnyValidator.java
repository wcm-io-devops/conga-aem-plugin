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

import io.wcm.devops.conga.generator.spi.ValidationException;
import io.wcm.devops.conga.generator.spi.ValidatorPlugin;
import io.wcm.devops.conga.generator.spi.context.FileContext;
import io.wcm.devops.conga.generator.spi.context.ValidatorContext;
import io.wcm.devops.conga.generator.util.FileUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.day.any.BaseHandler;
import com.day.any.Parser;
import com.day.any.ResourceExpander;

/**
 * Validates Day ANY files.
 */
public class AnyValidator implements ValidatorPlugin {

  /**
   * Plugin name
   */
  public static final String NAME = "any";

  private static final String FILE_EXTENSION = "any";

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

    try (InputStream is = new FileInputStream(file.getFile());
        Reader reader = new InputStreamReader(is, file.getCharset())) {
      parser.parse(new InputSource(reader));
    }
    catch (Throwable ex) {
      throw new ValidationException("ANY file is not valid: " + ex.getMessage(), ex);
    }
    return null;
  }

}
