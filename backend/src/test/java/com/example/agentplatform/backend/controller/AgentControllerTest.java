package com.example.agentplatform.backend.controller;

import com.example.agentplatform.backend.dto.AgentCreateRequest;
import com.example.agentplatform.backend.dto.AgentResponse;
import com.example.agentplatform.backend.dto.AgentVersionResponse;
import com.example.agentplatform.backend.service.AgentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;


@WebMvcTest(AgentController.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentService agentService;

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON strings

    private AgentResponse agentResponse;
    private AgentCreateRequest agentCreateRequest;

    @BeforeEach
    void setUp() {
        agentResponse = new AgentResponse();
        agentResponse.setId(1L);
        agentResponse.setName("Test Agent");
        agentResponse.setDescription("Test Description");
        agentResponse.setCurrentVersionTag("v1.0");
        agentResponse.setCreatedAt(LocalDateTime.now());
        agentResponse.setUpdatedAt(LocalDateTime.now());

        AgentVersionResponse versionResponse = new AgentVersionResponse();
        versionResponse.setId(1L);
        versionResponse.setAgentId(1L);
        versionResponse.setVersionTag("v1.0");
        versionResponse.setActive(true);
        agentResponse.setVersions(Collections.singletonList(versionResponse));

        agentCreateRequest = new AgentCreateRequest();
        agentCreateRequest.setName("New Agent");
        agentCreateRequest.setDescription("New Description");
        agentCreateRequest.setInitialVersionTag("v1.0");
    }

    @Test
    void createAgent_success() throws Exception {
        when(agentService.createAgent(any(AgentCreateRequest.class))).thenReturn(agentResponse);

        mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(agentCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Agent")))
                .andExpect(jsonPath("$.currentVersionTag", is("v1.0")));
    }

    @Test
    void createAgent_validationError() throws Exception {
        AgentCreateRequest invalidRequest = new AgentCreateRequest(); // Missing name
        invalidRequest.setDescription("Description without name");
        invalidRequest.setInitialVersionTag("v1.0");
        // Note: GlobalExceptionHandler should handle MethodArgumentNotValidException

        mockMvc.perform(post("/api/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Assuming GlobalExceptionHandler is active
    }

    @Test
    void getAgentById_success() throws Exception {
        when(agentService.getAgentById(1L)).thenReturn(agentResponse);

        mockMvc.perform(get("/api/agents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Agent")));
    }

    @Test
    void getAgentById_notFound() throws Exception {
        when(agentService.getAgentById(anyLong())).thenThrow(new javax.persistence.EntityNotFoundException("Agent not found"));
        // Note: GlobalExceptionHandler should handle EntityNotFoundException

        mockMvc.perform(get("/api/agents/99"))
                .andExpect(status().isNotFound()); // Assuming GlobalExceptionHandler is active
    }

    @Test
    void getAllAgents_success() throws Exception {
        AgentResponse agent2 = new AgentResponse();
        agent2.setId(2L);
        agent2.setName("Test Agent 2");
        ArrayList<AgentResponse> agents = new ArrayList<>();
        agents.add(agentResponse);
        agents.add(agent2);

        when(agentService.getAllAgents()).thenReturn(agents);

        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Test Agent")))
                .andExpect(jsonPath("$[1].name", is("Test Agent 2")));
    }

    // TODO: Add more tests for other AgentController methods:
    // updateAgent, deleteAgent, createAgentVersion, getAgentVersions, getAgentVersionByTag, setActiveAgentVersion
}
