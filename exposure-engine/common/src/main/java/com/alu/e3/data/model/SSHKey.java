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
package com.alu.e3.data.model;

import java.io.Serializable;

/**
 * SSHKey class Defines a SSH key
 * 
 */
public class SSHKey implements Serializable {
	
	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 6230964622405375075L;
	
	/* members. */
	private String m_strName;
	private byte[] m_bytePrivateKey;
	private byte[] m_bytePublicKey;

	/**
	 * Constructor
	 * 
	 * @param strType
	 * @param strName
	 * @param strPrivateKeyPath
	 * @param strPublicKeyPath
	 */
	public SSHKey(String strName, byte[] bytePrivateKeyPath,
			byte[] bytePublicKeyPath) {
		this.m_strName = strName;
		this.m_bytePrivateKey = bytePrivateKeyPath;
		this.m_bytePublicKey = bytePublicKeyPath;
	}

	/**
	 * Constructor
	 * 
	 * @param strName
	 */
	public SSHKey(String strName) {
		m_strName = strName;
	}

	public String getName() {
		return m_strName;
	}

	public void setName(String strName) {
		this.m_strName = strName;
	}

	public byte[] getPrivateKey() {
		return m_bytePrivateKey;
	}

	public void setPrivateKey(byte[] bytePrivateKeyPath) {
		this.m_bytePrivateKey = bytePrivateKeyPath;
	}

	public byte[] getPublicKey() {
		return m_bytePublicKey;
	}

	public void setPublicKey(byte[] bytePublicKeyPath) {
		this.m_bytePublicKey = bytePublicKeyPath;
	}

	public String toString() {
		return "[" + this.getClass().getSimpleName() + ": " + m_strName
				+ "\tpubKeyLen(" + m_bytePublicKey.length + ")\tprivKeyLen("
				+ (m_bytePrivateKey == null ? 0 : m_bytePrivateKey.length) + ")]";
	}

	public boolean isSameKey(SSHKey that) {
		if ( (this.m_bytePrivateKey == null && that.m_bytePrivateKey != null) || (this.m_bytePrivateKey != null && that.m_bytePrivateKey == null)) {
			return false;
		}
			
		if ((this.m_bytePrivateKey != null && that.m_bytePrivateKey != null)) {
			if (this.m_bytePrivateKey.length != that.m_bytePrivateKey.length)
				return false;
			for (int i = 0; i < this.m_bytePrivateKey.length; i++) {
				if (this.m_bytePrivateKey[i] != that.m_bytePrivateKey[i])
					return false;
			}
		}
		if (this.m_bytePublicKey.length != that.m_bytePublicKey.length)
			return false;
		for (int i = 0; i < this.m_bytePublicKey.length; i++) {
			if (this.m_bytePublicKey[i] != that.m_bytePublicKey[i])
				return false;
		}
		return true;
	}
}
