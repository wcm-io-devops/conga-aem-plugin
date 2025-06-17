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
package io.wcm.devops.conga.plugins.aem.postprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.xpath.JAXPXPathEngine;
import org.xmlunit.xpath.XPathEngine;
import org.zeroturnaround.zip.ZipUtil;

import io.wcm.tooling.commons.contentpackagebuilder.XmlNamespaces;

public final class ContentPackageTestUtil {

  private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
  private static final XPathEngine XPATH_ENGINE = new JAXPXPathEngine();
  static {
    DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
    XPATH_ENGINE.setNamespaceContext(XmlNamespaces.DEFAULT_NAMESPACES);
  }

  private ContentPackageTestUtil() {
    // static methods only
  }

  public static byte[] getDataFromZip(File file, String path) throws Exception {
    byte[] data = ZipUtil.unpackEntry(file, path);
    if (data == null) {
      throw new FileNotFoundException("File not found in ZIP: " + path);
    }
    return data;
  }

  public static Document getXmlFromZip(File file, String path) throws Exception {
    byte[] data = getDataFromZip(file, path);
    DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
    return documentBuilder.parse(new ByteArrayInputStream(data));
  }

  public static void assertXpathEvaluatesTo(String expected, String xpath, Node node) {
    assertEquals(expected, XPATH_ENGINE.evaluate(xpath, node));
  }

  public static void assertXpathEvaluatesTo(String expected, String xpath, Document doc) throws Exception {
    assertXpathEvaluatesTo(expected, xpath, doc.getDocumentElement());
  }

}
