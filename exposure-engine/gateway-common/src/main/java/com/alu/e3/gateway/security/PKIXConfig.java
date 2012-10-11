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
package com.alu.e3.gateway.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.CRL;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PKIXConfig {
	private final Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
	private final CertStore trustStore;
	private final boolean revocationEnabled;
	
	public PKIXConfig(Collection<? extends X509Certificate> trustedCerts,
			Collection<? extends CRL> revocationLists, Provider provider)
			throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		Set<Object> allCerts = new HashSet<Object>();
		if ((trustedCerts != null) && (trustedCerts.size() > 0)) {
			allCerts.addAll(trustedCerts);
			for (X509Certificate trustedCert : trustedCerts) {
				trustAnchors.add(new TrustAnchor(trustedCert, null));
			}
		}
		if ((revocationLists != null) && (revocationLists.size() > 0)) {
			allCerts.addAll(revocationLists);
			revocationEnabled = true;
		} else {
			revocationEnabled = false;
		}
		
		if (provider != null) {
			trustStore = CertStore.getInstance("Collection",
					new CollectionCertStoreParameters(allCerts), provider);
		} else {
			trustStore = CertStore.getInstance("Collection",
					new CollectionCertStoreParameters(allCerts));
		}
	}
	
	public PKIXParameters getParameters() throws InvalidAlgorithmParameterException	{
		final PKIXParameters params = new PKIXParameters(trustAnchors);
		params.addCertStore(trustStore);
		params.setRevocationEnabled(revocationEnabled);
		return params;
	}
	
	public Collection<? extends Certificate> getTrustedCerts() throws CertStoreException {
		return trustStore.getCertificates(null);
	}
	
	public Collection<? extends CRL> getRevocationLists() throws CertStoreException {
		return trustStore.getCRLs(null);
	}
}