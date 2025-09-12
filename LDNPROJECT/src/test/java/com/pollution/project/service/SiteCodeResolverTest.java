package com.pollution.project.service;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pollution.dto.HourlyIndexResponse;
import com.pollution.dto.MonitoringSite;
import com.pollution.dto.Trie;
import com.pollution.project.entity.AirQualityData;
import com.pollution.project.entity.AirQualitySnapshot;
import com.pollution.project.entity.Location;
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

    private static final Logger logger = LoggerFactory.getLogger(SiteCodeResolverTest.class);
    
    @BeforeEach
    void setup() {
        openMocks(this);
        siteCodeResolver = new SiteCodeResolver(restTemplate, snapshotRepository);

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
        siteCodeResolver.setSiteTrie(trie);

        assertEquals("BG3", siteCodeResolver.lookupSiteCode("Barking and Dagenham - North Street"));
    }

    @Test
    void testLookupSiteCode_SuggestionUsed() {
        Trie trie = new Trie();
        trie.insert("Bexley West", "BQ8");
        siteCodeResolver.setSiteTrie(trie);

        logger.info("Inserted 'Bexley West' with code 'BQ8' into trie.");
        

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
        // Arrange
        MonitoringSite[] sites = {site1, site2};
        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenReturn(sites);
    
        // Act
        siteCodeResolver.setSiteTrie(null); // Reset to force re-fetch
        Trie retrievedTrie = siteCodeResolver.getSiteTrie();
        logger.info("Retrieved Trie: {}", retrievedTrie);
        logger.info("Result for 'Site One': {}", retrievedTrie.searchExact("Site One"));
        logger.info("Result for 'Site Two': {}", retrievedTrie.searchExact("Site Two"));
    
        // Assert
        assertEquals("S1", retrievedTrie.searchExact("Site One"));
        assertEquals("S2", retrievedTrie.searchExact("Site Two"));
        verify(restTemplate, times(1)).getForObject(anyString(), eq(MonitoringSite[].class));
    }

    @Test
    void testGetSiteTrie_SubsequentCallsReturnSameInstance() {
        MonitoringSite[] sites = {site1};
        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenReturn(sites);

        // reset cached trie if needed
        ReflectionTestUtils.setField(siteCodeResolver, "siteTrie", null);

        Trie firstCall = siteCodeResolver.getSiteTrie();
        Trie secondCall = siteCodeResolver.getSiteTrie();

        assertSame(firstCall, secondCall);

        // should now be invoked once
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

        logger.info("Trie searchExact for 'Site A': {}", trie.searchExact("Site A"));
        logger.info("Trie searchExact for 'Site B': {}", trie.searchExact("Site B"));
        
        assertEquals("SA1", trie.searchExact("Site A"));
        assertEquals("SB2", trie.searchExact("Site B"));
    
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

    @Test
    void testCalculateSiteCode_NoSitesReturned() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenReturn(null);

        // Act
        String nearestCode = siteCodeResolver.calculateSiteCode(51.505, 0.101);

        // Assert
        assertNull(nearestCode);
    }
    
    @Test
    void testCalculateSiteCode_EmptySitesArray() {
        // Arrange
        MonitoringSite[] emptySites = {};
        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenReturn(emptySites);

        // Act
        String nearestCode = siteCodeResolver.calculateSiteCode(51.505, 0.101);

        // Assert
        assertNull(nearestCode);
    }

    @Test
    void testAssignSiteCode_InputMatchesSite() {
        Location location = new Location(51.5, 0.1);
        when(siteCodeResolver.lookupSiteCode("Known Site")).thenReturn("S1");

        siteCodeResolver.assignSiteCode(location, "Known Site");

        assertEquals("S1", location.getSiteCode());
    }

    @Test
    void testAssignSiteCode_FallbackToCalculate() {
        Location location = new Location(51.5, 0.1);
    
        // stub RestTemplate to return empty for lookup
        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenReturn(new MonitoringSite[]{ new MonitoringSite("S2", "51.5", "0.1") });
    
        // Call real method
        siteCodeResolver.assignSiteCode(location, "Unknown Site");
    
        // Assert the calculated fallback code was set
        assertEquals("S2", location.getSiteCode());
    }

    @Test
    void testAssignSiteCode_BothFail() {
        Location location = new Location(51.5, 0.1);

        // stub restTemplate so lookupSiteCode and calculateSiteCode fail gracefully
        when(restTemplate.getForObject(anyString(), eq(MonitoringSite[].class))).thenReturn(null);

        siteCodeResolver.assignSiteCode(location, "Unknown Site");

        assertNull(location.getSiteCode());
    }

    @Test
    void testPopulateLocationData_ValidResponse() {
        // Dummy location (latitude/longitude arbitrary)
        Location location = new Location("Dummy Location", 51.000, 0.000);

        // Build species list
        HourlyIndexResponse.Species pm25 = new HourlyIndexResponse.Species("PM25", "PM25", "5", "Moderate", "Automated");
        HourlyIndexResponse.Species no2 = new HourlyIndexResponse.Species("NO2", "NO2", "3", "Low", "Automated");

        // Build Site
        HourlyIndexResponse.Site site = new HourlyIndexResponse.Site("51.000", "0.000", "DUMMY1", "Dummy Location", "2025-09-10 12:00:00", List.of(pm25, no2));

        // Build LocalAuthority
        HourlyIndexResponse.LocalAuthority la = new HourlyIndexResponse.LocalAuthority("Dummy Authority", "99", "51.000", "0.000", site);

        // Build HourlyAirQualityIndex
        HourlyIndexResponse.HourlyAirQualityIndex hqi = new HourlyIndexResponse.HourlyAirQualityIndex("60", la);

        // Build Response
        HourlyIndexResponse response = new HourlyIndexResponse(hqi);

        // Mock API call
        when(restTemplate.getForObject(anyString(), eq(HourlyIndexResponse.class))).thenReturn(response);

        // Mock assignSiteCode (skip actual lookup logic)
        doNothing().when(siteCodeResolver).assignSiteCode(location, "Dummy Location");

        // Act
        siteCodeResolver.populateLocationData(location, "Dummy Location");

        // Assert
        assertNotNull(location.getAirQualityData());
        assertEquals(5.0, location.getAirQualityData().getPm25());
        assertEquals(3.0, location.getAirQualityData().getNo2());
        assertEquals("DUMMY1", location.getSiteCode());
        assertEquals("Dummy Location", location.getName());
    }

    @Test
    void testPopulateLocationData_ReturnNull() {
        Location location = new Location("Dummy Location", 51.000, 0.000);
        location.setSiteCode(null);
        doNothing().when(siteCodeResolver).assignSiteCode(location, "X");

        when(restTemplate.getForObject(anyString(), eq(HourlyIndexResponse.class))).thenReturn(null);

        siteCodeResolver.populateLocationData(location, "X");
        assertNull(location.getAirQualityData());
    }

    @Test
    void testPopulateLocationData_InvalidIndex() {
        Location location = new Location("Dummy Location", 51.000, 0.000);

        // Species with invalid index
        HourlyIndexResponse.Species pm25 = new HourlyIndexResponse.Species("PM25", "PM25", "N/A", "Moderate", "Automated");

        HourlyIndexResponse.Site site = new HourlyIndexResponse.Site("51.000", "0.000", "DUMMY1", "Dummy Location", "2025-09-10 12:00:00",List.of(pm25));

        HourlyIndexResponse.LocalAuthority la = new HourlyIndexResponse.LocalAuthority("Dummy Authority", "99", "51.000", "0.000", site);

        HourlyIndexResponse.HourlyAirQualityIndex hqi = new HourlyIndexResponse.HourlyAirQualityIndex("60", la);

        HourlyIndexResponse response = new HourlyIndexResponse(hqi);

        when(restTemplate.getForObject(anyString(), eq(HourlyIndexResponse.class))).thenReturn(response);

        doNothing().when(siteCodeResolver).assignSiteCode(location, "Dummy Location");

        // Act
        siteCodeResolver.populateLocationData(location, "Dummy Location");

        // Assert
        assertNotNull(location.getAirQualityData());
        assertNull(location.getAirQualityData().getPm25()); // invalid index handled
        assertEquals("DUMMY1", location.getSiteCode());
        assertEquals("Dummy Location", location.getName());
    }

    @Test
    void testRefreshLocationData_SnapshotSaved() {
        Location location = new Location(51.5, 0.1);
        AirQualityData airData = new AirQualityData(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, LocalDateTime.now());
        location.setAirQualityData(airData);

        siteCodeResolver.refreshLocationData(location);
        verify(snapshotRepository, times(1)).save(any(AirQualitySnapshot.class));
    }

    @Test
    void testRefreshLocationData_NoAirQualityData() {
        Location location = new Location(51.5, 0.1);

        doNothing().when(siteCodeResolver).populateLocationData(eq(location), anyString());

        siteCodeResolver.refreshLocationData(location);

        verify(snapshotRepository, never()).save(any());
    }

    @Test
    void testGetIndex_ValidSpecies() {
        HourlyIndexResponse.Species pm25 = new HourlyIndexResponse.Species("PM25", "PM25", "5", "Moderate", "Automated");
        HourlyIndexResponse.Species no2 = new HourlyIndexResponse.Species("NO2", "NO2", "3", "Low", "Automated");
        List<HourlyIndexResponse.Species> speciesList = List.of(pm25, no2);

        assertEquals(5.0, siteCodeResolver.getIndex(speciesList, "PM25"));
        assertEquals(3.0, siteCodeResolver.getIndex(speciesList, "NO2"));
    }

    @Test
    void testGetIndex_SpeciesNotFound() {
        HourlyIndexResponse.Species pm25 = new HourlyIndexResponse.Species("PM25", "PM25", "5", "Moderate", "Automated");
        List<HourlyIndexResponse.Species> speciesList = List.of(pm25);

        assertNull(siteCodeResolver.getIndex(speciesList, "NO2"));
    }

    @Test
    void testGetIndex_InvalidIndexValue() {
        HourlyIndexResponse.Species pm25 = new HourlyIndexResponse.Species("PM25", "PM25", "N/A", "Moderate", "Automated");
        List<HourlyIndexResponse.Species> speciesList = List.of(pm25);

        assertNull(siteCodeResolver.getIndex(speciesList, "PM25"));
    }
}
