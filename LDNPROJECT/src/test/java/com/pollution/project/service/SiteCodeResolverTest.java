package com.pollution.project.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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
import com.pollution.dto.MonitoringSiteResponse;
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
        MonitoringSiteResponse response = new MonitoringSiteResponse();
        response.setMonitoringSites(new MonitoringSite[]{site1, site2});
        when(restTemplate.getForObject(anyString(), eq(MonitoringSiteResponse.class))).thenReturn(response);
    
        // Act
        siteCodeResolver.setSiteTrie(null); // Reset to force re-fetch
        Trie retrievedTrie = siteCodeResolver.getSiteTrie();
        logger.info("Retrieved Trie: {}", retrievedTrie);
        logger.info("Result for 'Site One': {}", retrievedTrie.searchExact("Site One"));
        logger.info("Result for 'Site Two': {}", retrievedTrie.searchExact("Site Two"));
    
        // Assert
        assertEquals("S1", retrievedTrie.searchExact("Site One"));
        assertEquals("S2", retrievedTrie.searchExact("Site Two"));
        verify(restTemplate, times(1)).getForObject(anyString(), eq(MonitoringSiteResponse.class));
    }

    @Test
    void testGetSiteTrie_SubsequentCallsReturnSameInstance() {
        MonitoringSiteResponse response = new MonitoringSiteResponse();
        response.setMonitoringSites(new MonitoringSite[]{site1, site2});
        when(restTemplate.getForObject(anyString(), eq(MonitoringSiteResponse.class))).thenReturn(response);

        // reset cached trie if needed
        ReflectionTestUtils.setField(siteCodeResolver, "siteTrie", null);

        Trie firstCall = siteCodeResolver.getSiteTrie();
        Trie secondCall = siteCodeResolver.getSiteTrie();

        assertSame(firstCall, secondCall);

        // should now be invoked once
        verify(restTemplate, times(1)).getForObject(anyString(), eq(MonitoringSiteResponse.class));
    }

    @Test
    void testRefreshSiteTrie_SuccessfulUpdate() {
        // Arrange
        MonitoringSite siteA = new MonitoringSite("Site A", "SA1");
        MonitoringSite siteB = new MonitoringSite("Site B", "SB2");
        MonitoringSite[] newSites = {siteA, siteB};
    
        MonitoringSiteResponse response = new MonitoringSiteResponse();
        response.setMonitoringSites(newSites);

        when(restTemplate.getForObject(anyString(), eq(MonitoringSiteResponse.class))).thenReturn(response);
    
        // Act
        siteCodeResolver.refreshSiteTrie();
    
        // Assert
        Trie trie = siteCodeResolver.getSiteTrie();

        logger.info("Trie searchExact for 'Site A': {}", trie.searchExact("Site A"));
        logger.info("Trie searchExact for 'Site B': {}", trie.searchExact("Site B"));
        
        assertEquals("SA1", trie.searchExact("Site A"));
        assertEquals("SB2", trie.searchExact("Site B"));
    
        verify(restTemplate, times(1)).getForObject(anyString(), eq(MonitoringSiteResponse.class));
    }

    @Test
    void testRefreshSiteTrie_ApiThrowsException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(MonitoringSiteResponse.class))).thenThrow(new RestClientException("API down"));

        // Act → should NOT throw
        assertDoesNotThrow(() -> siteCodeResolver.refreshSiteTrie());

        // Assert → trie should remain null (or unchanged if previously set)
        Trie trie = siteCodeResolver.getSiteTrie();
        assertTrue(trie.getSuggestions("Anything").isEmpty());

        // Verify API call attempted once
        verify(restTemplate, times(1)).getForObject(anyString(), eq(MonitoringSiteResponse.class));
    }

    @Test
    void testRefreshSiteTrie_ReplacesOldTrie() {
        // Arrange: initial trie with old data
        Trie initialTrie = new Trie();
        initialTrie.insert("Old Site", "OLD1");
        siteCodeResolver.setSiteTrie(initialTrie);

        // Mock API with new data
        MonitoringSite siteC = new MonitoringSite("Fresh Site", "FS1");
        MonitoringSiteResponse response = new MonitoringSiteResponse();
        response.setMonitoringSites(new MonitoringSite[]{siteC});

        when(restTemplate.getForObject(anyString(), eq(MonitoringSiteResponse.class))).thenReturn(response);

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

        MonitoringSiteResponse response = new MonitoringSiteResponse();
        response.setMonitoringSites(new MonitoringSite[]{siteA, siteB});

        when(restTemplate.getForObject(anyString(), eq(MonitoringSiteResponse.class))).thenReturn(response);

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
    
        MonitoringSite[] sites = { siteA, siteB };
        MonitoringSiteResponse response = new MonitoringSiteResponse();
        response.setMonitoringSites(sites);
    
        // Stub RestTemplate to return the wrapper instead of raw array
        when(restTemplate.getForObject(anyString(), eq(MonitoringSiteResponse.class))).thenReturn(response);
    
        // Act
        String nearestCode = siteCodeResolver.calculateSiteCode(51.505, 0.101);
    
        // Assert
        assertEquals("B", nearestCode); // site A skipped due to invalid latitude
    }

    @Test
    void testCalculateSiteCode_NoSitesReturned() {
        // Arrange
        MonitoringSiteResponse emptyResponse = new MonitoringSiteResponse();
        emptyResponse.setMonitoringSites(null); 
        logger.info("Empty response monitoring sites: {}", emptyResponse.getMonitoringSites());

        // stub restTemplate to return the wrapper instead of the raw array
        when(restTemplate.getForObject(anyString(), eq(MonitoringSiteResponse.class))).thenReturn(emptyResponse);
        logger.info("Stubbed restTemplate to return empty response");

        // Act
        String nearestCode = siteCodeResolver.calculateSiteCode(51.505, 0.101);
        logger.info("Nearest code calculated: {}", nearestCode);

        // Assert
        assertNull(nearestCode);
    }
    
    @Test
    void testCalculateSiteCode_EmptySitesArray() {
        // Arrange
        MonitoringSiteResponse response = new MonitoringSiteResponse();
        response.setMonitoringSites(new MonitoringSite[0]);
        when(restTemplate.getForObject(anyString(), eq(MonitoringSiteResponse.class))).thenReturn(response);

        // Act
        String nearestCode = siteCodeResolver.calculateSiteCode(51.505, 0.101);

        // Assert
        assertNull(nearestCode);
    }

    @Test
    void testAssignSiteCode_InputMatchesSite() {
        Location location = new Location(51.5, 0.1);

        // Spy the resolver
        SiteCodeResolver spyResolver = Mockito.spy(siteCodeResolver);

        // Stub lookupSiteCode properly for a spy
        doReturn("S1").when(spyResolver).lookupSiteCode("Known Site");

        // Act
        spyResolver.assignSiteCode(location, "Known Site");

        // Assert
        assertEquals("S1", location.getSiteCode());
    }

    @Test
    void testAssignSiteCode_FallbackToCalculate() {
        Location location = new Location(51.5, 0.1);
        Trie emptyTrie = new Trie(); // no matching sites
        ReflectionTestUtils.setField(siteCodeResolver, "siteTrie", emptyTrie);

        MonitoringSiteResponse fallbackResponse = new MonitoringSiteResponse();
        MonitoringSite fallbackSite = new MonitoringSite();
        fallbackSite.setSiteCode("S2");
        fallbackSite.setLatitude("51.5");
        fallbackSite.setLongitude("0.1");
        fallbackResponse.setMonitoringSites(new MonitoringSite[]{ fallbackSite });
 
        when(restTemplate.getForObject(anyString(), eq(MonitoringSiteResponse.class)))
            .thenReturn(fallbackResponse);

        // Optional: spy to verify calculateSiteCode was called
        SiteCodeResolver spyResolver = spy(siteCodeResolver);
        doCallRealMethod().when(spyResolver).calculateSiteCode(anyDouble(), anyDouble());

        spyResolver.assignSiteCode(location, "Unknown Site");

        // Verify fallback was actually called
        verify(spyResolver).calculateSiteCode(51.5, 0.1);
        assertEquals("S2", location.getSiteCode(), "Fallback site code should have been assigned");
    }

    @Test
    void testAssignSiteCode_BothFail() {
        Location location = new Location(51.5, 0.1);
    
        // create a wrapper with null or empty array
        MonitoringSiteResponse emptyResponse = new MonitoringSiteResponse();
        emptyResponse.setMonitoringSites(new MonitoringSite[0]);
    
        // stub restTemplate to return the wrapper instead of the raw array
        when(restTemplate.getForObject(anyString(), eq(MonitoringSiteResponse.class))).thenReturn(emptyResponse);
    
        siteCodeResolver.assignSiteCode(location, "Unknown Site");
    
        assertNull(location.getSiteCode());
    }

    @Test
    void testPopulateLocationData_ValidResponse() {
        // Dummy location (latitude/longitude arbitrary)
        Location location = new Location("Dummy Location", 51.0, 0.0);
        location.setId(1L);
        location.setSiteCode("DUMMY1");  // skip lookup/calculate

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
        lenient().when(restTemplate.getForObject(anyString(), eq(HourlyIndexResponse.class))).thenReturn(response);

        // Act
        SiteCodeResolver spyResolver = Mockito.spy(siteCodeResolver);
        doAnswer(invocation -> {Location loc = invocation.getArgument(0); loc.setSiteCode("DUMMY1"); // ensure non-null site code
            return null;
        }).when(spyResolver).assignSiteCode(any(Location.class), anyString());
        spyResolver.populateLocationData(location, "Dummy Location");

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

        lenient().when(restTemplate.getForObject(anyString(), eq(HourlyIndexResponse.class))).thenReturn(null);
        
        SiteCodeResolver spyResolver = Mockito.spy(siteCodeResolver);
        doAnswer(invocation -> {Location loc = invocation.getArgument(0); loc.setSiteCode(null); // ensure null site code
            return null;
        }).when(spyResolver).assignSiteCode(any(Location.class), anyString());
        spyResolver.populateLocationData(location, "Dummy Location");

        assertNull(location.getAirQualityData());
    }

    @Test
    void testPopulateLocationData_InvalidIndex() {
        // Arrange
        Location location = new Location("Dummy Location", 51.000, 0.000);
        location.setId(1L);  // non-null id

        // Species with invalid index
        HourlyIndexResponse.Species pm25 = new HourlyIndexResponse.Species("PM25", "PM25", "N/A", "Moderate", "Automated");
        HourlyIndexResponse.Site site = new HourlyIndexResponse.Site("51.000", "0.000", "DUMMY1", "Dummy Location", 
                                                                    "2025-09-10 12:00:00", List.of(pm25));
        HourlyIndexResponse.LocalAuthority la = new HourlyIndexResponse.LocalAuthority("Dummy Authority", "99", "51.000", "0.000", site);
        HourlyIndexResponse.HourlyAirQualityIndex hqi = new HourlyIndexResponse.HourlyAirQualityIndex("60", la);
        HourlyIndexResponse response = new HourlyIndexResponse(hqi);

        lenient().when(restTemplate.getForObject(anyString(), eq(HourlyIndexResponse.class))).thenReturn(response);

        // Spy the resolver so we can stub assignSiteCode
        SiteCodeResolver spyResolver = Mockito.spy(siteCodeResolver);
        doAnswer(invocation -> {Location loc = invocation.getArgument(0); loc.setSiteCode("DUMMY1"); // ensure non-null site code
            return null;
        }).when(spyResolver).assignSiteCode(any(Location.class), anyString());

        // Act
        spyResolver.populateLocationData(location, "Dummy Location");

        // Logging for debugging
        logger.info("Mocked Response: {}", response);
        logger.info("Species List: {}", response.getHourlyAirQualityIndex().getLocalAuthority().getSite().getSpecies());
        logger.info("Air quality data: {}", location.getAirQualityData());

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
    
        // Mock RestTemplate to return no data, so populateLocationData sets no AirQualityData
        MonitoringSiteResponse emptyResponse = new MonitoringSiteResponse();
        emptyResponse.setMonitoringSites(new MonitoringSite[0]); // empty array
        when(restTemplate.getForObject(anyString(), eq(MonitoringSiteResponse.class))).thenReturn(emptyResponse);
    
        siteCodeResolver.refreshLocationData(location);
    
        // Verify snapshotRepository.save is never called
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
