package com.pollution.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.Mockito.verify;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pollution.dto.MonitoringSite;
import com.pollution.dto.Trie;

class SiteCodeResolverTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SiteCodeResolver siteCodeResolver;

    @BeforeEach
    void setUp() {
        openMocks(this);
        Trie trie = new Trie();
        trie.insert("Newham - Hoola Tower", "TL5");
        siteCodeResolver.setSiteTrie(trie); 
        
    }
}
