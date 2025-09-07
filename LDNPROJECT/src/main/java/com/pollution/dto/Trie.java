package com.pollution.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Trie {
    private final TrieNode root = new TrieNode();

    public void insert(String siteName, String siteCode) {
        TrieNode current = root;
        for (char ch : siteName.toLowerCase().toCharArray()) {
            current = current.children.computeIfAbsent(ch, c -> new TrieNode());
        }
        current.isEndOfWord = true;
        current.siteCode = siteCode;
    }

    public String searchExact(String siteName) {
        TrieNode current = root;
        for (char ch : siteName.toLowerCase().toCharArray()) {
            current = current.children.get(ch);
            if (current == null) {
                return null;
            }
        }
        return current.isEndOfWord ? current.siteCode : null;
    }

    public void searchByPrefix(String prefix, StringBuilder currentPrefix, TrieNode node, List<String> results) {
        if (node.isEndOfWord) {
            results.add(currentPrefix.toString() + " (Site Code: " + node.siteCode + ")");
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            currentPrefix.append(entry.getKey());
            searchByPrefix(prefix, currentPrefix, entry.getValue(), results);
            currentPrefix.deleteCharAt(currentPrefix.length() - 1);
        }
    }

    public List<String> getSuggestions(String prefix) {
        TrieNode current = root;
        for (char ch : prefix.toLowerCase().toCharArray()) {
            current = current.children.get(ch);
            if (current == null) {
                return new ArrayList<>();
            }
        }
        List<String> results = new ArrayList<>();
        searchByPrefix(prefix, new StringBuilder(prefix.toLowerCase()), current, results);
        return results;
    }
}
