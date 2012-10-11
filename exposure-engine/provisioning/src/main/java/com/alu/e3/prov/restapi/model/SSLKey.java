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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * The SSLKey object
 * 
 */
@XmlRootElement(name="key")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "key", propOrder = {  
		"activeCertId",
		"keyPassphrase",
		"content",
		"displayName",
		"type"
		})
public class SSLKey {
	
	/**
	 * The key ID. Could be provided.
	 */
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    @XmlAttribute
    protected String id;
    
    /**
     * The key's currently active certificate
     */
    @XmlElement
    protected String activeCertId;
    
    /**
     * The key's passphrase
     */
    @XmlElement
    protected String keyPassphrase;
    
    /**
     * The key content
     */
    @XmlElement
    protected String content;
    
    /**
     * The key's  display name
     */
    @XmlElement
    protected String displayName;
    
    /**
     * The key type. This is essentially metadata and not really useful
     */
    @XmlElement
    protected String type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String keyContent) {
		this.content = keyContent;
	}
	
	public static void main(String [] args) throws Exception{
		JAXBContext context = JAXBContext.newInstance(SSLKey.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		SSLKey key = new SSLKey();
		key.setId("1234");
		key.setContent("asdfasdfasdfasdfasdf");
		key.setActiveCertId("1234");
		
		marshaller.marshal(key, System.out);
	}

	public String getActiveCertId() {
		return activeCertId;
	}

	public void setActiveCertId(String activeCertId) {
		this.activeCertId = activeCertId;
	}
	
	public void setKeyPassphrase(String keyPassphrase) {
		this.keyPassphrase = keyPassphrase;
	}
	
	public String getKeyPassphrase() {
		return keyPassphrase;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayname) {
		this.displayName = displayname;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
