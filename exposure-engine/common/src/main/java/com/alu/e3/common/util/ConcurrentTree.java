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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Root element of a tree. 
 * It is thread-safe, as the add/remove operations override volatile objects.
 *  
 * @param <K> type of the path element 
 * @param <V> type of the value element
 */
public class ConcurrentTree<K, V> extends ConcurrentTreeNode<K, V> {

	/**
	 * Adds or overrides a value of the specified path.
	 * @param path
	 * @param value
	 */
	public synchronized void add(final K[] path, final V value) {
		ConcurrentTreeNode<K, V> node = this;
		for (final K part : path) {
			if (node.getChild(part) == null) {
				node.addChild(new ConcurrentTreeNode<K, V>(part, node));
			}
			node = node.getChild(part);
		}

		node.setValue(value);
	}

	/**
	 * Removes the specified path.
	 * @param path
	 * @return value of the removed path
	 */
	public synchronized V remove(final K[] path) {
		ConcurrentTreeNode<K, V> node = getNode(path);
		if (node == null) {
			return null;
		}
		final V value = node.getValue();
		node.setValue(null);

		// Remove the nodes as long as they contain nor sub-nodes neither
		// value, and they have parent
		ConcurrentTreeNode<K, V> parent;
		while (node.getChildren().isEmpty() && (node.getValue() == null)
				&& ((parent = node.getParent()) != null)) {
			parent.removeChild(node.getKey());
			node = parent;
		}

		return value;
	}

	/**
	 * Gets the node of the specified path.
	 * @param path
	 * @return node element
	 */
	protected ConcurrentTreeNode<K, V> getNode(final K[] path) {
		ConcurrentTreeNode<K, V> node = this;
		for (final K part : path) {
			node = node.getChild(part);
			if (node == null) {
				return null;
			}
		}
		return node;
	}

	/**
	 * Gets the value of the specified path.
	 * @param path
	 * @return value
	 */
	public V get(final K[] path) {
		ConcurrentTreeNode<K, V> node = this;
		for (final K part : path) {
			node = node.getChild(part);
			if (node == null) {
				return null;
			}
		}
		return node.getValue();
	}

	/**
	 * Gets the values of all the nodes of the specified path. 
	 * @param path
	 * @return list of values, sorted from the longest to shortest path
	 */
	public List<V> getMatches(final K[] path) {
		final List<V> values = new LinkedList<V>();
		ConcurrentTreeNode<K, V> node = this;
		for (final K part : path) {
			if (node.getValue() != null) {
				values.add(node.getValue());
			}
			node = node.getChild(part);
			if (node == null) {
				break;
			}
		}
		if ((node != null) && (node.getValue() != null)) {
			values.add(node.getValue());
		}
		Collections.reverse(values);
		return values;
	}

	@Override
	public String toString() {
		return "[" + getChildren() + "]";
	}
}
