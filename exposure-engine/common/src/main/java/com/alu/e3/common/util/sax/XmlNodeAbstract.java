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

public abstract class XmlNodeAbstract implements XmlNode {
  private String tagName;

  protected XmlNodeAbstract(String tagName) {
    this.tagName = tagName;
  }

  public String getTagName() {
    return tagName;
  }

  private StringBuffer charactersValue = new StringBuffer();

  final public void characters(char ch[], int start, int length) {
    String value = new String(ch, start, length);
    value = value.replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", " ");
    charactersValue.append(value);
  }

  protected String getCharacters() {
    return charactersValue.toString();
  }

  public void endElement() {

  }
}