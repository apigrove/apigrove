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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * The Auth detail used for NB authentication in E3 subsystem.
 * <p>Supported types are: Basic, AuthKey, IP White List
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "auth", propOrder = {
		"id",
		"status",
		"type",
		"policyContext",
		"apiContext",
		"basicAuth",
		"oAuth",
		"authKeyAuth",
		"ipWhiteListAuth",
		"wsseAuth",
		"tdr",
		"properties",
		"headerTransformations"
})
public class Auth {
	
	/**
	 * The auth ID. Could be provided.
	 */
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    @XmlSchemaType(name = "ID")
    @XmlElement(required = false)
    protected String id;
    /**
     * The auth status: active, inactive, pending
     */
    @XmlElement(required = true)
    protected Status status;
    
    /**
     * The auth type: basic, authKey, IP white list
     */
    @XmlElement(required = true)
    protected AuthType type;
    
    /**
     * The policy context for which this Auth is applicable.
     */
    @XmlElement(required = true)
    protected IdAttribute policyContext;
    
    /**
     * The api context for which this Auth is applicable.
     */
    @XmlElement(required = true)
    protected IdAttribute apiContext;
    
    /**
     * The Basic Auth details if basic is selected as a type.
     */
    @XmlElement(required = true)
    protected BasicAuth basicAuth;
    
    /**
     * The OAuth details if oauth is selected as a type.
     */
    @XmlElement(required = true)
    protected OAuth oAuth;
    
    /**
     * The authKey details if authKey selected as a type
     */
    @XmlElement(required = true)
    protected AuthKeyAuth authKeyAuth;
    
    /**
     * The IP white list if this auth is selected as a type.
     */
    @XmlElement(required = true)
    protected IpWhiteList ipWhiteListAuth;
    
    /**
	 * The IP white list if this auth is selected as a type.
	 */
	@XmlElement(required = true)
	protected WSSEAuth wsseAuth;

	/**
     * TDR data that will be appended at TDR generation time.
     */
    protected TdrData tdr;
    
    /**
     * properties data that will be used by header transforms and/or tdr outputs.
     */
    @XmlElementWrapper(name="properties")
    @XmlElement(name="property")
    protected List<Key> properties;
    
    /**
     * The header transformations for the API.
     */
    @XmlElementWrapper(name="headerTransformations", required = false)
    @XmlElement(name="headerTransformation", required = false)
    protected List<HeaderTransformation> headerTransformations;
    
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
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setStatus(Status value) {
        this.status = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link AuthType }
     *     
     */
    public AuthType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthType }
     *     
     */
    public void setType(AuthType value) {
        this.type = value;
    }

    /**
     * Gets the value of the apiContext property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApiContext() {
    	if (apiContext == null)
    		return "";
        return apiContext.getId();
    }

    /**
     * Sets the value of the apiContext property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApiContext(String value) {
    	if (apiContext == null)
    		apiContext = new IdAttribute();
    	apiContext.setId(value);
    }

    /**
     * Gets the value of the policyContext property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPolicyContext() {
    	if (policyContext == null)
    		return "";
        return policyContext.getId();
    }

    /**
     * Sets the value of the policyContext property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPolicyContext(String value) {
    	if (policyContext == null)
    		policyContext = new IdAttribute();
    	policyContext.setId(value);
    }

    /**
     * Gets the value of the basicAuth property.
     * 
     * @return
     *     possible object is
     *     {@link BasicAuth }
     *     
     */
    public BasicAuth getBasicAuth() {
        return basicAuth;
    }

    /**
     * Sets the value of the basicAuth property.
     * 
     * @param value
     *     allowed object is
     *     {@link BasicAuth }
     *     
     */
    public void setBasicAuth(BasicAuth value) {
        this.basicAuth = value;
    }

    /**
     * Gets the value of the oAuth property.
     * 
     * @return
     *     possible object is
     *     {@link OAuth }
     *     
     */
    public OAuth getOAuth() {
        return oAuth;
    }

    /**
     * Sets the value of the oAuth property.
     * 
     * @param value
     *     allowed object is
     *     {@link OAuth }
     *     
     */
    public void setOAuth(OAuth value) {
        this.oAuth = value;
    }

    /**
     * Gets the value of the authKeyAuth property.
     * 
     * @return
     *     possible object is
     *     {@link AuthKeyAuth }
     *     
     */
    public AuthKeyAuth getAuthKeyAuth() {
        return authKeyAuth;
    }

    /**
     * Sets the value of the authKeyAuth property.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthKeyAuth }
     *     
     */
    public void setAuthKeyAuth(AuthKeyAuth value) {
        this.authKeyAuth = value;
    }

    /**
     * Gets the value of the ipWhiteListAuth property.
     * 
     * @return
     *     possible object is
     *     {@link IpWhiteList }
     *     
     */
    public IpWhiteList getIpWhiteListAuth() {
        return ipWhiteListAuth;
    }

    /**
     * Sets the value of the ipWhiteListAuth property.
     * 
     * @param value
     *     allowed object is
     *     {@link IpWhiteList }
     *     
     */
    public void setIpWhiteListAuth(IpWhiteList value) {
        this.ipWhiteListAuth = value;
    }    
    
    /**
     * Gets tdr property.
     * @return
     */
	public TdrData getTdr() {
		return tdr;
	}

	/**
	 * Sets the value of the tdr property.
	 * @param tdr
	 */
	public void setTdr(TdrData tdr) {
		this.tdr = tdr;
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "idAttribute")
	public static class IdAttribute
	{
		@XmlAttribute(name="id", required = true)
	    protected String id;
		
		public void setId(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	}
	

	public List<Key> getProperties(){
		if(this.properties == null)
			this.properties = new ArrayList<Key>();
		return this.properties;
	}
	
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

	public WSSEAuth getWsseAuth() {
		return wsseAuth;
	}

	public void setWsseAuth(WSSEAuth wsseAuth) {
		this.wsseAuth = wsseAuth;
	}
	
	
}
