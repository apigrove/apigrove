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
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tdr", propOrder = {
    "_static",
    "dynamic"
})
public class TdrData {

    @XmlElement(name = "static")
    protected List<StaticTdr> _static;
    protected List<DynamicTdr> dynamic;
    
    public String toString() {
    	final String comma = ", ";
    	
    	StringBuilder v = new StringBuilder("TDRstatic [");
    	
    	for (StaticTdr stdr : _static)
    	{
    		v.append(stdr).append(comma);
    	}
    	
    	v.append("], TDRdynamic [");

    	for (DynamicTdr dtdr : dynamic)
    	{
    		v.append(dtdr).append(comma);
    	}
    	
    	v.append("]");
    	
    	return v.toString();
    }

    /**
     * Gets the value of the static property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the static property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStatic().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StaticTdr }
     * 
     * 
     */
    public List<StaticTdr> getStatic() {
        if (_static == null) {
            _static = new ArrayList<StaticTdr>();
        }
        return this._static;
    }

    /**
     * Gets the value of the dynamic property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dynamic property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDynamic().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DynamicTdr }
     * 
     * 
     */
    public List<DynamicTdr> getDynamic() {
        if (dynamic == null) {
            dynamic = new ArrayList<DynamicTdr>();
        }
        return this.dynamic;
    }

}
