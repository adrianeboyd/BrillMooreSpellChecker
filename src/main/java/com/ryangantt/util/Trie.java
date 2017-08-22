package com.ryangantt.util;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

/**
 * Trie class adapted from: https://gist.github.com/rgantt/5711830
 * 
 * Related documentation:
 * http://code.ryangantt.com/articles/introduction-to-prefix-trees/
 *
 * @author Ryan Gantt 
 * @author Adriane Boyd
 */
public class Trie<V> {
	private Node root;

	public Trie() {
		root = new Node(null);
	}

	public class Node {
		private final String key;
		private V value;
		private Map<String, Node> children;
		private Node parent = null;
		private boolean terminated = false;
		
		public Node(final String key) {
			this(key, null);
		}

		public Node(final String key, final V value) {
			this(key, value, new TreeMap<String, Node>());
		}

		public Node(final String key, final V value, final Map<String, Node> children) {
			this.key = key;
			this.value = value;
			this.children = children;
		}

		public void addChild(final Node node) {
			node.parent = this;
			children.put(node.getKey(), node);
		}

		public Node findChild(final String key) {
			return children.get(key);
		}

		public void removeChild(final String key) {
			children.remove(key);
		}

		public String getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public Node getParent() {
			return parent;
		}

		public void setFinal(boolean terminated) {
			this.terminated = terminated;
		}

		public boolean isFinal() {
			return terminated;
		}

		public void setValue(final V value) {
			this.value = value;
		}

		public Collection<Node> getChildren() {
			return children.values();
		}

		public boolean isVapid() {
			return children.isEmpty() && (null == value);
		}

		public String toString() {
			return "[ " + this.key + " " + this.value + " [ " + this.children + " ] ]";
		}
	}

	public Deque<Node> pathFromLeaf(final Deque<Node> path, final String key) {
		final String k = String.valueOf(key.charAt(0));
		final Node child = path.peek().findChild(k);

		if ((null == child) && !k.equals(key)) {
			throw new RuntimeException("Key does not exist in trie");
		} else {
			path.push(child);
		}

		if (k.equals(key)) {
			return path;
		} else {
			return pathFromLeaf(path, key.substring(1));
		}
	}

	public void remove(final String key) {
		final Deque<Node> startingPath = new LinkedList<Node>();
		startingPath.push(root);
		remove(startingPath, key);
	}

	public void remove(final Deque<Node> startingPath, final String key) {
		final Deque<Node> path = pathFromLeaf(startingPath, key);
		Node current = path.pop(), parent;
		current.setValue(null);

		while (!path.isEmpty() && current.isVapid()) {
			parent = path.pop();
			parent.removeChild(current.getKey());
			current = parent;
		}
	}

	public String getPathFromRootToNode(final Node node) {
		if (node.parent != null) {
			return getPathFromRootToNode(node.parent) + node.getKey();
		}

		return "";
	}

	public V get(final String key) {
		return get(root, key);
	}

	public V get(final Node node, final String key) {
		final String k = String.valueOf(key.charAt(0));
		final Node matchingChild = node.findChild(k);

		if (null == matchingChild) {
			return null;
		} else if (k.equals(key)) {
			return matchingChild.getValue(); // could be null
		} else {
			return get(matchingChild, key.substring(1));
		}
	}

	public Node getNode(final String key) {
		return getNode(root, key);
	}

	public Node getNode(final Node node, final String key) {
		final String k = String.valueOf(key.charAt(0));
		final Node matchingChild = node.findChild(k);

		if (null == matchingChild) {
			return null;
		} else if (k.equals(key)) {
			return matchingChild; // could be null
		} else {
			return getNode(matchingChild, key.substring(1));
		}
	}

	public void put(final String key, final V value) {
		put(root, key, value);
	}

	public void put(final Node node, final String key, final V value) {
		final String k = String.valueOf(key.charAt(0));
		Node matchingChild = node.findChild(k);

		if (null == matchingChild) {
			matchingChild = new Node(k);
			node.addChild(matchingChild);
		}

		if (k.equals(key)) {
			matchingChild.setValue(value);
			matchingChild.setFinal(true);
		} else {
			put(matchingChild, key.substring(1), value);
		}
	}

	public Map<String, V> traverse() {
		return traverse(new TreeMap<String, V>(), "", root, false);
	}
	
	public Map<String, V> traverse(final boolean onlyTerminated) {
		return traverse(new TreeMap<String, V>(), "", root, onlyTerminated);
	}

	public Map<String, V> traverse(final Map<String, V> valueMap, final String prefix, final Node node, final boolean onlyTerminated) {
		if (onlyTerminated && node.isFinal()) {
			valueMap.put(prefix, node.getValue());
		} else if (!onlyTerminated && null != node.getValue()) {
			valueMap.put(prefix, node.getValue());
		}

		for (final Node child : node.getChildren()) {
			if ((null != child) && (null != child.getKey())) {
				traverse(valueMap, prefix + child.getKey(), child, onlyTerminated);
			}
		}
		return valueMap;
	}
	
	public void clearValues() {
		clearValues(root);
	}
	
	public void clearValues(final Node node) {
		node.setValue(null);
		
		for (final Node child : node.getChildren()) {
			if ((null != child) && (null != child.getKey())) {
				clearValues(child);
			}
		}
	}

	public Node getRoot() {
		return root;
	}
	
	public String toString() {
		return StringUtils.join(this.traverse(false), " ").concat("\n");
	}
}