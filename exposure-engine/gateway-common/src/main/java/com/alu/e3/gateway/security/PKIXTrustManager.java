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
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PKIXTrustManager implements X509TrustManager {
	private static final Logger LOG = LoggerFactory.getLogger(PKIXTrustManager.class);
	
	private CertificateFactory certFactory;
	private Provider provider;
	private PKIXConfigProvider configProvider;
	private boolean allowEmptyClientCert;
	
	public void setProvider(String provider) {
		try {
			certFactory = CertificateFactory.getInstance("X.509", provider);
			this.provider = certFactory.getProvider();
		} catch (Exception e) {
			LOG.error("Could not set provider to '" + provider + "'", e);
		}
	}
	
	public void setConfigProvider(PKIXConfigProvider configProvider) {
		this.configProvider = configProvider;
	}

	public void setAllowEmptyClientCert(boolean allowEmptyClientCert) {
		this.allowEmptyClientCert = allowEmptyClientCert;
	}
	
	public void init() {
		if (certFactory == null) {
			try {
				certFactory = CertificateFactory.getInstance("X.509");
				provider = certFactory.getProvider();
			} catch (CertificateException e) {
				LOG.error("Could not find provider", e);
			}
		}
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (allowEmptyClientCert && ((chain == null) || (chain.length == 0))) {
			return;
		}
		validateCertPath(chain);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		validateCertPath(chain);
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		final PKIXConfig config = configProvider.getConfig();
		try {
			return config.getTrustedCerts().toArray(new X509Certificate[0]);
		} catch (CertStoreException e) {
			LOG.error("Could not retrieve CA certificates", e);
			return new X509Certificate[0];
		}
	}
	
	private void validateCertPath(X509Certificate[] chain) throws CertificateException {
		final PKIXConfig config = configProvider.getConfig();
		try {
			final CertPathValidator validator = CertPathValidator.getInstance("PKIX", provider);
			validator.validate(certFactory.generateCertPath(Arrays.asList(chain)), config.getParameters());
			
		} catch (NoSuchAlgorithmException e) {
			logExcetpion(e, chain, config);
			throw new CertificateException(e);
		} catch (InvalidAlgorithmParameterException e) {
			logExcetpion(e, chain, config);
			throw new CertificateException(e);
		} catch (CertPathValidatorException e) {
			logExcetpion(e, chain, config);
			throw new CertificateException(e);
		}
	}
	
	private static void logExcetpion(Exception e, X509Certificate[] chain, PKIXConfig config) {
		LOG.error("Certificate validation failed", e);
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Provided chain:");
			for (X509Certificate x509Cert : chain) {
				LOG.debug("Subject: " + x509Cert.getSubjectDN() + "\tIssuer: " + x509Cert.getIssuerDN());
			}
			LOG.debug("Accepted issuers:");
			try {
				for (Certificate cert : config.getTrustedCerts()) {
					X509Certificate x509Cert = (X509Certificate) cert;
					LOG.debug("Issuer: " + x509Cert.getSubjectDN());
				}
			} catch (CertStoreException e1) {
				LOG.debug("Could not retrieve CA certificates", e);
			}
		}
	}
}
