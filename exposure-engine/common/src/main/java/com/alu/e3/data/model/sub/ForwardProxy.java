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
package com.alu.e3.data.model.sub;

import java.io.Serializable;

public class ForwardProxy implements Serializable, IForwardProxy {
	
	private static final long serialVersionUID = 6290190762467317009L;
	
    private String proxyHost;
    private String proxyPort;
    private String proxyUser;
    private String proxyPass;

    /* (non-Javadoc)
	 * @see com.alu.e3.data.model.sub.IForwardProxy#getProxyHost()
	 */
    @Override
	public String getProxyHost() {
        return proxyHost;
    }
    public void setProxyHost(String value) {
        this.proxyHost = value;
    }

    /* (non-Javadoc)
	 * @see com.alu.e3.data.model.sub.IForwardProxy#getProxyPort()
	 */
    @Override
	public String getProxyPort() {
        return proxyPort;
    }
    public void setProxyPort(String value) {
        this.proxyPort = value;
    }
   
    /* (non-Javadoc)
	 * @see com.alu.e3.data.model.sub.IForwardProxy#getProxyUser()
	 */
    @Override
	public String getProxyUser() {
        return proxyUser;
    }
    public void setProxyUser(String value) {
        this.proxyUser = value;
    }
    
    /* (non-Javadoc)
	 * @see com.alu.e3.data.model.sub.IForwardProxy#getProxyPass()
	 */
    @Override
	public String getProxyPass() {
        return proxyPass;
    }
    public void setProxyPass(String value) {
        this.proxyPass = value;
    }
    
    public String serialize() {
    	return proxyHost + "#" + proxyPort + "#" + proxyUser + "#" + proxyPass;
    }
    public static ForwardProxy deserialize(String s) {
    	String[] res = s.split("#", 4);
    	if (res == null)
    		return null;
    	
    	if (res.length < 4)
    		return null;
    	
    	ForwardProxy f = new ForwardProxy();
    	f.setProxyHost(res[0]);
    	f.setProxyPort(res[1]);
    	f.setProxyUser(res[2]);
    	f.setProxyPass(res[3]);
    	
    	return f;
    }
}
