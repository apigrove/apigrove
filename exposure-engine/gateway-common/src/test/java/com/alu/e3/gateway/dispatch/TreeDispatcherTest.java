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
package com.alu.e3.gateway.dispatch;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class TreeDispatcherTest {

	private TreeDispatcher<String> setupDispatcher() {
		TreeDispatcher<String> dispatcher = new TreeDispatcher<String>();
		for (String path : new String[] {
				"/one/two",
				"/one/two/three/four",
				"/one/two/three/four/five/six",
				"/one/three/three/four",
				"/one/three/three/four/five/six" })
		{
			dispatcher.addEndpoint(path, path);
		}
		return dispatcher;
	}
	
	@Test
	public void testSetDelimiter() {
		TreeDispatcher<String> dispatcher = setupDispatcher();
		dispatcher.setDelimiter("\\\\");
		
		assertEquals(dispatcher.findExactMatch("\\one\\two"), "/one/two");
		assertNull(dispatcher.findExactMatch("/one/two"));
	}
	
	@Test
	public void testFindExactMatch() {
		TreeDispatcher<String> dispatcher = setupDispatcher();
		
		assertNull(dispatcher.findExactMatch(""));
		assertNull(dispatcher.findExactMatch("/"));
		assertNull(dispatcher.findExactMatch("/one"));
		assertEquals(dispatcher.findExactMatch("/one/two"), "/one/two");
		assertNull(dispatcher.findExactMatch("/one/two/three"));
		assertEquals(dispatcher.findExactMatch("/one/two/three/four"), "/one/two/three/four");
		assertNull(dispatcher.findExactMatch("/one/two/three/four/five"));
		assertEquals(dispatcher.findExactMatch("/one/two/three/four/five/six"), "/one/two/three/four/five/six");
		assertNull(dispatcher.findExactMatch("/one/two/three/four/five/six/seven"));
		assertNull(dispatcher.findExactMatch("/one/three"));
		assertNull(dispatcher.findExactMatch("/one/three/three"));
		assertEquals(dispatcher.findExactMatch("/one/three/three/four"), "/one/three/three/four");
		assertNull(dispatcher.findExactMatch("/one/three/three/four/five"));
		assertEquals(dispatcher.findExactMatch("/one/three/three/four/five/six"), "/one/three/three/four/five/six");
		assertNull(dispatcher.findExactMatch("/one/three/three/four/five/six/seven"));
		assertNull(dispatcher.findExactMatch("/one/three/four"));
		assertNull(dispatcher.findExactMatch("/one/four"));
		
		assertEquals(dispatcher.findExactMatch("/one////two////"), "/one/two");
		assertEquals(dispatcher.findExactMatch("////one////two"), "/one/two");
		assertEquals(dispatcher.findExactMatch("////one////two////"), "/one/two");
		
		assertNull(dispatcher.findExactMatch("/*"));
		assertNull(dispatcher.findExactMatch("/.*"));
		assertNull(dispatcher.findExactMatch("one"));
		assertNull(dispatcher.findExactMatch("/one/tw"));
		assertNull(dispatcher.findExactMatch("/one/twoo"));
		assertNull(dispatcher.findExactMatch("/one/two*"));
		assertNull(dispatcher.findExactMatch("/one/two.*"));
		assertNull(dispatcher.findExactMatch("/on/two"));
		assertNull(dispatcher.findExactMatch("/onee/two"));
		assertNull(dispatcher.findExactMatch("/onetwo"));
		assertNull(dispatcher.findExactMatch("/oneetwo"));
		assertNull(dispatcher.findExactMatch("/one\\two"));
	}

	@Test
	public void testFindPrefixMatches() {
		TreeDispatcher<String> dispatcher = setupDispatcher();
		
		List<String> matches = dispatcher.findPrefixMatches("/one/two/three/four/five/six/seven/eight");
		assertNotNull(matches);
		assertEquals(matches.size(), 3);
		assertEquals(matches.get(0), "/one/two/three/four/five/six");
		assertEquals(matches.get(1), "/one/two/three/four");
		assertEquals(matches.get(2), "/one/two");
		
		matches = dispatcher.findPrefixMatches("//one///two////three/////four////five///six///seven/eight////");
		assertEquals(matches.size(), 3);
		assertEquals(matches.get(0), "/one/two/three/four/five/six");
		assertEquals(matches.get(1), "/one/two/three/four");
		assertEquals(matches.get(2), "/one/two");
		
		matches = dispatcher.findPrefixMatches("//one///two////three//\\//four/five/six/seven/eight");
		assertEquals(matches.size(), 1);
		assertEquals(matches.get(0), "/one/two");
		
		matches = dispatcher.findPrefixMatches("/one/two");
		assertEquals(matches.size(), 1);
		assertEquals(matches.get(0), "/one/two");
		
		matches = dispatcher.findPrefixMatches("/one/three/three");
		assertNotNull(matches);
		assertTrue(matches.isEmpty());
		
		assertTrue(dispatcher.findPrefixMatches("/*").isEmpty());
		assertTrue(dispatcher.findPrefixMatches("/.*").isEmpty());
		assertTrue(dispatcher.findPrefixMatches("one").isEmpty());
		assertTrue(dispatcher.findPrefixMatches("/one/tw").isEmpty());
		assertTrue(dispatcher.findPrefixMatches("/one/twoo").isEmpty());
		assertTrue(dispatcher.findPrefixMatches("/one/two*").isEmpty());
		assertTrue(dispatcher.findPrefixMatches("/one/two.*").isEmpty());
		assertTrue(dispatcher.findPrefixMatches("/on/two").isEmpty());
		assertTrue(dispatcher.findPrefixMatches("/onee/two").isEmpty());
		assertTrue(dispatcher.findPrefixMatches("/onetwo").isEmpty());
		assertTrue(dispatcher.findPrefixMatches("/oneetwo").isEmpty());
		assertTrue(dispatcher.findPrefixMatches("/one\\two").isEmpty());
	}
}
