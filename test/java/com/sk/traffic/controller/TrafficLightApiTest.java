package com.sk.traffic.controller;

import com.sk.traffic.domain.model.*;
import com.sk.traffic.domain.model.ControllerStatus;
import com.sk.traffic.domain.model.Intersection;
import com.sk.traffic.domain.service.TrafficLightService;
import com.sk.traffic.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TrafficLightApi.class)
public class TrafficLightApiTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TrafficLightService service;

    @Test
    @DisplayName("Create intersection returns 201")
    void createIntersection() throws Exception {
        Intersection isect = new Intersection("abc");
        Mockito.when(service.createIntersection(Mockito.any())).thenReturn(isect);

        mvc.perform(post("/api/traffic/intersections").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("abc")));
    }

    @Test
    @DisplayName("State endpoint returns status and map")
    void stateEndpoint() throws Exception {
        Intersection isect = new Intersection("abc");
        isect.setStatus(ControllerStatus.PAUSED);
        Mockito.when(service.get("abc")).thenReturn(isect);

        mvc.perform(get("/api/traffic/intersections/abc/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intersectionId", is("abc")))
                .andExpect(jsonPath("$.status", is("PAUSED")));
    }
}
