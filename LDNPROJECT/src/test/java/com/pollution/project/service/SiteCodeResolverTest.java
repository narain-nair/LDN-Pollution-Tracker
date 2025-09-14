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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
        String mockJson = """
        {
            "Sites": {
                "Site": [
                    {"@SiteName":"Site One","@SiteCode":"S1"},
                    {"@SiteName":"Site Two","@SiteCode":"S2"}
                ]
            }
        }
        """;
    
        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);
    
        // Act
        siteCodeResolver.setSiteTrie(null); // Reset to force re-fetch
        Trie retrievedTrie = siteCodeResolver.getSiteTrie();
    
        // Logging (optional)
        logger.info("Retrieved Trie: {}", retrievedTrie);
        logger.info("Result for 'Site One': {}", retrievedTrie.searchExact("Site One"));
        logger.info("Result for 'Site Two': {}", retrievedTrie.searchExact("Site Two"));
    
        // Assert
        assertEquals("S1", retrievedTrie.searchExact("Site One"));
        assertEquals("S2", retrievedTrie.searchExact("Site Two"));
    
        // Verify
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(String.class));
    }
    
    @Test
    void testGetSiteTrie_SubsequentCallsReturnSameInstance() {
        // Arrange
        String mockJson = """
        {
            "Sites": {
                "Site": [
                    {"@SiteName":"Site One","@SiteCode":"S1"},
                    {"@SiteName":"Site Two","@SiteCode":"S2"}
                ]
            }
        }
        """;
        
        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);
        
        // reset cached trie if needed
        ReflectionTestUtils.setField(siteCodeResolver, "siteTrie", null);

        Trie firstCall = siteCodeResolver.getSiteTrie();
        Trie secondCall = siteCodeResolver.getSiteTrie();

        assertSame(firstCall, secondCall);

        // should now be invoked once
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(MonitoringSiteResponse.class));
    }

    @Test
    void testRefreshSiteTrie_SuccessfulUpdate() throws JsonProcessingException {
        // Arrange
        MonitoringSite siteA = new MonitoringSite();
        siteA.setSiteName("Site A");
        siteA.setSiteCode("SA1");

        logger.info("Created MonitoringSite: {}", siteA);
    
        MonitoringSite siteB = new MonitoringSite();
        siteB.setSiteName("Site B");
        siteB.setSiteCode("SB2");
        
        logger.info("Created MonitoringSite: {}", siteB);

        MonitoringSite[] newSites = {siteA, siteB};
        logger.info("New MonitoringSites array: {}", Arrays.toString(newSites));

        MonitoringSiteResponse response = new MonitoringSiteResponse();
        response.setMonitoringSites(newSites);
        logger.info("MonitoringSiteResponse to be returned by mock: {}", response);

        String mockJson = """
        {
            "Sites": {
                "Site": 
                [
                    {"@SiteName":"Site A","@SiteCode":"SA1"},
                    {"@SiteName":"Site B","@SiteCode":"SB2"}
                ]
            }
        }
        """;
        
        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);
        // Act
        siteCodeResolver.refreshSiteTrie();

        logger.info("Site trie after refresh: {}", siteCodeResolver.getSiteTrie().toString());
    
        // Assert
        Trie trie = siteCodeResolver.getSiteTrie();
        assertEquals("SA1", trie.searchExact("Site A"));
        assertEquals("SB2", trie.searchExact("Site B"));

        verify(restTemplate, times(1)).getForEntity(anyString(), eq(String.class));
    }

    @Test
    void testRefreshSiteTrie_ApiThrowsException() {
        // Arrange: make the API throw
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
            .thenThrow(new RestClientException("API down"));
    
        // Optional: ensure trie is null before test
        siteCodeResolver.setSiteTrie(null);
    
        // Act → should NOT throw
        assertDoesNotThrow(() -> siteCodeResolver.refreshSiteTrie());
    
        // Assert → trie should remain null or empty
        Trie trie = siteCodeResolver.getSiteTrie();
        assertNotNull(trie); // getSiteTrie() will lazily initialize, even if API fails
        assertTrue(trie.getSuggestions("Anything").isEmpty());
    
        // Verify API call attempted once
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(String.class));
    }

    @Test
    void testRefreshSiteTrie_ReplacesOldTrie() {
        // Arrange: initial trie with old data
        Trie initialTrie = new Trie();
        initialTrie.insert("Old Site", "OLD1");
        siteCodeResolver.setSiteTrie(initialTrie);
    
        // Mock API with new JSON data
        String mockJson = """
        {
            "Sites": {
                "Site": [
                    {"@SiteName":"Fresh Site","@SiteCode":"FS1"}
                ]
            }
        }
        """;
        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);
    
        // Act
        try {
            siteCodeResolver.refreshSiteTrie();
        } catch (JsonProcessingException e) {
            logger.info("JsonProcessingException caught: {}", e.getMessage());
            e.printStackTrace();
        }
    
        // Assert
        Trie refreshedTrie = siteCodeResolver.getSiteTrie();
        assertEquals("FS1", refreshedTrie.searchExact("Fresh Site"));
        assertNull(refreshedTrie.searchExact("Old Site")); // Old site should be gone
    }
    
    @Test
    void testCalculateSiteCode_NearestSiteReturned() {
        // Arrange: JSON string for sites
        String mockJson = """
        {
            "Sites": {
                "Site": [
                    {"@SiteName":"A","@Latitude":"51.500","@Longitude":"0.100","@SiteCode":"A"},
                    {"@SiteName":"B","@Latitude":"52.000","@Longitude":"0.200","@SiteCode":"B"}
                ]
            }
        }
        """;
    
        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);
    
        // Act
        String nearestCode = siteCodeResolver.calculateSiteCode(51.505, 0.101); // closer to site A
    
        // Assert
        assertEquals("A", nearestCode);
    }
    
    @Test
    void testCalculateSiteCode_InvalidCoordinatesSkipped() {
        // Arrange
        String mockJson = """
        {
            "Sites": {
                "Site": [
                    {"@SiteName":"A","@SiteCode":"A","@Latitude":"not-a-number","@Longitude":"0.100"},
                    {"@SiteName":"B","@SiteCode":"B","@Latitude":"51.500","@Longitude":"0.100"}
                ]
            }
        }
        """;

        // stub restTemplate to return the wrapper instead of the raw array
        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);    
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);

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
        
        String mockJson = """
        {
            "Sites": {
                "Site": null
            }
        }
        """;
        
        // stub restTemplate to return the wrapper instead of the raw array
        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);    
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);
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
        String mockJson = """
        {
            "Sites": {
                "Site": []
            }
        }
        """;

        // stub restTemplate to return the wrapper instead of the raw array
        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);    
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);

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

        String mockJson = """
        {
            "Sites": {
                "Site": [
                    {"@SiteCode":"S2","@Latitude":"51.5","@Longitude":"0.1"}
                ]
            }
        }
        """;

        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);    
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);
 
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
        logger.info("Testing assignSiteCode with location: {}", location);
    
        // create a wrapper with null or empty array
        MonitoringSiteResponse emptyResponse = new MonitoringSiteResponse();
        emptyResponse.setMonitoringSites(new MonitoringSite[0]);

        String mockJson = """
        {
            "Sites": {
                "Site": []
            }
        }
        """;

        // stub restTemplate to return the wrapper instead of the raw array
        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);    
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);
    
        siteCodeResolver.assignSiteCode(location, "Unknown Site");
    
        assertNull(location.getSiteCode());
    }

    @Test
    void testPopulateLocationData_ValidResponse() throws JsonProcessingException {
        Location location = new Location("Dummy Location", 51.0, 0.0);
        location.setId(1L);
        location.setSiteCode("DUMMY1");  // skip lookup/calculate
    
        // JSON representing valid PM25/NO2 indices
        String mockJson = """
        {
            "HourlyAirQualityIndex": {
                "LocalAuthority": {
                    "LocalAuthorityName": "Dummy Authority",
                    "LocalAuthorityCode": "99",
                    "Latitude": "51.000",
                    "Longitude": "0.000",
                    "Site": {
                        "@Latitude": "51.000",
                        "@Longitude": "0.000",
                        "@SiteCode": "DUMMY1",
                        "@SiteName": "Dummy Location",
                        "@BulletinDate": "2025-09-10 12:00:00",
                        "Species": [
                            {"@Code":"PM25","@Name":"PM25","@Index":"5","@Band":"Moderate","@Method":"Automated"},
                            {"@Code":"NO2","@Name":"NO2","@Index":"3","@Band":"Low","@Method":"Automated"}
                        ]
                    }
                }
            }
        }
        """;
    
        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);
    
        // Spy resolver to stub assignSiteCode
        SiteCodeResolver spyResolver = Mockito.spy(siteCodeResolver);
        doAnswer(invocation -> {
            Location loc = invocation.getArgument(0);
            loc.setSiteCode("DUMMY1");
            return null;
        }).when(spyResolver).assignSiteCode(any(Location.class), anyString());
    
        // Act
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
        try {
            spyResolver.populateLocationData(location, "Dummy Location");
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            logger.info("JsonProcessingException caught: {}", e.getMessage());
            e.printStackTrace();
        }

        assertNull(location.getAirQualityData());
    }

    @Test
    void testPopulateLocationData_InvalidIndex() throws JsonProcessingException {
        // Arrange
        Location location = new Location("Dummy Location", 51.000, 0.000);
        location.setId(1L);  // non-null id

        // JSON representing invalid PM25 index
        String mockJson = """
        {
            "HourlyAirQualityIndex": {
                "LocalAuthority": {
                    "LocalAuthorityName": "Dummy Authority",
                    "LocalAuthorityCode": "99",
                    "Latitude": "51.000",
                    "Longitude": "0.000",
                    "Site": {
                        "@Latitude": "51.000",
                        "@Longitude": "0.000",
                        "@SiteCode": "DUMMY1",
                        "@SiteName": "Dummy Location",
                        "@BulletinDate": "2025-09-10 12:00:00",
                        "Species": [
                            {"@Code":"PM25","@Name":"PM25","@Index":"N/A","@Band":"Moderate","@Method":"Automated"}
                        ]
                    }
                }
            }
        }
        """;

        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);

        // Spy the resolver to stub assignSiteCode so we have a non-null siteCode
        SiteCodeResolver spyResolver = Mockito.spy(siteCodeResolver);
        doAnswer(invocation -> {
            Location loc = invocation.getArgument(0);
            loc.setSiteCode("DUMMY1"); // ensure non-null site code
            return null;
        }).when(spyResolver).assignSiteCode(any(Location.class), anyString());

        // Act
        spyResolver.populateLocationData(location, "Dummy Location");

        // Assert
        assertNotNull(location.getAirQualityData());
        assertNull(location.getAirQualityData().getPm25()); // invalid index handled
        assertEquals("DUMMY1", location.getSiteCode());
        assertEquals("Dummy Location", location.getName());
    }

    @Test
    void testRefreshLocationData_SnapshotSaved() throws JsonProcessingException {
        // Arrange
        Location location = new Location(51.5, 0.1);
        
        String mockJson = """
        {
            "Sites": {
                "Site": [
                    {"@SiteName":"Site A","@SiteCode":"SA1"}
                ]
            }
        }
        """;
    
        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);
    
        // Act
        siteCodeResolver.refreshLocationData(location);
    
        // Assert
        verify(snapshotRepository, times(1)).save(any(AirQualitySnapshot.class));
    }
    @Test
    void testRefreshLocationData_NoAirQualityData() throws JsonProcessingException {
        Location location = new Location(51.5, 0.1);
    
        // Mock RestTemplate to return a JSON with empty sites array
        String mockJson = """
        {
            "Sites": {
                "Site": []
            }
        }
        """;
    
        ResponseEntity<String> fakeResponse = new ResponseEntity<>(mockJson, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(fakeResponse);
    
        // Act
        siteCodeResolver.refreshLocationData(location);
    
        // Assert
        // AirQualityData should remain null since there was no site info
        assertNull(location.getAirQualityData());
    
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
