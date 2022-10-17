/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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
package io.wcm.devops.conga.plugins.aem.maven.allpackage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * Read and write properties.xml for FileVault package.
 */
class FileVaultProperties {

  private final Properties props;

  /**
   * Read properties from input stream.
   * @param is Input stream
   * @throws IOException I/O exception
   */
  FileVaultProperties(InputStream is) throws IOException {
    props = new Properties();
    props.loadFromXML(is);
  }

  public Properties getProperties() {
    return this.props;
  }

  /**
   * Store properties content to output stream.
   * Ensures consistent line endings are used on all operating systems.
   * @param os Output stream
   * @throws IOException I/O exception
   */
  public void storeToXml(OutputStream os) throws IOException {
    // write properties XML to string
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    props.storeToXML(bos, null);
    String xmlOutput = bos.toString(StandardCharsets.UTF_8.name());

    // normalize line endings with unix line ending
    xmlOutput = StringUtils.replace(xmlOutput, System.lineSeparator(), "\n");

    // output normalized XML
    OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
    writer.write(xmlOutput);
    writer.flush();
  }

}
