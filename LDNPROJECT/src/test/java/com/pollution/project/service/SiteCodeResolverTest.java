package com.pollution.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.pollution.dto.MonitoringSite;
import com.pollution.dto.Trie;  
import com.pollution.project.repository.AirQualitySnapshotRepository;

@ExtendWith(MockitoExtension.class)
class SiteCodeResolverTest {

    @Mock
    private AirQualitySnapshotRepository snapshotRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SiteCodeResolver siteCodeResolver;

    private MonitoringSite site1;
    private MonitoringSite site2;

    @BeforeEach
    void setup() {
        openMocks(this);
        Trie trie = new Trie();
        trie.insert("Newham - Hoola Tower", "TL5");
        siteCodeResolver.setSiteTrie(trie);

        site1 = new MonitoringSite();
        site1.setSiteName("Site One");
        site1.setSiteCode("S1");

        site2 = new MonitoringSite();
        site2.setSiteName("Site Two");
        site2.setSiteCode("S2");
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

    @Test
    void testLookupSiteCode_SpecialCharacters() {
        Trie trie = new Trie();
        trie.insert("- National Physical Laboratory, Teddington", "TD0");
        siteCodeResolver.setSiteTrie(trie);

        assertEquals("TD0", siteCodeResolver.lookupSiteCode("- National Physical Laboratory, Teddington"));
    }

    @Test
    void testLookupSiteCode_SubstringNotPrefix() {
        Trie trie = new Trie();
        trie.insert("Bexley West", "BQ8");
        siteCodeResolver.setSiteTrie(trie);

        assertNull(siteCodeResolver.lookupSiteCode("West"));
    }

    @Test
    void getSiteTrie_ShouldReturnNonNullTrie() {
        Trie trie = siteCodeResolver.getSiteTrie();
        assertEquals(1, trie.getSuggestions("Newham").size());
    }

    @Test
    void testGetSiteTrie_FirstCallPopulatesTrie() {
        MonitoringSite[] sites = {site1, site2};
        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenReturn(sites);

        Trie trie = siteCodeResolver.getSiteTrie();

        assertNotNull(trie);
        assertEquals("S1", trie.searchExact("Site One"));
        assertEquals("S2", trie.searchExact("Site Two"));

        verify(restTemplate, times(1)).getForObject(anyString(), eq(MonitoringSite[].class));
    }
}
