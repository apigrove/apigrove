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
package com.alu.e3.prov.restapi.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.alu.e3.prov.restapi.util.Base64Adapter;


/**
* This element holds the XSD/WSDL file meta-data: name, content, is main file.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResourceItemType", propOrder = {

})
public class ResourceItem {
	
	/**
	 * The filename of the resource
	 */
    @XmlElement(required = true)
    protected String name;
    /**
     * The content of the xsd/wsdl file 
     */
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(value=Base64Adapter.class)
    protected String grammar;
    /**
     * Specifies if this resource is the main. This done for imports/includes.
     * Only one main resource is allowed.
     */
    protected boolean isMain;
   
    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the grammar property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public String getGrammar() {
        return grammar;
    }

    /**
     * Sets the value of the grammar property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setGrammar(String value) {
        this.grammar = value;
    }

    /**
     * Gets the value of the isMain property.
     * 
     */
    public boolean isIsMain() {
        return isMain;
    }

    /**
     * Sets the value of the isMain property.
     * 
     */
    public void setIsMain(boolean value) {
        this.isMain = value;
    }

}
