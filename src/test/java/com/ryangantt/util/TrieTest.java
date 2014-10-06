package com.ryangantt.util;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TrieTest {
  private Trie<Integer> trie;
  
  @BeforeMethod
  public void beforeMethod() {
    trie = new Trie<Integer>();
  }
  
  @Test
  public void insertionAndRetrieval() {
    trie.put("foo", 12);
    
    Assert.assertEquals(trie.get("foo"), Integer.valueOf(12));
  }
  
  @Test
  public void insertAndRetrieveWithCommonSubstring() {
    trie.put("foo", 12);
    trie.put("foofer", 11);
    
    Assert.assertEquals(trie.get("foofer"), Integer.valueOf(11));
    Assert.assertEquals(trie.get("foo"), Integer.valueOf(12));
  }
  
  @Test
  public void removal() {
    trie.put("foo", 12);
    trie.remove("foo");
    
    Assert.assertNull(trie.get("foo"));
  }
  
  @Test
  public void removalWithCommonSuperstring() {
    trie.put("foof", 10);
    trie.put("foofer", 12);
    Assert.assertEquals(trie.get("foof"), Integer.valueOf(10));
    
    trie.remove("foofer");
    Assert.assertEquals(trie.get("foof"), Integer.valueOf(10));
  }
  
  @Test
  public void removalWithCommonSubstring() {
    trie.put("foofer", 10);
    Assert.assertEquals(trie.get("foofer"), Integer.valueOf(10));
    
    trie.remove("foo");
    Assert.assertEquals(trie.get("foofer"), Integer.valueOf(10));
  }
  
  @Test(expectedExceptions = {RuntimeException.class})
  public void removeOnEmptyTrie() {
    trie.remove("foo");
  }
}