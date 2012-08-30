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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Defines a bucket of authIds.
 */
@XmlRootElement(name = "quotaRLBucket")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuthIdsNoIdType", propOrder = {
    "authIds"
})
public class AuthIdsType {

    @XmlElement(name = "authId", required = true)
    protected List<String> authIds;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    
    public String toString() {
    	String v = "authIds = " + authIds; //[";
//    	
//    	if(authIds != null)
//    	{
//    		for (String id : authIds) {
//    			v += id + ", ";
//    		}
//    	}
//		v += "]";
//    	
    	return v;
    }

    /**
     * Gets the value of the authIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the authIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAuthIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAuthIds() {
        if (authIds == null) {
            authIds = new ArrayList<String>();
        }
        return this.authIds;
    }
    
    /**
     * Gets the value of the id attribute.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
    	return this.id;
    }
    
    /**
     * Sets the value of the id attribute.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }
    
}
