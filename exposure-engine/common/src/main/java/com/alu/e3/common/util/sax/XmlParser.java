/**
 * Copyright © 2012 Alcatel-Lucent.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * Licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alu.e3.common.util.sax;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.Stack;

public class XmlParser extends DefaultHandler {

  private SAXParserFactory factory = SAXParserFactory.newInstance();
  private Stack<XmlNode> nodesStacks = new Stack<XmlNode>();

  public void parse(Reader xmlIn, XmlNode rootNode) throws Exception {
    try {
      nodesStacks.clear();

      XMLReader reader = null;
      reader = factory.newSAXParser().getXMLReader();
      reader.setContentHandler(this);

      nodesStacks.add(rootNode);

      // parse the xml
      reader.parse(new InputSource(new BufferedReader(xmlIn)));

    } finally {
      xmlIn.close();
    }
  }

  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
          throws SAXException {
    XmlNode currentNode = nodesStacks.peek();
    XmlNode childNode = null;
    childNode = currentNode.createSubNode(qName, attributes);
    nodesStacks.push(childNode);
  }

  public void endElement(String uri, String localName, String qName)
          throws SAXException {
    XmlNode currentNode = nodesStacks.pop();
    currentNode.endElement();
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    XmlNode currentNode = nodesStacks.peek();
    currentNode.characters(ch, start, length);
  }

}
