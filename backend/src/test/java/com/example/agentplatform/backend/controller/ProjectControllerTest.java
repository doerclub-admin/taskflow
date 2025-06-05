package com.example.agentplatform.backend.controller;

import com.example.agentplatform.backend.dto.ProjectAgentAssociationRequest;
import com.example.agentplatform.backend.dto.ProjectCreateRequest;
import com.example.agentplatform.backend.dto.ProjectResponse;
import com.example.agentplatform.backend.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProjectResponse projectResponse;
    private ProjectCreateRequest projectCreateRequest;

    @BeforeEach
    void setUp() {
        projectResponse = new ProjectResponse();
        projectResponse.setId(1L);
        projectResponse.setName("Test Project");
        projectResponse.setDescription("Test Description");
        projectResponse.setStatus("CREATED");
        projectResponse.setCreatedAt(LocalDateTime.now());
        projectResponse.setUpdatedAt(LocalDateTime.now());
        projectResponse.setAgents(new HashSet<>()); // Initialize agents set

        projectCreateRequest = new ProjectCreateRequest();
        projectCreateRequest.setName("New Project");
        projectCreateRequest.setDescription("New Desc");
    }

    @Test
    void createProject_success() throws Exception {
        when(projectService.createProject(any(ProjectCreateRequest.class))).thenReturn(projectResponse);

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Project")))
                .andExpect(jsonPath("$.status", is("CREATED")));
    }

    @Test
    void createProject_validationError() throws Exception {
        ProjectCreateRequest invalidRequest = new ProjectCreateRequest(); // Name is blank
        // GlobalExceptionHandler should catch this via @Valid on request body
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProjectById_success() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(projectResponse);

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Project")));
    }

    @Test
    void getAllProjects_success() throws Exception {
        ProjectResponse project2 = new ProjectResponse();
        project2.setId(2L);
        project2.setName("Test Project 2");
        ArrayList<ProjectResponse> projects = new ArrayList<>();
        projects.add(projectResponse);
        projects.add(project2);

        when(projectService.getAllProjects()).thenReturn(projects);

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Test Project")))
                .andExpect(jsonPath("$[1].name", is("Test Project 2")));
    }

    @Test
    void addAgentsToProject_success() throws Exception {
        Set<Long> agentIds = Collections.singleton(10L);
        ProjectAgentAssociationRequest associationRequest = new ProjectAgentAssociationRequest();
        associationRequest.setAgentIds(agentIds);

        ProjectResponse.SimpleAgentInfo agentInfo = new ProjectResponse.SimpleAgentInfo();
        agentInfo.setId(10L);
        agentInfo.setName("Agent X");
        projectResponse.setAgents(Collections.singleton(agentInfo)); // Simulate agent added in response

        when(projectService.addAgentsToProject(eq(1L), eq(agentIds))).thenReturn(projectResponse);

        mockMvc.perform(post("/api/projects/1/agents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(associationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.agents", hasSize(1)))
                .andExpect(jsonPath("$.agents[0].id", is(10)));
    }

    @Test
    void startProject_success() throws Exception {
        projectResponse.setStatus("RUNNING"); // Simulate status change
        when(projectService.startProject(1L)).thenReturn(projectResponse);

        mockMvc.perform(post("/api/projects/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("RUNNING")));
    }

    @Test
    void startProject_projectNotFound() throws Exception {
        when(projectService.startProject(anyLong())).thenThrow(new javax.persistence.EntityNotFoundException("Project not found"));

        mockMvc.perform(post("/api/projects/99/start"))
                .andExpect(status().isNotFound()); // Handled by GlobalExceptionHandler
    }


    // TODO: Add more tests for other ProjectController methods:
    // updateProject, deleteProject, removeAgentsFromProject, stopProject, pauseProject, getProjectStatus, getResourceUsage
}
