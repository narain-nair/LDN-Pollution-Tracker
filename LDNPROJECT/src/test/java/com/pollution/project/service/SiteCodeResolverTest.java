package com.pollution.project.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.web.client.RestClientException;
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

    @Test
    void testGetSiteTrie_SubsequentCallsReturnSameInstance() {
        MonitoringSite[] sites = {site1};
        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenReturn(sites);

        Trie firstCall = siteCodeResolver.getSiteTrie();
        Trie secondCall = siteCodeResolver.getSiteTrie();

        assertSame(firstCall, secondCall);

        verify(restTemplate, times(1)).getForObject(anyString(), eq(MonitoringSite[].class));
    }

    @Test
    void testRefreshSiteTrie_SuccessfulUpdate() {
        // Arrange
        MonitoringSite siteA = new MonitoringSite("Site A", "SA1");
        MonitoringSite siteB = new MonitoringSite("Site B", "SB2");

        MonitoringSite[] newSites = {siteA, siteB};
        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenReturn(newSites);

        // Act
        siteCodeResolver.refreshSiteTrie();

        // Assert
        Trie trie = siteCodeResolver.getSiteTrie();
        assertEquals("SA1", trie.searchExact("Site A"));
        assertEquals("SB2", trie.searchExact("Site B"));

        // API should have been called once
        verify(restTemplate, times(1)).getForObject(anyString(), eq(MonitoringSite[].class));
    }

    @Test
    void testRefreshSiteTrie_ApiThrowsException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenThrow(new RestClientException("API down"));

        // Act → should NOT throw
        assertDoesNotThrow(() -> siteCodeResolver.refreshSiteTrie());

        // Assert → trie should remain null (or unchanged if previously set)
        Trie trie = siteCodeResolver.getSiteTrie();
        assertTrue(trie.getSuggestions("Anything").isEmpty());

        // Verify API call attempted once
        verify(restTemplate, times(1)).getForObject(anyString(), eq(MonitoringSite[].class));
    }

    @Test
    void testRefreshSiteTrie_ReplacesOldTrie() {
        // Arrange: initial trie with old data
        Trie initialTrie = new Trie();
        initialTrie.insert("Old Site", "OLD1");
        siteCodeResolver.setSiteTrie(initialTrie);

        // Mock API with new data
        MonitoringSite siteC = new MonitoringSite("Fresh Site", "FS1");
        MonitoringSite[] refreshedSites = {siteC};

        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenReturn(refreshedSites);

        // Act
        siteCodeResolver.refreshSiteTrie();

        // Assert
        Trie refreshedTrie = siteCodeResolver.getSiteTrie();
        assertEquals("FS1", refreshedTrie.searchExact("Fresh Site"));
        assertNull(refreshedTrie.searchExact("Old Site")); // Old site should be gone
    }

    @Test
    void testCalculateSiteCode_NearestSiteReturned() {
        // Arrange
        MonitoringSite siteA = new MonitoringSite("A", "51.500", "0.100");
        MonitoringSite siteB = new MonitoringSite("B", "52.000", "0.200");

        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenReturn(new MonitoringSite[]{siteA, siteB});

        // Act
        String nearestCode = siteCodeResolver.calculateSiteCode(51.505, 0.101); // closer to site A

        // Assert
        assertEquals("A", nearestCode);
    }


    @Test
    void testCalculateSiteCode_InvalidCoordinatesSkipped() {
        // Arrange
        MonitoringSite siteA = new MonitoringSite("A", "not-a-number", "0.100");
        MonitoringSite siteB = new MonitoringSite("B", "51.500", "0.100");

        MonitoringSite[] sites = {siteA, siteB};

        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenReturn(sites);

        // Act
        String nearestCode = siteCodeResolver.calculateSiteCode(51.505, 0.101);

        // Assert
        assertEquals("B", nearestCode); // site A skipped
    }
}
