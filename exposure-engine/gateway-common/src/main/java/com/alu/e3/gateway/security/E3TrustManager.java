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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.CRL;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.X509TrustManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class E3TrustManager implements X509TrustManager {
	private static final Logger LOG = LoggerFactory.getLogger(E3TrustManager.class);
	
	private static final Provider PROVIDER = new BouncyCastleProvider();
	private static final Map<String, CRL> CRL_MAP = new HashMap<String, CRL>();
	private static volatile CertStore crlStore = null;
	
	private final CertificateFactory certFactory;
	private final Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
	private final CertStore certStore;
	
	public E3TrustManager(KeyStore trustStore) throws CertificateException, KeyStoreException,
			InvalidAlgorithmParameterException, NoSuchAlgorithmException
	{
		certFactory = CertificateFactory.getInstance("X.509", PROVIDER);
		
		Set<X509Certificate> allX509Certs = new HashSet<X509Certificate>();
		Enumeration<String> aliases = trustStore.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			if (!trustStore.isCertificateEntry(alias)) {
				continue;
			}
			Certificate cert = trustStore.getCertificate(alias);
			if (!(cert instanceof X509Certificate)) {
				continue;
			}
			
			X509Certificate x509Cert = (X509Certificate) cert;
			allX509Certs.add(x509Cert);
			trustAnchors.add(new TrustAnchor(x509Cert, null));
		}
		
		certStore = CertStore.getInstance("Collection",
				new CollectionCertStoreParameters(allX509Certs), PROVIDER);
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if ((chain == null) || (chain.length == 0)) {
			// No certificate provided by the client,
			// it will be handled later by ClientCertificateValidator
			return;
		}
		
		try {
			// PKIXParameters is not thread-safe
			final PKIXParameters params = new PKIXParameters(trustAnchors);
			params.addCertStore(certStore);
			
			// Keep the local copy of crlStore, as the class attribute may change.
			final CertStore crlStore = E3TrustManager.crlStore;
			if (crlStore != null) {
				params.addCertStore(crlStore);
				params.setRevocationEnabled(true);
			} else {
				params.setRevocationEnabled(false);
			}
			
			// CertPathValidator is not thread-safe
			final CertPathValidator validator = CertPathValidator.getInstance("PKIX", PROVIDER);
			validator.validate(certFactory.generateCertPath(Arrays.asList(chain)), params);
			
		} catch (InvalidAlgorithmParameterException e) {
			logExcetpion(e, chain);
			throw new CertificateException(e);
		} catch (NoSuchAlgorithmException e) {
			logExcetpion(e, chain);
			throw new CertificateException(e);
		} catch (CertPathValidatorException e) {
			logExcetpion(e, chain);
			throw new CertificateException(e);
		}
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// Not needed now
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		try {
			return certStore.getCertificates(null).toArray(new X509Certificate[0]);
		} catch (CertStoreException e) {
			LOG.error("Could not retrieve CA certificates", e);
			return new X509Certificate[0];
		}
	}

	public static void addCRL(String id, CRL crl)
		throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		synchronized (CRL_MAP) {
			CRL_MAP.put(id, crl);
			crlStore = CertStore.getInstance("Collection",
					new CollectionCertStoreParameters(CRL_MAP.values()), PROVIDER);
		}
	}
	
	public static void removeCRL(String id)
			throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		synchronized (CRL_MAP) {
			CRL_MAP.remove(id);
			if (CRL_MAP.isEmpty()) {
				crlStore = null;
			} else {
				crlStore = CertStore.getInstance("Collection",
						new CollectionCertStoreParameters(CRL_MAP.values()), PROVIDER);
			}
		}
	}
	
	private void logExcetpion(Exception e, X509Certificate[] chain) {
		LOG.error("Certificate validation failed", e);
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Provided chain:");
			for (X509Certificate x509Cert : chain) {
				LOG.debug("Subject: " + x509Cert.getSubjectDN() + "\tIssuer: " + x509Cert.getIssuerDN());
			}
			LOG.debug("Accepted issuers:");
			try {
				for (Certificate cert : certStore.getCertificates(null)) {
					X509Certificate x509Cert = (X509Certificate) cert;
					LOG.debug("Issuer: " + x509Cert.getSubjectDN());
				}
			} catch (CertStoreException e1) {
				LOG.debug("Could not retrieve CA certificates", e);
			}
		}
	}
}
