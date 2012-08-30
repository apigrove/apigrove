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


import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.alu.e3.data.model.Api;
import com.alu.e3.data.model.Auth;
import com.alu.e3.data.model.Policy;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {
//	})
public class AllSerializableModelTest {
	
	@Test
	public void allSeriazableTest() throws InstantiationException, IllegalAccessException {
		Class<?>[] classesToTest = new Class<?>[] {
				Api.class,
				Auth.class, 
				Policy.class
		};
		
		for(Class<?> classToTest : classesToTest) {
			log(0, "Testing of class: "+classToTest.getName());
			assertAllSerializable(classToTest, 1);
		}
	}

	private <T> void assertAllSerializable(Class<T> classToLoad, int level) throws InstantiationException, IllegalAccessException {

		assertTrue("Class:"+classToLoad.getSimpleName()+" must be serializable", interfaceOf(classToLoad.getInterfaces(),Serializable.class));
		
		for(Field field : classToLoad.getDeclaredFields()) {
			log(level, "Tested field: "+(field.isAccessible()?"public":"private")+" "+field.getName());
			
			Class<?> clazz = field.getType();
			if (clazz.isAssignableFrom(String.class))
				good();
			else if (clazz.isAssignableFrom(Boolean.class) || clazz.equals(boolean.class))
				good();
			else if (clazz.isAssignableFrom(Long.class) || clazz.equals(long.class))
				good();
			else if (clazz.isAssignableFrom(Integer.class) || clazz.equals(int.class))
				good();
			else if (clazz.isAssignableFrom(Float.class) || clazz.equals(float.class))
				good();
			else if (clazz.isAssignableFrom(Short.class) || clazz.equals(short.class))
				good();
			else if (clazz.isEnum())
				good();
			else if (clazz.isAssignableFrom(Date.class))
				good();
			else if (clazz.isAssignableFrom(List.class) || clazz.isAssignableFrom(Map.class)) {
				ParameterizedType type = (ParameterizedType) field.getGenericType();
				Class<?> clazzInList = (Class<?>) type.getActualTypeArguments()[0];
				
				if (clazzInList.isAssignableFrom(String.class))
					good();
				else if (clazzInList.isAssignableFrom(Integer.class))
					good();
				else if (clazzInList.isAssignableFrom(Boolean.class))
					good();
				else if (clazzInList.isAssignableFrom(Long.class))
					good();
				else if (clazzInList.isAssignableFrom(Float.class))
					good();
				else if (clazzInList.isAssignableFrom(Short.class))
					good();
				else if (clazzInList.isEnum())
					good();
				else
					assertAllSerializable(clazzInList, level+1);
				
			} else if (clazz.isArray())
				good();
			else
				assertAllSerializable(clazz, level+1);
		}
	}
	
	private boolean interfaceOf(Class<?>[] interfaces, Class<?> i) {
		for(Class<?> ii : interfaces)
			if (ii.isAssignableFrom(i)) return true;
		return false;
	}
	
	private void good() {}

	private void log(int level, String s) {
		for(int i=0; i<level; i++)
			System.out.print("  ");
		System.out.println(s);
	}
}
