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
package com.alu.e3.common.util;

import static org.junit.Assert.*;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

public class ConcurrentTreeTest {

	private final Pattern delimiter = Pattern.compile("/");
	
	private ConcurrentTree<String, String> setupTree() {
		ConcurrentTree<String, String> tree = new ConcurrentTree<String, String>();
		for (String path : new String[] {
				"a/b",
				"a/b/c/d",
				"a/b/c/d/e/f",
				"a/a",
				"a/a/c/d",
				"a/a/c/d/e/f",
				"b/b",
				"b/b/c/d",
				"b/b/c/d/e/f",
				"b/a",
				"b/a/c/d",
				"b/a/c/d/e/f" })
		{
			tree.add(delimiter.split(path), path);
		}
		return tree;
	}
	
	@Test
	public void testAdd() {
		ConcurrentTree<String, String> tree = setupTree();
		
		tree.add(delimiter.split("c"), "c");
		assertEquals(tree.get(delimiter.split("c")), "c");
		
		tree.add(delimiter.split("c"), "d");
		assertEquals(tree.get(delimiter.split("c")), "d");
		
		tree.add(delimiter.split("/c"), "e");
		assertEquals(tree.get(delimiter.split("c")), "d");
		assertEquals(tree.get(delimiter.split("/c")), "e");
		
		tree.add(delimiter.split("c/ "), "f");
		assertEquals(tree.get(delimiter.split("c")), "d");
		assertEquals(tree.get(delimiter.split("c/ ")), "f");
			
		tree.add(delimiter.split("a/c"), "g");
		assertEquals(tree.get(delimiter.split("a/c")), "g");
		
		tree.add(delimiter.split("a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z"), "z");
		assertEquals(tree.get(delimiter.split("a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z")), "z");
		
		tree.add(delimiter.split("a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p//q/r/s/t/u/v/w/x/y/z"), "y");
		assertEquals(tree.get(delimiter.split("a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p//q/r/s/t/u/v/w/x/y/z")), "y");
		assertEquals(tree.get(delimiter.split("a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p/q/r/s/t/u/v/w/x/y/z")), "z");
	}

	@Test
	public void testRemove() {
		ConcurrentTree<String, String> tree = setupTree();
		
		assertEquals(tree.remove(delimiter.split("a/b")), "a/b");
		assertNull(tree.get(delimiter.split("a/b")));
		assertNotNull(tree.get(delimiter.split("a/b/c/d/e/f")));
		assertNotNull(tree.get(delimiter.split("a/a")));
		
		assertEquals(tree.remove(delimiter.split("a/b/c/d/e/f")), "a/b/c/d/e/f");
		assertNull(tree.remove(delimiter.split("a/b/c/d/e/f")));
		assertNull(tree.remove(delimiter.split("a/b/c/d/e")));
		assertNotNull(tree.get(delimiter.split("a/b/c/d")));
		
		assertNull(tree.remove(delimiter.split("/b/b/c/d/e/f")));
		assertNull(tree.remove(delimiter.split("b/b/c//d/e/f")));
		assertNull(tree.remove(delimiter.split("b/b/c/d/e/f/ ")));
		assertNotNull(tree.get(delimiter.split("b/b/c/d/e/f")));
		
		assertNull(tree.remove(delimiter.split("b/b/c/d/e")));
		assertNotNull(tree.get(delimiter.split("b/b/c/d/e/f")));
		assertNotNull(tree.get(delimiter.split("b/b/c/d")));
	}
	
	@Test
	public void testGet() {
		ConcurrentTree<String, String> tree = setupTree();
		
		assertNull(tree.get(delimiter.split("a/b/c/d/e/f/g")));
		assertEquals(tree.get(delimiter.split("a/b/c/d/e/f")), "a/b/c/d/e/f");
		assertNull(tree.get(delimiter.split("a/b/c/d/e")));
		assertEquals(tree.get(delimiter.split("a/b/c/d")), "a/b/c/d");
		assertNull(tree.get(delimiter.split("a/b/c")));
		assertEquals(tree.get(delimiter.split("a/b")), "a/b");
		assertNull(tree.get(delimiter.split("a")));
		assertNull(tree.get(delimiter.split("")));
		
		assertNull(tree.get(delimiter.split("/a/b")));
		assertNull(tree.get(delimiter.split("a//b")));
		assertNull(tree.get(delimiter.split("a/b/ ")));
	}
	
	@Test
	public void testGetMatches() {
		ConcurrentTree<String, String> tree = setupTree();
		
		List<String> matches = tree.getMatches(delimiter.split("b/a/c/d/e/f/g"));
		assertNotNull(matches);
		assertEquals(matches.size(), 3);
		assertEquals(matches.get(0), "b/a/c/d/e/f");
		assertEquals(matches.get(1), "b/a/c/d");
		assertEquals(matches.get(2), "b/a");
		
		matches = tree.getMatches(delimiter.split("b/a/c/c/e/f/g"));
		assertEquals(matches.size(), 1);
		assertEquals(matches.get(0), "b/a");
		
		matches = tree.getMatches(delimiter.split("/b/a/c/d/e/f/g"));
		assertNotNull(matches);
		assertTrue(matches.isEmpty());
		
		matches = tree.getMatches(delimiter.split("b//a/c/d/e/f/g"));
		assertTrue(matches.isEmpty());
		
		matches = tree.getMatches(delimiter.split("b/ /a/c/d/e/f/g"));
		assertTrue(matches.isEmpty());
		
		matches = tree.getMatches(delimiter.split("b/a//c"));
		assertFalse(matches.isEmpty());
	}
}
