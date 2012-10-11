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

import java.util.List;
import java.util.regex.Pattern;

import com.alu.e3.common.util.ConcurrentTree;

/**
 * Dispatcher based on ConcurrentTree.
 * The default path delimiter is "/+".
 *
 * @param <E> type of the endpoint objects
 */
public class TreeDispatcher<E> implements Dispatcher<E> {
	private final ConcurrentTree<String, E> tree = new ConcurrentTree<String, E>();
	private Pattern delimiter = Pattern.compile("/+");

	/**
	 * Sets the path delimiter.
	 * @param delimiter
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = Pattern.compile(delimiter);
	}

	@Override
	public void addEndpoint(String path, E endpoint) {
		tree.add(delimiter.split(path), endpoint);
	}

	@Override
	public E removeEndpoint(String path) {
		return tree.remove(delimiter.split(path));
	}
	
	@Override
	public E findExactMatch(String path) {
		return tree.get(delimiter.split(path));
	}

	@Override
	public List<E> findPrefixMatches(String path) {
		return tree.getMatches(delimiter.split(path));
	}
}
