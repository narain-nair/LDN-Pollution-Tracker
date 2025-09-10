package com.pollution.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import static org.mockito.MockitoAnnotations.openMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pollution.dto.Trie;  

@ExtendWith(MockitoExtension.class)
class SiteCodeResolverTest {

    @InjectMocks
    private SiteCodeResolver siteCodeResolver;

    @BeforeEach
    void setup() {
        openMocks(this);
        Trie trie = new Trie();
        trie.insert("Newham - Hoola Tower", "TL5");
        siteCodeResolver.setSiteTrie(trie); 
        
    }

    @Test
    void testGetSiteCode_Found() {
        String siteName = "Newham - Hoola Tower";
        String expectedSiteCode = "TL5";
        String actualSiteCode = siteCodeResolver.lookupSiteCode(siteName);
        assertEquals(expectedSiteCode, actualSiteCode);
    }

    @Test
    void testGetSiteCode_NotFound() {
        String siteName = "Unknown Site";
        String actualSiteCode = siteCodeResolver.lookupSiteCode(siteName);
        assertNull(actualSiteCode);
    }  

    @Test
    void testLookupSiteCode_NullInput() {
        assertNull(siteCodeResolver.lookupSiteCode(null));
    }

    @Test
    void testLookupSiteCode_EmptyInput() {
        assertNull(siteCodeResolver.lookupSiteCode(""));
    }

    @Test
    void testLookupSiteCode_ExactMatch() {
        Trie trie = new Trie();
        trie.insert("Barking and Dagenham - North Street", "BG3");
        assertEquals("BG3", siteCodeResolver.lookupSiteCode("Barking and Dagenham - North Street"));
    }

    @Test
    void testLookupSiteCode_SuggestionUsed() {
        Trie trie = new Trie();
        trie.insert("Bexley West", "BQ8");
        assertEquals("BQ8", siteCodeResolver.lookupSiteCode("Bexley"));
    }

    @Test
    void testLookupSiteCode_NotFound() {
        assertNull(siteCodeResolver.lookupSiteCode("Unknown Site"));
    }

    @Test
    void testLookupSiteCode_MultipleSuggestions() {
        Trie trie = new Trie();
        trie.insert("Barking and Dagenham - North Street", "BG3");
        trie.insert("Barking and Dagenham - Rush Green", "BG1");
        siteCodeResolver.setSiteTrie(trie);

        String actualSiteCode = siteCodeResolver.lookupSiteCode("Barking and Dagenham");
        assertEquals("BG3", actualSiteCode);
    }

    @Test
    void testLookupSiteCode_SuggestionWithoutSiteCode() {
        Trie trie = new Trie();
        trie.insert("Test Site", null);
        siteCodeResolver.setSiteTrie(trie);

        assertNull(siteCodeResolver.lookupSiteCode("Test"));
    }

    @Test
    void testLookupSiteCode_CaseInsensitive() {
        Trie trie = new Trie();
        trie.insert("Newham - Hoola Tower", "TL5");
        siteCodeResolver.setSiteTrie(trie);

        assertEquals("TL5", siteCodeResolver.lookupSiteCode("newham - hoola tower"));
        assertEquals("TL5", siteCodeResolver.lookupSiteCode("NEWHAM - HOOLA TOWER"));
    }

    @Test
    void testLookupSiteCode_TrimInput() {
        Trie trie = new Trie();
        trie.insert("Newham - Hoola Tower", "TL5");
        siteCodeResolver.setSiteTrie(trie);

        assertEquals("TL5", siteCodeResolver.lookupSiteCode("  Newham - Hoola Tower  "));
    }
}
