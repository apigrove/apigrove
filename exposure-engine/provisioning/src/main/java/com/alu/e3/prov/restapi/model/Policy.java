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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Defines a policy.
 */
@XmlRootElement(name = "policy")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolicyDataType", propOrder = {
	"id",
    "apiIds",
    "authIds",
    "contexts",
    "tdrOnLimitReached",
    "tdr",
    "properties",
    "headerTransformations"
})
public class Policy {

    protected String id;
    @XmlElementWrapper(name="apiIds")
    @XmlElement(name="apiId")
    protected List<String> apiIds;
    @XmlElementWrapper(name="authIds")
    @XmlElement(name="quotaRLBucket", required = true)
    protected List<AuthIdsType> authIds;
    @XmlElement(name="context", required = true)
    @XmlElementWrapper(name="contexts")
    protected List<Context> contexts;
    protected TypeAttribute tdrOnLimitReached;
    protected TdrData tdr;
    @XmlElementWrapper(name="properties")
    @XmlElement(name="property")
    protected List<Key> properties;
    @XmlElementWrapper(name="headerTransformations", required = false)
    @XmlElement(name="headerTransformation", required = false)
    protected List<HeaderTransformation> headerTransformations;
    
    public String toString() {
    	final String comma = ", ";
    	final String openingBracket = "[";
    	final String closingBracket = "], ";

    	StringBuilder v = new StringBuilder ("id = " + id + ", apiIds = [");
    	
    	if (apiIds != null)
    	{
	    	for (String id : apiIds) {
	    		v.append(id).append(", ");
	    	}
    	}
    	
    	v.append("], quotaRLBuckets = [");
    	
    	if (authIds != null)
    	{
	    	for (AuthIdsType id : authIds) {
	    		v.append(id).append(comma);
	    	}
    	}
    	
    	v.append("], env = [");
    	
    	if (contexts != null)
    	{
	    	for (Context ctx : contexts) {
	    		v.append(openingBracket).append(ctx).append(closingBracket);
	    	}
    	}
    	
    	v.append("], tdr = ").append(tdr);
    	
    	return v.toString();
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the apiIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the apiIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApiIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getApiIds() {
        if (apiIds == null) {
            apiIds = new ArrayList<String>();
        }
        return this.apiIds;
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
     * {@link AuthIdsType }
     * 
     * 
     */
    public List<AuthIdsType> getAuthIds() {
        if (authIds == null) {
        	authIds = new ArrayList<AuthIdsType>();
        }
        return this.authIds;
    }

    /**
     * Gets the value of the contexts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contexts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContexts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Context }
     * 
     * 
     */
    public List<Context> getContexts() {
        if (contexts == null) {
        	contexts = new ArrayList<Context>();
        }
        return this.contexts;
    }
    
    /**
     * Gets the value of the tdrOnLimitReached property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTdrOnLimitReached() {
    	if (tdrOnLimitReached == null)
    		return "";
        return tdrOnLimitReached.getType();
    }

    /**
     * Sets the value of the tdrOnLimitReached property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTdrOnLimitReached(String value) {
    	if (tdrOnLimitReached == null)
    		tdrOnLimitReached = new TypeAttribute();
    	tdrOnLimitReached.setType(value);
    }

    /**
     * 
     * @return
     */
    public TdrData getTdr() {
		return tdr;
	}
    
    /**
     * 
     * @param tdrData
     */
    public void setTdr(TdrData tdr) {
		this.tdr = tdr;
	}
    
    /**
     * 
     * @return
     */
	public List<Key> getProperties(){
		if(this.properties == null)
			this.properties = new ArrayList<Key>();
		return this.properties;
	}
	
    /**
     * 
     * @param value
     */
	public void setProperties(List<Key> value){
		this.properties = value;
	}

	public List<HeaderTransformation> getHeaderTransformations(){
		if(this.headerTransformations == null)
			this.headerTransformations = new ArrayList<HeaderTransformation>();
		return this.headerTransformations;
	}
	
	public void setHeaderTransformations(List<HeaderTransformation> value){
		this.headerTransformations = value;
	}
}
