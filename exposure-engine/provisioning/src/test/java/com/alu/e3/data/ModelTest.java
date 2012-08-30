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
package com.alu.e3.data;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.junit.Test;

import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.Policy;
import com.alu.e3.data.wrapper.BeanConverterUtil;
import com.alu.e3.prov.restapi.model.AuthType;
import com.alu.e3.prov.restapi.model.TdrEnabled;

public class ModelTest {
	
	@Test
	public void beanWrapperUtilTest() throws InstantiationException, IllegalAccessException, Exception {
		Class<?>[] classesToLoad = new Class<?>[] {
				com.alu.e3.prov.restapi.model.Api.class,
				//com.alu.e3.prov.restapi.model.Auth.class,
				//com.alu.e3.prov.restapi.model.Policy.class,
		};
		
		
		
		for(Class<?> classToLoad : classesToLoad) {
			JAXBContext context = JAXBContext.newInstance(classToLoad);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			for(int testingNumber=0; testingNumber<10; testingNumber++) {
				System.out.println("Testing of class: "+classToLoad.getName());
				Object filledObject = loadObjectClass(classToLoad, 2);
				System.out.println("Generated randomly full filled object: "+filledObject);
				
				Object transformed = null;
				Object reTransformed = null;
				
				if (filledObject instanceof com.alu.e3.prov.restapi.model.Api) {
					
					Random random = new Random(); 
					int intRandom = random.nextInt();
					if(intRandom > 100)
						((com.alu.e3.prov.restapi.model.Api)filledObject).getProxySettings().setGlobalProxy(null);
					else
						((com.alu.e3.prov.restapi.model.Api)filledObject).getProxySettings().setLocalProxy(null);
					
					transformed = BeanConverterUtil.toDataModel((com.alu.e3.prov.restapi.model.Api)filledObject);
					System.out.println("Transformed toDataModel object: "+transformed);
					reTransformed = BeanConverterUtil.fromDataModel((Api)transformed);
					System.out.println("ReTransformed back toWsModel object: "+reTransformed);
				}
				else if (filledObject instanceof com.alu.e3.prov.restapi.model.Auth) {
					com.alu.e3.prov.restapi.model.Auth specialPrepared = (com.alu.e3.prov.restapi.model.Auth)filledObject;
					// Only one of auth type
					if (specialPrepared.getType() != AuthType.AUTHKEY)
						specialPrepared.setAuthKeyAuth(null);
					if (specialPrepared.getType() != AuthType.BASIC)
						specialPrepared.setBasicAuth(null);
					if (specialPrepared.getType() != AuthType.IP_WHITE_LIST)
						specialPrepared.setIpWhiteListAuth(null);
					
					transformed = BeanConverterUtil.toDataModel(specialPrepared);
					System.out.println("Transformed toDataModel object: "+transformed);
					reTransformed = BeanConverterUtil.fromDataModel((Auth)transformed);
					System.out.println("ReTransformed back toWsModel object: "+reTransformed);
				}
				else if (filledObject instanceof com.alu.e3.prov.restapi.model.Policy) {
					transformed = BeanConverterUtil.toDataModel((com.alu.e3.prov.restapi.model.Policy)filledObject);
					System.out.println("Transformed toDataModel object: "+transformed);
					reTransformed = BeanConverterUtil.fromDataModel((Policy)transformed);
					System.out.println("ReTransformed back toWsModel object: "+reTransformed);
				}
				else
					throw new IllegalArgumentException("Unsupported test for type: "+filledObject.getClass().getName());
	
				System.out.println("=====================================================");
				System.out.println("Deep assert equals on: "+classToLoad.getSimpleName());
				deepAssertEquals(filledObject, reTransformed, classToLoad.getName());
				System.out.println("!! Double Conversion Success !!");
				
				marshaller.marshal(filledObject, System.out);

			}
		}
	}

	private <T> T loadObjectClass(Class<T> classToLoad, int level) throws InstantiationException, IllegalAccessException {
		T instance = classToLoad.newInstance();
		for(Field field : classToLoad.getDeclaredFields()) {
			boolean wasAccessible = field.isAccessible();
			field.setAccessible(true);
			
			Class<?> clazz = field.getType();
			Object randomValue = null;
			if (clazz.isAssignableFrom(TdrEnabled.class))
				randomValue = randomTdrEnabledValue();
			else if (clazz.isAssignableFrom(String.class))
				randomValue = randomString();
			else if (clazz.isAssignableFrom(Integer.class) || clazz.equals(int.class))
				randomValue = randomInt();
			else if (clazz.isAssignableFrom(Float.class) || clazz.equals(float.class))
				randomValue = randomFloat();
			else if (clazz.isAssignableFrom(Long.class) || clazz.equals(long.class))
				randomValue = randomLong();
			else if (clazz.isAssignableFrom(Boolean.class) || clazz.equals(boolean.class))
				randomValue = randomBoolean();
			else if (clazz.isEnum())
				randomValue = randomEnumOf(clazz);
			else if (clazz.isAssignableFrom(List.class))
				randomValue = randomListOf(field, level+1);
			else if (clazz.isArray())
				randomValue = arrayOfBytes();
			else
				randomValue = loadObjectClass(clazz, level+1);
			field.set(instance, randomValue);
			
			field.setAccessible(wasAccessible);
		}
		return instance;
	}


	private TdrEnabled randomTdrEnabledValue() {
		TdrEnabled tdr = new TdrEnabled();
		if (Math.random()>0.5)
			tdr.setEnabled("true");
		else
			tdr.setEnabled("false");
		return tdr;
	}
	
	private String randomString() {
		return UUID.randomUUID().toString();
	}

	private Integer randomInt() {
		return new Random().nextInt();
	}

	private Float randomFloat() {
		return Math.round((new Random()).nextFloat() * 100.0f) / 100.0f;
	}
	
	private Long randomLong() {
		return new Random().nextLong();
	}
	
	private Boolean randomBoolean() {
		return randomInt() % 2 == 0;
	}
	
	private Object randomEnumOf(Class<?> enumClazz) throws InstantiationException, IllegalAccessException {
		Object[] enumValues = enumClazz.getEnumConstants();
		return enumValues[new Random().nextInt(enumValues.length)];
	}
	
	private byte[] arrayOfBytes() {
		return new byte[] { 0x01, 0x02, 0x03 };
	}
	
	private <T> List<Object> randomListOf(Field field, int level) throws InstantiationException, IllegalAccessException {
		ParameterizedType type = (ParameterizedType) field.getGenericType();
		Class<?> clazz = (Class<?>) type.getActualTypeArguments()[0];
		
		List<Object> list = new ArrayList<Object>();
		for(int i=0; i<10; i++) {
			Object randomValue = null;
			if (clazz.isAssignableFrom(String.class))
				randomValue = randomString();
			else if (clazz.isEnum())
				randomValue = randomEnumOf(clazz);
			else
				randomValue = loadObjectClass(clazz, level+1);
			list.add(randomValue);
		}
		return list;
	}
	
	private void deepAssertEquals(Object filledObject, Object reTransformed, String pkg) throws IllegalArgumentException, IllegalAccessException {
		if (filledObject == null) {
			assertNull("null comparison failed :"+pkg, reTransformed);
			return;
		}
		
		Class<?> testedClazz = filledObject.getClass();
		
		assertNotNull("Transformation has not supported, it's null: "+pkg, reTransformed);
		
		for(Field field : testedClazz.getDeclaredFields()) {
			//System.out.println("Tested field: "+pkg+"."+field.getName());
			
			boolean wasAccessible = field.isAccessible();
			field.setAccessible(true);
			
			Class<?> clazz = field.getType();
			Object a = field.get(filledObject);
			Object b = field.get(reTransformed);
			
			field.setAccessible(wasAccessible);
			
			if (clazz.isAssignableFrom(String.class))
				assertEquals("String comparison failed: "+pkg+"."+field.getName(), a, b);
			else if (clazz.isAssignableFrom(Integer.class) || clazz.equals(int.class))
				assertEquals("Integer comparison failed: "+pkg+"."+field.getName(), a, b);
			else if (clazz.isAssignableFrom(Float.class) || clazz.equals(float.class))
				assertEquals("Float comparison failed: "+pkg+"."+field.getName(), a, b);
			else if (clazz.isAssignableFrom(Long.class) || clazz.equals(long.class))
				assertEquals("Long comparison failed: "+pkg+"."+field.getName(), a, b);			
			else if (clazz.isAssignableFrom(Boolean.class) || clazz.equals(boolean.class))
				assertEquals("Boolean comparison failed: "+pkg+"."+field.getName(), a, b);
			else if (clazz.isEnum())
				assertEquals("Enum comparison failed: "+pkg+"."+field.getName(), a, b);
			else if (clazz.isAssignableFrom(List.class))
				deepAssertEqualsList((List<?>)a, (List<?>)b, pkg+"."+field.getName());
			else if (clazz.isArray())
				deepAssertEqualsByteArray((byte[])a, (byte[])b, pkg+"."+field.getName());
			else
				deepAssertEquals(a, b, pkg+"."+field.getName());
		}
	}
	
	
	private void deepAssertEqualsByteArray(byte[] filledObject, byte[] reTransformed, String pkg) {
		for(int i=0; i<filledObject.length; i++)
			assertEquals("byte comparison failed: "+pkg, filledObject[i], reTransformed[i]);
	}

	private <T> void  deepAssertEqualsList(List<?> filledObject, List<?> reTransformed, String pkg) throws IllegalArgumentException, IllegalAccessException {
		for(int i=0; i<filledObject.size(); i++) {
			//System.out.println("Testing "+pkg+"["+i+"]");
			
			Object a = filledObject.get(i);
			assertNotNull("BeanConverterUtil may miss this attribute: "+pkg+"<"+a.getClass().getSimpleName()+">", reTransformed);
			
			Object b = reTransformed.get(i);
			
			Class<?> clazz = a.getClass();
			if (clazz.isAssignableFrom(String.class))
				assertEquals("String comparison failed: "+pkg+"["+i+"]", a, b);
			else if (clazz.isAssignableFrom(Integer.class) || clazz.equals(int.class))
				assertEquals("Integer comparison failed: "+pkg+"["+i+"]", a, b);
			else if (clazz.isAssignableFrom(Float.class) || clazz.equals(float.class))
				assertEquals("Float comparison failed: "+pkg+"["+i+"]", a, b);
			else if (clazz.isAssignableFrom(Long.class) || clazz.equals(long.class))
				assertEquals("Long comparison failed: "+pkg+"["+i+"]", a, b);			
			else if (clazz.isAssignableFrom(Boolean.class) || clazz.equals(boolean.class))
				assertEquals("Boolean comparison failed: "+pkg+"["+i+"]", a, b);
			else if (clazz.isEnum())
				assertEquals("Enum comparison failed: "+pkg+"["+i+"]", a, b);
			else if (clazz.isArray())
				deepAssertEqualsByteArray((byte[])a, (byte[])b, pkg+"["+i+"]");
			else
				deepAssertEquals(a, b, pkg+"["+i+"]");
		}
	}
}
