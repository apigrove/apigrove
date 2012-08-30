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
package com.alu.e3.data.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.ApiDetail;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.AuthDetail;
import com.alu.e3.data.model.Certificate;
import com.alu.e3.data.model.CertificateDetail;
import com.alu.e3.data.model.KeyDetail;
import com.alu.e3.data.model.Policy;
import com.alu.e3.data.model.SSLCRL;
import com.alu.e3.data.model.enumeration.ActionType;
import com.alu.e3.data.model.enumeration.ApiNotificationFormat;
import com.alu.e3.data.model.enumeration.ApiSubscriptionStep;
import com.alu.e3.data.model.enumeration.ApiType;
import com.alu.e3.data.model.enumeration.HeaderTransformationAction;
import com.alu.e3.data.model.enumeration.HeaderTransformationType;
import com.alu.e3.data.model.enumeration.LoadBalancingType;
import com.alu.e3.data.model.enumeration.NBAuthType;
import com.alu.e3.data.model.enumeration.SchemaValidationEnum;
import com.alu.e3.data.model.enumeration.SoapVersionEnum;
import com.alu.e3.data.model.enumeration.StatusType;
import com.alu.e3.data.model.enumeration.TLSMode;
import com.alu.e3.data.model.sub.APIContext;
import com.alu.e3.data.model.sub.ConnectionParameters;
import com.alu.e3.data.model.sub.Context;
import com.alu.e3.data.model.sub.Counter;
import com.alu.e3.data.model.sub.FailOver;
import com.alu.e3.data.model.sub.ForwardProxy;
import com.alu.e3.data.model.sub.HTTPSType;
import com.alu.e3.data.model.sub.HeaderTransformation;
import com.alu.e3.data.model.sub.LoadBalancing;
import com.alu.e3.data.model.sub.QuotaRLBucket;
import com.alu.e3.data.model.sub.SBAuthentication;
import com.alu.e3.data.model.sub.TargetHealthCheck;
import com.alu.e3.data.model.sub.TargetHost;
import com.alu.e3.data.model.sub.TdrDynamicRule;
import com.alu.e3.data.model.sub.TdrGenerationRule;
import com.alu.e3.data.model.sub.TdrStaticRule;
import com.alu.e3.prov.restapi.model.ApiProxySettings;
import com.alu.e3.prov.restapi.model.AuthKeyAuth;
import com.alu.e3.prov.restapi.model.AuthType;
import com.alu.e3.prov.restapi.model.Authentication;
import com.alu.e3.prov.restapi.model.Authkey;
import com.alu.e3.prov.restapi.model.BasicAuth;
import com.alu.e3.prov.restapi.model.Data;
import com.alu.e3.prov.restapi.model.DynamicTdr;
import com.alu.e3.prov.restapi.model.IpWhiteListAuth;
import com.alu.e3.prov.restapi.model.Key;
import com.alu.e3.prov.restapi.model.NotificationFormat;
import com.alu.e3.prov.restapi.model.OAuth;
import com.alu.e3.prov.restapi.model.ProvisionAuthentication;
import com.alu.e3.prov.restapi.model.ResourceItem;
import com.alu.e3.prov.restapi.model.SSLCert;
import com.alu.e3.prov.restapi.model.SSLKey;
import com.alu.e3.prov.restapi.model.StaticTdr;
import com.alu.e3.prov.restapi.model.SubscriptionStep;
import com.alu.e3.prov.restapi.model.TdrData;
import com.alu.e3.prov.restapi.model.TdrEnabled;
import com.alu.e3.prov.restapi.model.TdrType;
import com.alu.e3.prov.restapi.model.Validation;
import com.alu.e3.prov.restapi.model.Validation.Schema;
import com.alu.e3.prov.restapi.model.WSSEAuth;

public final class BeanConverterUtil {

	public static final com.alu.e3.prov.restapi.model.Api fromDataModel(Api api) {
		if (api==null) throw new IllegalArgumentException("apiData must not be null");
		if (api.getApiDetail()==null) throw new IllegalArgumentException("apiData.detail must not be null");

		com.alu.e3.prov.restapi.model.Api provisionData = new com.alu.e3.prov.restapi.model.Api();

		provisionData.setId						(api.getId());
		provisionData.setEndpoint				(api.getApiDetail().getEndpoint());
		provisionData.setDisplayName			(api.getApiDetail().getDisplayName());
		provisionData.setVersion				(api.getApiDetail().getVersion());
		provisionData.setTdrEnabled				(fromDataModelToTdr(api.getApiDetail().getTdrEnabled()));
		provisionData.setType					(fromDataModel(api.getApiDetail().getType()));
		provisionData.setSubscriptionStep		(fromDataModel(api.getApiDetail().getSubscriptionStep()));
		provisionData.setNotificationFormat		(fromDataModel(api.getApiDetail().getNotificationFormat()));
		provisionData.getContexts().addAll		(BeanConverterUtil.<com.alu.e3.prov.restapi.model.ApiContext, APIContext>fromDataModels(api.getApiDetail().getContexts()));
		provisionData.setAuthentication			(fromDataModelToProvisionAuthentication(api.getApiDetail()));
		provisionData.setTdr					(fromDataModel(api.getTdrGenerationRule()));		
		provisionData.setTdrOnUse				(api.getTdrOnUse());
		provisionData.setTdrOnLimitReached		(api.getTdrOnLimitReached());
		provisionData.setStatus					(fromDataModel(api.getApiDetail().getStatus()));
		provisionData.setHttps					(fromDataModel(api.getApiDetail().getHttps())); 

		for(String keyname : api.getProperties().keySet()){
			Key k = new Key();
			k.setName(keyname);
			k.setValue(api.getProperties().get(keyname));
			provisionData.getProperties().add(k);
		}

		provisionData.setHeaderTransformations  (BeanConverterUtil.<com.alu.e3.prov.restapi.model.HeaderTransformation,HeaderTransformation>fromDataModels(api.getHeaderTransformations()));

		provisionData.setAllowedHttpMethodsAsString(api.getApiDetail().getAllowedMethods());

		provisionData.setValidation(fromDataModel(api.getValidation()));
		provisionData.setHeaderTransEnabled(api.getHeaderTransEnabled());
		provisionData.setInternal(api.getInternal());
		
		ApiProxySettings proxySettings = null;
		
		if(api.isUseGlobalProxy()){
			proxySettings = new ApiProxySettings();
			proxySettings.setGlobalProxy(new ApiProxySettings.GlobalProxy());
		}else if(api.getLocalProxy() != null){
			proxySettings = new ApiProxySettings();
			proxySettings.setLocalProxy(fromDataModel(api.getLocalProxy()));
		}
		provisionData.setProxySettings(proxySettings);
		
		return provisionData;
	}

	public static final Api toDataModel(com.alu.e3.prov.restapi.model.Api provisionData) {
		if (provisionData==null) throw new IllegalArgumentException("provisionData must not be null");

		Api api = new Api();

		api.setId								(provisionData.getId());
		api.setTdrGenerationRule				(toDataModel(provisionData.getTdr()));
		//Not populated to WS
		//api.setPolicyIds						(null);
		api.setTdrOnUse(provisionData.getTdrOnUse());
		api.setTdrOnLimitReached(provisionData.getTdrOnLimitReached());

		for(Key prop : provisionData.getProperties()){
			api.getProperties().put(prop.getName(), prop.getValue());
		}

		api.setHeaderTransformation             (BeanConverterUtil.<HeaderTransformation, com.alu.e3.prov.restapi.model.HeaderTransformation>toDataModels(provisionData.getHeaderTransformations()));

		ApiDetail apiDetail = new ApiDetail();
		api.setApiDetail						(apiDetail);

		apiDetail.setDisplayName				(provisionData.getDisplayName());
		apiDetail.setEndpoint					(provisionData.getEndpoint());
		apiDetail.setHttps						(toDataModel(provisionData.getHttps()));
		apiDetail.setType						(toDataModel(provisionData.getType()));
		apiDetail.setSubscriptionStep			(toDataModel(provisionData.getSubscriptionStep()));
		apiDetail.setNotificationFormat			(toDataModel(provisionData.getNotificationFormat()));
		apiDetail.setVersion					(provisionData.getVersion());
		apiDetail.getContexts().addAll			(BeanConverterUtil.<APIContext, com.alu.e3.prov.restapi.model.ApiContext>toDataModels(provisionData.getContexts()));
		apiDetail.setTdrEnabled					(toDataModel(provisionData.getTdrEnabled()));
		if (provisionData.getAuthentication() != null && provisionData.getAuthentication().getAuthKey() != null) {
			apiDetail.setAuthKeyName			(provisionData.getAuthentication().getAuthKey().getKeyName());
			apiDetail.setAuthHeaderName			(provisionData.getAuthentication().getAuthKey().getHeaderName());
		}
		if (provisionData.getAuthentication() != null)
			apiDetail.getEnabledAuthType().addAll	(BeanConverterUtil.<NBAuthType, AuthType>toDataModels(provisionData.getAuthentication().getAuths()));

		apiDetail.setStatus						(toDataModel(provisionData.getStatus()));

		// FIXME: The current WS API model does not support notification (true|false) attributes.
		apiDetail.setNotification			(null);

		apiDetail.setAllowedMethods(provisionData.getAllowedHttpMethodsAsString());

		api.setValidation(toDataModel(provisionData.getValidation()));		
		api.setHeaderTransEnabled(provisionData.getHeaderTransEnabled());
		api.setInternal(provisionData.getInternal());
		
		if(provisionData.getProxySettings() != null){
			if(provisionData.getProxySettings().getGlobalProxy() != null){
				api.setUseGlobalProxy(true);
			}else if (provisionData.getProxySettings().getLocalProxy() != null){
				api.setLocalProxy( toDataModel(provisionData.getProxySettings().getLocalProxy()) );
			}// else no proxy
		}
		
		
		return api;
	}

	private static final com.alu.e3.prov.restapi.model.HeaderTransformation fromDataModel(HeaderTransformation ht){
		com.alu.e3.prov.restapi.model.HeaderTransformation xmlht = new com.alu.e3.prov.restapi.model.HeaderTransformation();

		xmlht.setAction(com.alu.e3.prov.restapi.model.HeaderTransformationAction.valueOf(ht.getAction().toString()));
		xmlht.setName(ht.getName());
		xmlht.setProperty(ht.getProperty());
		xmlht.setType(com.alu.e3.prov.restapi.model.HeaderTransformationType.valueOf(ht.getType().toString()));
		xmlht.setValue(ht.getValue());

		return xmlht;
	}

	private static final HeaderTransformation toDataModel(com.alu.e3.prov.restapi.model.HeaderTransformation xmlht){
		HeaderTransformation ht = new HeaderTransformation();

		ht.setAction(HeaderTransformationAction.valueOf(xmlht.getAction().toString()));
		ht.setName(xmlht.getName());
		ht.setProperty(xmlht.getProperty());
		ht.setType(HeaderTransformationType.valueOf(xmlht.getType().toString()));
		ht.setValue(xmlht.getValue());

		return ht;
	}


	public static final com.alu.e3.prov.restapi.model.Auth fromDataModel(Auth auth) {
		if (auth==null) throw new IllegalArgumentException("auth must not be null");

		com.alu.e3.prov.restapi.model.Auth a = new com.alu.e3.prov.restapi.model.Auth();
		a.setId							(auth.getId());
		a.setStatus						(fromDataModel(auth.getAuthDetail().getStatus()));
		a.setApiContext					(auth.getApiContext());
		a.setPolicyContext				(auth.getPolicyContext());
		a.setType						(fromDataModel(auth.getAuthDetail().getType()));
		a.setTdr						(fromDataModel(auth.getTdrGenerationRule()));
		a.setHeaderTransformations  (BeanConverterUtil.<com.alu.e3.prov.restapi.model.HeaderTransformation,HeaderTransformation>fromDataModels(auth.getHeaderTransformations()));

		for(String keyname : auth.getProperties().keySet()){
			Key k = new Key();
			k.setName(keyname);
			k.setValue(auth.getProperties().get(keyname));
			a.getProperties().add(k);
		}

		switch(auth.getAuthDetail().getType()) {
		case AUTHKEY:
			a.setAuthKeyAuth		(fromDataModelToAuthKeyAuth(auth.getAuthDetail()));
			break;
		case BASIC:
			a.setBasicAuth			(fromDataModelToBasicAuth(auth.getAuthDetail()));
			break;
		case IP_WHITE_LIST:
			a.setIpWhiteListAuth	(fromDataModelToIpWhiteListAuth(auth.getAuthDetail()));
			break;
		case NO_AUTH:
			break;
		case OAUTH:
			a.setOAuth(fromDataModelToOAuth(auth.getAuthDetail()));
			break;
		case WSSE:
			a.setWsseAuth(fromDataModelToWsseAuth(auth));
			break;
		default:
			throw new IllegalArgumentException("Unknown authType specified");
		}

		return a;
	}

	public static final Auth toDataModel(com.alu.e3.prov.restapi.model.Auth authData) {
		if (authData==null) throw new IllegalArgumentException("authData must not be null");

		Auth auth = new Auth();	
		auth.setId						(authData.getId());
		auth.setTdrGenerationRule		(toDataModel(authData.getTdr()));
		auth.setApiContext       		(authData.getApiContext());
		auth.setPolicyContext     		(authData.getPolicyContext());
		auth.setHeaderTransformation    (BeanConverterUtil.<HeaderTransformation, com.alu.e3.prov.restapi.model.HeaderTransformation>toDataModels(authData.getHeaderTransformations()));

		for(Key prop : authData.getProperties()){
			auth.getProperties().put(prop.getName(), prop.getValue());
		}

		AuthDetail authDetail = new AuthDetail();
		auth.setAuthDetail				(authDetail);

		authDetail.setType				(toDataModel(authData.getType()));
		authDetail.setStatus			(toDataModel(authData.getStatus()));

		switch(authData.getType()) {
		case AUTHKEY:
			authDetail.setAuthKeyValue(authData.getAuthKeyAuth().getKeyValue());
			break;
		case BASIC:
			authDetail.setUsername(authData.getBasicAuth().getUsername());
			authDetail.setPassword(authData.getBasicAuth().getPassword());
			break;
		case IP_WHITE_LIST:
			authDetail.getWhiteListedIps().addAll(authData.getIpWhiteListAuth().getIp());
			break;
		case NO_AUTH:
			break;
		case OAUTH:
			authDetail.setClientId(authData.getOAuth().getClientId());
			authDetail.setClientSecret(authData.getOAuth().getClientSecret());
			break;
		case WSSE:
			authDetail.setUsername(authData.getWsseAuth().getUsername());
			auth.setPasswordType(authData.getWsseAuth().getPasswordType());
			auth.setWssePassword(authData.getWsseAuth().getPassword());
			break;
		default:
			throw new IllegalArgumentException("Unknown authType specified");
		}

		return auth;
	}

	public static final com.alu.e3.prov.restapi.model.Policy fromDataModel(Policy policy) {
		if (policy==null) throw new IllegalArgumentException("policy must not be null");

		com.alu.e3.prov.restapi.model.Policy p = new com.alu.e3.prov.restapi.model.Policy();
		p.setId						(policy.getId());
		p.getApiIds().addAll		(policy.getApiIds());
		p.getAuthIds().addAll		(BeanConverterUtil.<com.alu.e3.prov.restapi.model.AuthIdsType, QuotaRLBucket>fromDataModels(policy.getAuthIds()));
		p.getContexts().addAll		(BeanConverterUtil.<com.alu.e3.prov.restapi.model.Context, Context>fromDataModels(policy.getContexts()));

		p.setTdr					(fromDataModel(policy.getTdrGenerationRule()));

		p.setTdrOnLimitReached		(policy.getTdrOnLimitReached());
		p.setHeaderTransformations  (BeanConverterUtil.<com.alu.e3.prov.restapi.model.HeaderTransformation,HeaderTransformation>fromDataModels(policy.getHeaderTransformations()));


		for(String keyname : policy.getProperties().keySet()){
			Key k = new Key();
			k.setName(keyname);
			k.setValue(policy.getProperties().get(keyname));
			p.getProperties().add(k);
		}

		return p;
	}

	public static final Policy toDataModel(com.alu.e3.prov.restapi.model.Policy policy) {
		if (policy==null) throw new IllegalArgumentException("policy must not be null");

		Policy p = new Policy();
		p.setId						(policy.getId());
		p.getApiIds().addAll		(policy.getApiIds());
		p.getAuthIds().addAll		(BeanConverterUtil.<QuotaRLBucket, com.alu.e3.prov.restapi.model.AuthIdsType>toDataModels(policy.getAuthIds()));
		p.getContexts().addAll		(BeanConverterUtil.<Context,com.alu.e3.prov.restapi.model.Context>toDataModels(policy.getContexts()));

		p.setTdrGenerationRule		(toDataModel(policy.getTdr()));

		p.setTdrOnLimitReached		(policy.getTdrOnLimitReached());

		p.setHeaderTransformation   (BeanConverterUtil.<HeaderTransformation, com.alu.e3.prov.restapi.model.HeaderTransformation>toDataModels(policy.getHeaderTransformations()));

		for(Key prop : policy.getProperties()){
			p.getProperties().put(prop.getName(), prop.getValue());
		}

		return p;

	}

	public static final QuotaRLBucket toDataModel(com.alu.e3.prov.restapi.model.AuthIdsNoIdType authIds) {
		if (authIds==null) throw new IllegalArgumentException("authIds must not be null");

		QuotaRLBucket ids = new QuotaRLBucket();
		ids.getAuthIds().addAll(authIds.getAuthIds());
		ids.setId(authIds.getId());

		return ids;
	}




	private static final TdrGenerationRule toDataModel(TdrData tdr) {
		if (tdr==null) return null;// throw new IllegalArgumentException("tdr must not be null");
		TdrGenerationRule t = new TdrGenerationRule();
		t.getStaticRules().addAll	(BeanConverterUtil.<TdrStaticRule,StaticTdr>toDataModels(tdr.getStatic()));
		t.getDynamicRules().addAll	(BeanConverterUtil.<TdrDynamicRule,DynamicTdr>toDataModels(tdr.getDynamic()));
		return t;
	}

	/*************************************************************************
	 *************************************************************************
	 *  PRIVATE SUB CONVERTERS
	 *************************************************************************
	 *************************************************************************/

	private static final TdrEnabled fromDataModelToTdr(Boolean tdrEnabled) {
		if (tdrEnabled==null) throw new IllegalArgumentException("tdrEnabled must not be null");

		com.alu.e3.prov.restapi.model.TdrEnabled tdr = new com.alu.e3.prov.restapi.model.TdrEnabled();
		tdr.setEnabled(tdrEnabled?"true":"false");

		return tdr;
	}

	private static final Boolean toDataModel(TdrEnabled tdr) {
		if (tdr==null) throw new IllegalArgumentException("tdr must not be null");
		// FIXME: false value of tdr.getEnabled() is implicitly and badly determined
		if ("true".equals(tdr.getEnabled()))
			return true;
		if ("false".equals(tdr.getEnabled()))
			return false;
		throw new IllegalArgumentException("tdr.enabled must be one of 'true' or 'false' value");
	}

	private static final Validation fromDataModel(com.alu.e3.data.model.sub.Validation val) {
		if (val == null)
			return null;

		Validation rVal = new Validation();

		if (val.getXml() != null) {
			rVal.setXml(new Validation.Xml());
		}
		if (val.getSchema() != null) {
			rVal.setSchema(fromDataModel(val.getSchema()));
		}

		if (val.getSoap() != null) {
			rVal.setSoap(new Validation.Soap(com.alu.e3.prov.restapi.model.SoapVersionEnum.fromValue(val.getSoap().getVersion().value())));
		}

		return rVal;
	}

	private static final com.alu.e3.prov.restapi.model.Validation.Schema fromDataModel(com.alu.e3.data.model.sub.Validation.Schema schema) {
		Schema ret = new Schema();	
		ret.setType(com.alu.e3.prov.restapi.model.SchemaValidationEnum.fromValue(schema.getType().value()));

		ret.setResourcesList(BeanConverterUtil.<ResourceItem, com.alu.e3.data.model.sub.ResourceItem> fromDataModels(schema.getResourcesList()));

		return ret;
	}

	private static final com.alu.e3.data.model.sub.Validation.Schema toDataModel(com.alu.e3.prov.restapi.model.Validation.Schema schema) {

		com.alu.e3.data.model.sub.Validation.Schema mSchema = new com.alu.e3.data.model.sub.Validation.Schema();
		mSchema.setType(SchemaValidationEnum.fromValue(schema.getType().value()));

		mSchema.setResourcesList(BeanConverterUtil.<com.alu.e3.data.model.sub.ResourceItem, ResourceItem> toDataModels(schema.getResourcesList()));		

		return mSchema;
	}

	private static final com.alu.e3.prov.restapi.model.ResourceItem fromDataModel(com.alu.e3.data.model.sub.ResourceItem item) {

		ResourceItem wsItem = new ResourceItem();
		wsItem.setName(item.getName());
		wsItem.setGrammar(item.getGrammar());
		wsItem.setIsMain(item.isIsMain());		

		return wsItem;
	}

	private static final com.alu.e3.data.model.sub.ResourceItem toDataModel(com.alu.e3.prov.restapi.model.ResourceItem item) {
		com.alu.e3.data.model.sub.ResourceItem ret = new com.alu.e3.data.model.sub.ResourceItem();
		ret.setName(item.getName());
		ret.setGrammar(item.getGrammar());
		ret.setIsMain(item.isIsMain());

		return ret;
	}

	private static final com.alu.e3.data.model.sub.Validation toDataModel(Validation val) {
		if (val == null)
			return null;

		com.alu.e3.data.model.sub.Validation mVal = new com.alu.e3.data.model.sub.Validation();

		if (val.getXml() != null) {
			mVal.setXml(new com.alu.e3.data.model.sub.Validation.Xml());
		}
		if (val.getSchema() != null) {
			mVal.setSchema(toDataModel(val.getSchema()));
		}

		if (val.getSoap() != null) {
			mVal.setSoap(new com.alu.e3.data.model.sub.Validation.Soap(SoapVersionEnum.fromValue(val.getSoap().getVersion().value())));
		}

		return mVal;
	}

	private static final TdrData fromDataModel(TdrGenerationRule tdrGenerationRule) {
		if (tdrGenerationRule==null) return null; //throw new IllegalArgumentException("tdrGenerationRules must not be null");
		TdrData t = new TdrData();
		t.getStatic().addAll	(BeanConverterUtil.<StaticTdr, TdrStaticRule>fromDataModels(tdrGenerationRule.getStaticRules()));
		t.getDynamic().addAll	(BeanConverterUtil.<DynamicTdr, TdrDynamicRule>fromDataModels(tdrGenerationRule.getDynamicRules()));
		return t;
	}

	private static final StaticTdr fromDataModel(TdrStaticRule tdrStaticRule) {
		if (tdrStaticRule==null) throw new IllegalArgumentException("tdrStaticRule must not be null");

		StaticTdr s = new StaticTdr();
		s.setValue					(tdrStaticRule.getValue());
		s.setTdrPropName			(tdrStaticRule.getTdrPropName());
		s.setPropertyName			(tdrStaticRule.getPropertyName());

		TdrType tdrType = new TdrType();
		s.setTypes					(tdrType);
		tdrType.getType().addAll	(tdrStaticRule.getTypes());

		return s;
	}

	private static final DynamicTdr fromDataModel(TdrDynamicRule tdrDynamicRule) {
		if (tdrDynamicRule==null) throw new IllegalArgumentException("tdrDynamicRule must not be null");

		DynamicTdr s = new DynamicTdr();
		s.setHttpHeaderName			(tdrDynamicRule.getHttpHeaderName());
		s.setTdrPropName			(tdrDynamicRule.getTdrPropName());
		s.setExtractFrom			(tdrDynamicRule.getExtractFrom());

		TdrType tdrType = new TdrType();
		s.setTypes					(tdrType);
		tdrType.getType().addAll	(tdrDynamicRule.getTypes());

		return s;
	}

	private static TdrDynamicRule toDataModel(DynamicTdr dynamicTdr) {
		if (dynamicTdr==null) throw new IllegalArgumentException("dynamicTdr must not be null");
		TdrDynamicRule t = new TdrDynamicRule();
		t.setHttpHeaderName	(dynamicTdr.getHttpHeaderName());
		t.setTdrPropName	(dynamicTdr.getTdrPropName());
		t.setExtractFrom	(dynamicTdr.getExtractFrom());

		if(dynamicTdr.getTypes() != null)	t.getTypes().addAll(dynamicTdr.getTypes().getType());

		return t;
	}

	private static TdrStaticRule toDataModel(StaticTdr staticTdr) {
		if (staticTdr==null) throw new IllegalArgumentException("staticTdr must not be null");
		TdrStaticRule t = new TdrStaticRule();
		t.setTdrPropName	(staticTdr.getTdrPropName());
		t.setValue			(staticTdr.getValue());
		t.setPropertyName	(staticTdr.getPropertyName());

		if(staticTdr.getTypes() != null)	t.getTypes().addAll	(staticTdr.getTypes().getType());

		return t;
	}

	private static final com.alu.e3.prov.restapi.model.LoadBalancingType fromDataModel(LoadBalancingType type) {
		return com.alu.e3.prov.restapi.model.LoadBalancingType.valueOf(type.name());
	}
	private static final LoadBalancingType toDataModel(com.alu.e3.prov.restapi.model.LoadBalancingType type) {
		if (type==null) throw new IllegalArgumentException("type must not be null");
		return LoadBalancingType.valueOf(type.name());
	}

	private static final com.alu.e3.prov.restapi.model.ApiType fromDataModel(ApiType type) {
		return com.alu.e3.prov.restapi.model.ApiType.valueOf(type.name());
	}
	private static final ApiType toDataModel(com.alu.e3.prov.restapi.model.ApiType type) {
		if (type==null) throw new IllegalArgumentException("type must not be null");
		return ApiType.valueOf(type.name());
	}

	private static SubscriptionStep fromDataModel(ApiSubscriptionStep subscriptionStep) {
		// This value can be null
		if (subscriptionStep == null) return null;
		return SubscriptionStep.valueOf(subscriptionStep.name());
	}

	private static NotificationFormat fromDataModel(ApiNotificationFormat notificationFormat) {
		// This value can be null
		if (notificationFormat == null) return null;
		return NotificationFormat.valueOf(notificationFormat.name());
	}

	private static ApiSubscriptionStep toDataModel(SubscriptionStep subscriptionStep) {
		if (subscriptionStep==null) return null;
		return ApiSubscriptionStep.valueOf(subscriptionStep.name());
	}

	private static ApiNotificationFormat toDataModel(NotificationFormat notificationFormat) {
		if (notificationFormat==null) return null;
		return ApiNotificationFormat.valueOf(notificationFormat.name());
	}

	private static AuthType fromDataModel(NBAuthType type) {
		if (type==null) throw new IllegalArgumentException("type must not be null");
		return AuthType.valueOf(type.name());
	}
	private static final NBAuthType toDataModel(AuthType type) {
		return NBAuthType.valueOf(type.name());
	}

	private static final com.alu.e3.prov.restapi.model.ApiContext fromDataModel(APIContext apiContext) {
		if (apiContext==null) throw new IllegalArgumentException("apiContext must not be null");
		com.alu.e3.prov.restapi.model.ApiContext context = new com.alu.e3.prov.restapi.model.ApiContext();

		context.setId						(apiContext.getId());
		context.setDefaultContext			(apiContext.isDefaultContext());
		context.setStatus					(fromDataModel(apiContext.getStatus()));
		context.setMaxRateLimitTPMThreshold	(apiContext.getMaxRateLimitTPMThreshold());
		context.setMaxRateLimitTPMWarning	(apiContext.getMaxRateLimitTPMWarning());
		context.setMaxRateLimitTPSThreshold	(apiContext.getMaxRateLimitTPSThreshold());
		context.setMaxRateLimitTPSWarning	(apiContext.getMaxRateLimitTPSWarning());

		if (apiContext.getLoadBalancing() != null)
			context.setLoadBalancing			(fromDataModel(apiContext.getLoadBalancing()));
		// Since the addition of MO call support
		// we can have no target hosts
		if (apiContext.getTargetHosts() != null)
			context.setTargetHosts				(BeanConverterUtil.<com.alu.e3.prov.restapi.model.TargetHost, TargetHost>fromDataModels(apiContext.getTargetHosts()));

		return context;
	}

	private static final com.alu.e3.prov.restapi.model.ConnectionParameters fromDataModel(ConnectionParameters connectionParameters) {
		if (connectionParameters==null) throw new IllegalArgumentException("failOver must not be null");

		com.alu.e3.prov.restapi.model.ConnectionParameters cp = new com.alu.e3.prov.restapi.model.ConnectionParameters();
		cp.setConnectionTimeout	(connectionParameters.getConnectionTimeout());
		cp.setSocketTimeout(connectionParameters.getSocketTimeout());
		cp.setMaxConnections(connectionParameters.getMaxConnections());

		return cp;
	}

	private static final ConnectionParameters toDataModel(com.alu.e3.prov.restapi.model.ConnectionParameters connectionParametersType) {
		if (connectionParametersType==null) throw new IllegalArgumentException("failOver must not be null");

		ConnectionParameters cp = new ConnectionParameters();
		cp.setConnectionTimeout	(connectionParametersType.getConnectionTimeout());
		cp.setSocketTimeout(connectionParametersType.getSocketTimeout());
		cp.setMaxConnections(connectionParametersType.getMaxConnections());

		return cp;
	}

	private static final com.alu.e3.prov.restapi.model.TargetHost fromDataModel(TargetHost targetHost) {
		if (targetHost==null) throw new IllegalArgumentException("targetHost must not be null");
		com.alu.e3.prov.restapi.model.TargetHost th = new com.alu.e3.prov.restapi.model.TargetHost();
		th.setUrl				(targetHost.getUrl());
		th.setSite				(targetHost.getSite());
		th.setAuthentication	(fromDataModel(targetHost.getAuthentication()));
		if (targetHost.getConnectionParameters() != null)
			th.setConnectionParameters(fromDataModel(targetHost.getConnectionParameters()));
		
		return th;
	}

	private static final Authentication fromDataModel(SBAuthentication authentication) {
		if (authentication==null) return null; // throw new IllegalArgumentException("authentication must not be null");
		Authentication a = new Authentication();
		a.setType	(authentication.getType());
		a.setData	(fromDataModelToData(authentication.getKeys()));
		return a;
	}

	private static final Data fromDataModelToData(Map<String, String> keys) {
		if (keys==null) throw new IllegalArgumentException("keys must not be null");
		Data d = new Data();
		for(Map.Entry<String, String> entry : keys.entrySet()) {
			Key k = new Key();
			k.setName	(entry.getKey());
			k.setValue	(entry.getValue());
			d.getKey().add(k);
		}
		return d;
	}

	private static final ProvisionAuthentication fromDataModelToProvisionAuthentication(ApiDetail apiDetail) {
		if (apiDetail==null) throw new IllegalArgumentException("apiDetail must not be null");

		Authkey ak = new Authkey();
		ak.setKeyName		(apiDetail.getAuthKeyName());
		ak.setHeaderName	(apiDetail.getAuthHeaderName());

		ProvisionAuthentication p = new ProvisionAuthentication();
		p.setAuthKey		(ak);
		p.getAuths().addAll	(BeanConverterUtil.<AuthType, NBAuthType>fromDataModels(apiDetail.getEnabledAuthType()));

		return p;
	}


	private static final APIContext toDataModel(com.alu.e3.prov.restapi.model.ApiContext context) {
		if (context==null) throw new IllegalArgumentException("environment must not be null");
		APIContext apiContext = new APIContext();

		apiContext.setId						(context.getId());
		apiContext.setDefaultContext			(context.isDefaultContext());
		apiContext.setStatus					(toDataModel(context.getStatus()));
		apiContext.setMaxRateLimitTPMThreshold	(context.getMaxRateLimitTPMThreshold());
		apiContext.setMaxRateLimitTPMWarning	(context.getMaxRateLimitTPMWarning());
		apiContext.setMaxRateLimitTPSThreshold	(context.getMaxRateLimitTPSThreshold());
		apiContext.setMaxRateLimitTPSWarning	(context.getMaxRateLimitTPSWarning());
		if (context.getLoadBalancing() != null)
			apiContext.setLoadBalancing				(toDataModel(context.getLoadBalancing()));

		// Since the addition of MO call support
		// We can have no TargetHosts
		if (context.getTargetHosts()!=null)
			apiContext.getTargetHosts().addAll		(BeanConverterUtil.<TargetHost, com.alu.e3.prov.restapi.model.TargetHost>toDataModels(context.getTargetHosts()));

		return apiContext;
	}

	private static final TargetHost toDataModel(com.alu.e3.prov.restapi.model.TargetHost targetHost) {
		if (targetHost==null) throw new IllegalArgumentException("targetHost must not be null");
		TargetHost t = new TargetHost();
		t.setUrl				(targetHost.getUrl());
		t.setSite				(targetHost.getSite());
		t.setAuthentication		(toDataModel(targetHost.getAuthentication()));
		if (targetHost.getConnectionParameters() != null)
			t.setConnectionParameters(toDataModel(targetHost.getConnectionParameters()));
		
		return t;
	}

	private static final SBAuthentication toDataModel(Authentication authentication) {
		if (authentication==null) return null; // throw new IllegalArgumentException("authentication must not be null");
		SBAuthentication s = new SBAuthentication();
		s.setType(authentication.getType());
		for (Key k : authentication.getData().getKey())
			s.getKeys().put(k.getName(), k.getValue());
		return s;
	}

	private static final AuthKeyAuth fromDataModelToAuthKeyAuth(AuthDetail authDetail) {
		if (authDetail==null) throw new IllegalArgumentException("authDetail must not be null");
		AuthKeyAuth a = new AuthKeyAuth();
		a.setKeyValue	(authDetail.getAuthKeyValue());
		return a;
	}

	private static BasicAuth fromDataModelToBasicAuth(AuthDetail authDetail) {
		if (authDetail==null) throw new IllegalArgumentException("authDetail must not be null");
		BasicAuth b = new BasicAuth();
		b.setUsername	(authDetail.getUsername());
		b.setPassword	(authDetail.getPassword());
		return b;
	}

	private static OAuth fromDataModelToOAuth(AuthDetail authDetail) {
		if (authDetail==null) throw new IllegalArgumentException("authDetail must not be null");
		OAuth b = new OAuth();
		b.setClientId(authDetail.getClientId());
		b.setClientSecret(authDetail.getClientSecret());
		return b;
	}

	private static IpWhiteListAuth fromDataModelToIpWhiteListAuth(AuthDetail authDetail) {
		if (authDetail==null) throw new IllegalArgumentException("authDetail must not be null");
		IpWhiteListAuth i = new IpWhiteListAuth();
		i.getIp().addAll	(authDetail.getWhiteListedIps());
		return i;
	}

	private static WSSEAuth fromDataModelToWsseAuth(Auth auth){
		if (auth==null) throw new IllegalArgumentException("authDetail must not be null");
		WSSEAuth wsseAuth = new WSSEAuth();
		wsseAuth.setPasswordType(auth.getPasswordType());
		wsseAuth.setPassword(auth.getWssePassword());
		wsseAuth.setUsername(auth.getAuthDetail().getUsername());

		return wsseAuth;
	}

	private static final com.alu.e3.prov.restapi.model.Context fromDataModel(Context context) {
		if (context==null) throw new IllegalArgumentException("context must not be null");

		com.alu.e3.prov.restapi.model.Context e = new com.alu.e3.prov.restapi.model.Context();
		e.setId					(context.getId());
		e.setStatus				(fromDataModel(context.getStatus()));

		// Following are optional
		if(context.getQuotaPerDay() != null)		e.setQuotaPerDay		(fromDataModel(context.getQuotaPerDay()));
		if(context.getQuotaPerMonth() != null)		e.setQuotaPerMonth		(fromDataModel(context.getQuotaPerMonth()));
		if(context.getQuotaPerWeek() != null)		e.setQuotaPerWeek		(fromDataModel(context.getQuotaPerWeek()));
		if(context.getRateLimitPerMinute() != null)	e.setRateLimitPerMinute	(fromDataModel(context.getRateLimitPerMinute()));
		if(context.getRateLimitPerSecond() != null)	e.setRateLimitPerSecond	(fromDataModel(context.getRateLimitPerSecond()));

		return e;
	}

	private static final Context toDataModel(com.alu.e3.prov.restapi.model.Context contextData) {
		if (contextData==null) throw new IllegalArgumentException("contextData must not be null");

		Context e = new Context();
		e.setId					(contextData.getId());
		e.setStatus				(toDataModel(contextData.getStatus()));

		// Following are optional
		if(contextData.getQuotaPerDay() != null)		e.setQuotaPerDay		(toDataModel(contextData.getQuotaPerDay()));
		if(contextData.getQuotaPerMonth() != null)		e.setQuotaPerMonth		(toDataModel(contextData.getQuotaPerMonth()));
		if(contextData.getQuotaPerWeek() != null)		e.setQuotaPerWeek		(toDataModel(contextData.getQuotaPerWeek()));
		if(contextData.getRateLimitPerMinute() != null)	e.setRateLimitPerMinute	(toDataModel(contextData.getRateLimitPerMinute()));
		if(contextData.getRateLimitPerSecond() != null)	e.setRateLimitPerSecond	(toDataModel(contextData.getRateLimitPerSecond()));

		return e;
	}

	private static final com.alu.e3.prov.restapi.model.Counter fromDataModel(Counter counter) {
		if (counter==null) throw new IllegalArgumentException("counter must not be null");

		com.alu.e3.prov.restapi.model.Counter c = new com.alu.e3.prov.restapi.model.Counter();
		c.setAction		(fromDataModel(counter.getAction()));
		c.setStatus		(fromDataModel(counter.getStatus()));
		c.setThreshold	(counter.getThreshold());
		c.setWarning	(counter.getWarning());

		return c;
	}

	private static final Counter toDataModel(com.alu.e3.prov.restapi.model.Counter counterType) {
		if (counterType==null) throw new IllegalArgumentException("counterType must not be null");

		Counter c = new Counter();
		c.setAction		(toDataModel(counterType.getAction()));
		c.setStatus		(toDataModel(counterType.getStatus()));
		c.setThreshold	(counterType.getThreshold());
		c.setWarning	(counterType.getWarning());

		return c;
	}

	private static final com.alu.e3.prov.restapi.model.AuthIdsType fromDataModel(QuotaRLBucket authIds) {
		if (authIds==null) throw new IllegalArgumentException("authIds must not be null");

		com.alu.e3.prov.restapi.model.AuthIdsType ids = new com.alu.e3.prov.restapi.model.AuthIdsType();
		ids.getAuthIds().addAll(authIds.getAuthIds());
		ids.setId(authIds.getId());

		return ids;
	}

	private static final QuotaRLBucket toDataModel(com.alu.e3.prov.restapi.model.AuthIdsType authIds) {
		if (authIds==null) throw new IllegalArgumentException("authIds must not be null");
		if (authIds.getId()==null) throw new IllegalArgumentException("id must not be null");

		QuotaRLBucket ids = new QuotaRLBucket();
		ids.getAuthIds().addAll(authIds.getAuthIds());
		ids.setId(authIds.getId());

		return ids;
	}

	private static final com.alu.e3.prov.restapi.model.Action fromDataModel(ActionType action) {
		if (action==null) throw new IllegalArgumentException("action must not be null");
		return com.alu.e3.prov.restapi.model.Action.valueOf(action.name());
	}

	private static final ActionType toDataModel(com.alu.e3.prov.restapi.model.Action action) {
		if (action==null) throw new IllegalArgumentException("action must not be null");
		return ActionType.valueOf(action.name());
	}

	private static final com.alu.e3.prov.restapi.model.Status fromDataModel(StatusType status) {
		if (status==null) return null; //throw new IllegalArgumentException("status must not be null");
		return com.alu.e3.prov.restapi.model.Status.valueOf(status.name());
	}

	private static final StatusType toDataModel(com.alu.e3.prov.restapi.model.Status status) {
		if (status==null) return null; //throw new IllegalArgumentException("status must not be null");
		return StatusType.valueOf(status.name());
	}

	private static final com.alu.e3.prov.restapi.model.TargetHealthCheck fromDataModel(TargetHealthCheck targetHealthCheck) {
		if (targetHealthCheck==null) throw new IllegalArgumentException("targetHealthCheck must not be null");

		com.alu.e3.prov.restapi.model.TargetHealthCheck thc = new com.alu.e3.prov.restapi.model.TargetHealthCheck();
		thc.setType(targetHealthCheck.getType());

		return thc;
	}

	private static final TargetHealthCheck toDataModel(com.alu.e3.prov.restapi.model.TargetHealthCheck targetHealthCheckType) {
		if (targetHealthCheckType==null) throw new IllegalArgumentException("targetHealthCheckType must not be null");

		TargetHealthCheck thc = new TargetHealthCheck();
		thc.setType(targetHealthCheckType.getType());

		return thc;
	}

	private static final com.alu.e3.prov.restapi.model.FailOver fromDataModel(FailOver failOver) {
		if (failOver==null) throw new IllegalArgumentException("failOver must not be null");

		com.alu.e3.prov.restapi.model.FailOver fo = new com.alu.e3.prov.restapi.model.FailOver();
		fo.setOnResponseCode(failOver.getOnResponseCode());

		return fo;
	}

	private static final FailOver toDataModel(com.alu.e3.prov.restapi.model.FailOver failOverType) {
		if (failOverType==null) throw new IllegalArgumentException("failOver must not be null");

		FailOver fo = new FailOver();
		fo.setOnResponseCode(failOverType.getOnResponseCode());

		return fo;
	}

	private static final com.alu.e3.prov.restapi.model.LoadBalancing fromDataModel(LoadBalancing loadBalancing) {
		if (loadBalancing==null) throw new IllegalArgumentException("loadBalancing must not be null");

		com.alu.e3.prov.restapi.model.LoadBalancing lb = new com.alu.e3.prov.restapi.model.LoadBalancing();
		lb.setLoadBalancingType(fromDataModel(loadBalancing.getLoadBalancingType()));

		if(loadBalancing.getTargetHealthCheck() != null)
			lb.setTargetHealthCheck(fromDataModel(loadBalancing.getTargetHealthCheck()));

		if(loadBalancing.getFailOver() != null)
			lb.setFailOver(fromDataModel(loadBalancing.getFailOver()));

		return lb;
	}

	private static final LoadBalancing toDataModel(com.alu.e3.prov.restapi.model.LoadBalancing loadBalancing) {
		if (loadBalancing==null) throw new IllegalArgumentException("loadBalancing must not be null");

		LoadBalancing lb = new LoadBalancing();
		lb.setLoadBalancingType(toDataModel(loadBalancing.getLoadBalancingType()));

		if(loadBalancing.getTargetHealthCheck() != null)
			lb.setTargetHealthCheck(toDataModel(loadBalancing.getTargetHealthCheck()));

		if(loadBalancing.getFailOver() != null)
			lb.setFailOver(toDataModel(loadBalancing.getFailOver()));

		return lb;
	}

	public static final SSLKey fromDataModel(com.alu.e3.data.model.Key key) {
		if (key==null) return null; //throw new IllegalArgumentException("status must not be null");

		SSLKey ret = new SSLKey();

		ret.setActiveCertId(key.getActiveCertId());
		ret.setDisplayName(key.getKeyDetail().getName());
		// do NOT set SSLKey.content and SSLKey.keyPassphrase
		// key data should not be accessible through the provisioning api.
		ret.setId(key.getId());
		ret.setType(key.getKeyDetail().getType());

		return ret;
	}


	public static final com.alu.e3.data.model.Key toDataModel(SSLKey key) {
		if (key==null) return null; //throw new IllegalArgumentException("status must not be null");
		com.alu.e3.data.model.Key ret = new com.alu.e3.data.model.Key();

		KeyDetail kd = new KeyDetail();
		kd.setId(key.getId());
		kd.setName(key.getDisplayName());
		kd.setType(key.getType());

		ret.setId(key.getId());
		ret.setKeyDetail(kd);
		ret.setData(key.getContent());

		ret.setActiveCertId(key.getActiveCertId());
		ret.setKeyPassphrase(key.getKeyPassphrase());

		return ret;
	}

	public static final SSLCert fromDataModel(Certificate cert){
		if(cert == null) throw new IllegalArgumentException("cert must not be null");

		SSLCert sslCert = new SSLCert();
		sslCert.setDisplayName(cert.getCertDetail().getName());
		sslCert.setContent(cert.getData());
		sslCert.setId(cert.getId());
		sslCert.setKeyId(cert.getCertDetail().getKeyId());

		return sslCert;
	}



	public static final Certificate toDataModel(SSLCert sslCert){
		if(sslCert == null) throw new IllegalArgumentException("cert must not be null");

		Certificate cert = new Certificate();
		cert.setData(sslCert.getContent());
		cert.setId(sslCert.getId());
		cert.setPassword(sslCert.getKeyId());

		CertificateDetail cd = new CertificateDetail();
		cd.setId(sslCert.getId());
		cd.setKeyId(sslCert.getKeyId());
		cd.setName(sslCert.getDisplayName());

		cert.setCertDetail(cd);

		return cert;
	}

	/*************************************************************************
	 *************************************************************************
	 *  GENERICS PRIVATE TOOLS
	 *************************************************************************
	 *************************************************************************/

	@SuppressWarnings("unchecked")
	private static final <U,V> List<U> fromDataModels(List<V> dataModels) {
		if (dataModels==null) throw new IllegalArgumentException("dataModels must not be null");
		List<U> us = new ArrayList<U>();
		for(V v : dataModels) {
			// Very very dumb generic limitation:
			// solve compilation and IDE ambiguity
			if (v instanceof APIContext)
				us.add((U)fromDataModel((APIContext)v));
			else if (v instanceof TargetHost)
				us.add((U)fromDataModel((TargetHost)v));
			else if (v instanceof Context)
				us.add((U)fromDataModel((Context)v));
			else if (v instanceof QuotaRLBucket)
				us.add((U)fromDataModel((QuotaRLBucket)v));
			else if (v instanceof NBAuthType)
				us.add((U)fromDataModel((NBAuthType)v));
			else if (v instanceof TdrStaticRule)
				us.add((U)fromDataModel((TdrStaticRule)v));
			else if (v instanceof TdrDynamicRule)
				us.add((U)fromDataModel((TdrDynamicRule)v));
			else if (v instanceof com.alu.e3.data.model.sub.ResourceItem)
				us.add((U) fromDataModel((com.alu.e3.data.model.sub.ResourceItem) v));
			else if (v instanceof HeaderTransformation)
				us.add((U) fromDataModel((HeaderTransformation) v));
			else
				throw new IllegalArgumentException("List type:"+v.getClass().getName()+" conversion not supported");
		}
		return us;
	}

	@SuppressWarnings("unchecked")
	private static final <U,V> List<U> toDataModels(List<V> wsModels) {
		if (wsModels==null) throw new IllegalArgumentException("wsModels must not be null");
		List<U> us = new ArrayList<U>();
		for(V v : wsModels) {
			// Very very dumb generic limitation:
			// solve compilation and IDE ambiguity
			if (v instanceof com.alu.e3.prov.restapi.model.ApiContext)
				us.add((U)toDataModel((com.alu.e3.prov.restapi.model.ApiContext)v));
			else if (v instanceof com.alu.e3.prov.restapi.model.TargetHost)
				us.add((U)toDataModel((com.alu.e3.prov.restapi.model.TargetHost)v));
			else if (v instanceof com.alu.e3.prov.restapi.model.Context)
				us.add((U)toDataModel((com.alu.e3.prov.restapi.model.Context)v));
			else if (v instanceof com.alu.e3.prov.restapi.model.AuthIdsType)
				us.add((U)toDataModel((com.alu.e3.prov.restapi.model.AuthIdsType)v));
			else if (v instanceof AuthType)
				us.add((U)toDataModel((AuthType)v));
			else if (v instanceof StaticTdr)
				us.add((U)toDataModel((StaticTdr)v));
			else if (v instanceof DynamicTdr)
				us.add((U)toDataModel((DynamicTdr)v));
			else if (v instanceof ResourceItem)
				us.add((U) toDataModel((ResourceItem) v));
			else if (v instanceof com.alu.e3.prov.restapi.model.HeaderTransformation)
				us.add((U) toDataModel((com.alu.e3.prov.restapi.model.HeaderTransformation) v));
			else
				throw new IllegalArgumentException("List type:"+v.getClass().getName()+" conversion not supported");
		}
		return us;
	}

	private static final com.alu.e3.prov.restapi.model.HTTPSType fromDataModel(HTTPSType httpsType) {
		if (httpsType==null) throw new IllegalArgumentException("httpsType must not be null");

		com.alu.e3.prov.restapi.model.HTTPSType r = new com.alu.e3.prov.restapi.model.HTTPSType();
		r.setEnabled(httpsType.isEnabled());

		if(httpsType.getTlsMode() != null)	r.setTlsMode(fromDataModel(httpsType.getTlsMode()));

		return r;
	}

	public static final HTTPSType toDataModel(com.alu.e3.prov.restapi.model.HTTPSType httpsType) {
		if (httpsType==null) throw new IllegalArgumentException("httpsType must not be null");

		HTTPSType r = new HTTPSType();
		r.setEnabled(httpsType.isEnabled());

		if(httpsType.getTlsMode() != null) r.setTlsMode(toDataModel(httpsType.getTlsMode()));

		return r;		
	}

	public static final com.alu.e3.prov.restapi.model.ForwardProxy fromDataModel(ForwardProxy forwardProxy) {
		if (forwardProxy==null) return null;

		com.alu.e3.prov.restapi.model.ForwardProxy fp = new com.alu.e3.prov.restapi.model.ForwardProxy();
		fp.setProxyHost(forwardProxy.getProxyHost());
		fp.setProxyPass(forwardProxy.getProxyPass());
		fp.setProxyPort(forwardProxy.getProxyPort());
		fp.setProxyUser(forwardProxy.getProxyUser());

		return fp;
	}

	public static final ForwardProxy toDataModel(com.alu.e3.prov.restapi.model.ForwardProxy forwardProxy) {
		if (forwardProxy==null) return null;

		ForwardProxy fp = new ForwardProxy();
		fp.setProxyHost(forwardProxy.getProxyHost());
		fp.setProxyPass(forwardProxy.getProxyPass());
		fp.setProxyPort(forwardProxy.getProxyPort());
		fp.setProxyUser(forwardProxy.getProxyUser());

		return fp;		
	}

	private static final com.alu.e3.prov.restapi.model.TLSMode fromDataModel(TLSMode tlsMode) {
		if (tlsMode==null) throw new IllegalArgumentException("tlsMode must not be null");

		return com.alu.e3.prov.restapi.model.TLSMode.valueOf(tlsMode.name());
	}

	public static final TLSMode toDataModel(com.alu.e3.prov.restapi.model.TLSMode tlsMode) {
		if (tlsMode==null) throw new IllegalArgumentException("tlsMode must not be null");

		return TLSMode.valueOf(tlsMode.name());
	}

	public static final com.alu.e3.prov.restapi.model.SSLCRL fromDataModel(SSLCRL crl) {
		if (crl==null) throw new IllegalArgumentException("crl must not be null");

		com.alu.e3.prov.restapi.model.SSLCRL r = new com.alu.e3.prov.restapi.model.SSLCRL();
		r.setId(crl.getId());
		r.setContent(crl.getContent());
		r.setDisplayName(crl.getDisplayName());
		return r;
	}

	public static final SSLCRL toDataModel(com.alu.e3.prov.restapi.model.SSLCRL crl) {
		if (crl==null) throw new IllegalArgumentException("crl must not be null");

		SSLCRL r = new SSLCRL();
		r.setId(crl.getId());
		r.setContent(crl.getContent());
		r.setDisplayName(crl.getDisplayName());
		return r;		
	}

}
