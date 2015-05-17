package de.hpi.dbda;

import java.util.HashSet;

import org.apache.spark.api.java.function.PairFlatMapFunction;

import scala.Tuple2;
import de.hpi.dbda.trie.InnerTrieNode;
import de.hpi.dbda.trie.TrieLeaf;
import de.hpi.dbda.trie.TrieNode;

public class CandidateMatcherTrie implements PairFlatMapFunction<IntArray, Integer, Integer> {
	private static final long serialVersionUID = -5812427046006186493L;
	InnerTrieNode trie;

	public CandidateMatcherTrie(InnerTrieNode trie) {
		this.trie = trie;
	}

	public Iterable<Tuple2<Integer, Integer>> call(IntArray transactionWrapper) throws Exception {
		HashSet<Tuple2<Integer, Integer>> result = new HashSet<Tuple2<Integer, Integer>>();

		int[] transaction = transactionWrapper.value;
		traverseTrie(transaction, 0, trie, result);
		return result;
	}

	private void traverseTrie(int[] transaction, int processedElements, InnerTrieNode trieNode, HashSet<Tuple2<Integer, Integer>> resultSet) {
		for (int labelIndex = 0; labelIndex < trieNode.edgeLabels.length; labelIndex++) {
			while (transaction[processedElements] < trieNode.edgeLabels[labelIndex]) {
				processedElements++;
				if (processedElements == transaction.length) {
					return;
				}
			}

			if (transaction[processedElements] == trieNode.edgeLabels[labelIndex]) {
				TrieNode childNode = trieNode.children[labelIndex];
				if (childNode instanceof TrieLeaf) {
					resultSet.add(new Tuple2<Integer, Integer>(((TrieLeaf) childNode).value, 1));
				} else {
					if (processedElements + 1 < transaction.length) { // At least one more transaction element must be matched against the trie. If no transaction element is left, this is impossible.
						traverseTrie(transaction, processedElements + 1, (InnerTrieNode) childNode, resultSet);
					}
				}
			}
		}
	}

}
