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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * The Auth detail used for NB authentication in E3 subsystem.
 * <p>Supported types are: Basic, AuthKey, IP White List
 * 
 */
@XmlRootElement(name="cert")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cert", propOrder = {
    "content",
    "displayName"
})
public class SSLCert {
	
	/**
	 * The key ID. Could be provided.
	 */
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    @XmlAttribute
    protected String id;
    
    @XmlTransient
    protected String keyId;
    
    /**
     * The cert content
     */
    @XmlElement
    protected String content;

    /**
     * The cert content
     */
    @XmlElement
    protected String displayName;

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getKeyId() {
		return keyId;
	}


	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String certContent) {
		this.content = certContent;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	
	public static void main(String[] args) throws Exception{
		JAXBContext context = JAXBContext.newInstance(SSLCert.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		SSLCert cert = new SSLCert();
		
		cert.setContent("abcdefgjik;,mop");
		cert.setId("blah");
		cert.setKeyId("keyidkeyid");
		
		marshaller.marshal(cert, System.out);
	}
    
}
