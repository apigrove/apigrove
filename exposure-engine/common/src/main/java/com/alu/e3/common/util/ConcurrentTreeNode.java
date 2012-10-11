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

import java.util.HashMap;
import java.util.Map;

/**
 * Node of a tree, storing one object.
 * It is thread-safe, as the add/remove operations override volatile objects. 
 *
 * @param <K> type of the path element 
 * @param <V> type of the value element
 */
public class ConcurrentTreeNode<K, V> {
	private final K key;
	private final ConcurrentTreeNode<K, V> parent;
	private volatile V value;
	private volatile Map<K, ConcurrentTreeNode<K, V>> children;

	protected ConcurrentTreeNode() {
		this.key = null;
		this.parent = null;
	    this.children = new HashMap<K, ConcurrentTreeNode<K, V>>(0);
	}

	protected ConcurrentTreeNode(final K key, final ConcurrentTreeNode<K, V> parent) {
		this.key = key;
		this.parent = parent;
	    this.children = new HashMap<K, ConcurrentTreeNode<K, V>>(0);
	}

	/**
	 * Adds a child node.
	 * @param child
	 */
	protected void addChild(final ConcurrentTreeNode<K, V> child) {
		final Map<K, ConcurrentTreeNode<K, V>> children =
				new HashMap<K, ConcurrentTreeNode<K, V>>((this.children.size() + 1) * 2);
		children.putAll(this.children);
		children.put(child.key, child);
		this.children = children;
	}

	/**
	 * Gets a child identified by the key.
	 * @param key
	 * @return child node
	 */
	protected ConcurrentTreeNode<K, V> getChild(final K key) {
		return this.children.get(key);
	}

	/**
	 * Removes a child identified by the key.
	 * @param key
	 */
	protected void removeChild(final K key) {
		final Map<K, ConcurrentTreeNode<K, V>> children =
				new HashMap<K, ConcurrentTreeNode<K, V>>(this.children);
		children.remove(key);
		if (children.isEmpty()) {
			this.children = new HashMap<K, ConcurrentTreeNode<K, V>>(0);
		} else {
			this.children = children;
		}
	}

	/**
	 * Gets the key.
	 * @return the last element of the node's path
	 */
	public K getKey() {
		return key;
	}

	/**
	 * Gets the parent node.
	 * @return the parent node
	 */
	public ConcurrentTreeNode<K, V> getParent() {
		return parent;
	}

	/**
	 * Gets the value stored by the node.
	 * @return the value
	 */
	public V getValue() {
		return value;
	}
	
	/**
	 * Sets the value stored by the node.
	 * @param value
	 */
	protected void setValue(final V value) {
		this.value = value;
	}

	/**
	 * Gets the children of the node.
	 * @return map of keys and related children
	 */
	public Map<K, ConcurrentTreeNode<K, V>> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return key + " : " + value + " [" + children + "]";
	}
}
