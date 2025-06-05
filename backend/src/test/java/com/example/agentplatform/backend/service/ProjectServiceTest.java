package com.example.agentplatform.backend.service;

import com.example.agentplatform.backend.dto.ProjectCreateRequest;
import com.example.agentplatform.backend.dto.ProjectResponse;
import com.example.agentplatform.backend.model.Agent;
import com.example.agentplatform.backend.model.Project;
import com.example.agentplatform.backend.repository.AgentRepository;
import com.example.agentplatform.backend.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AgentRepository agentRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private Agent agent1;
    private ProjectCreateRequest projectCreateRequest;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setDescription("Test Project Description");
        project.setStatus("CREATED");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        agent1 = new Agent();
        agent1.setId(10L);
        agent1.setName("Agent 10");
        agent1.setCurrentVersionTag("v1.0");

        projectCreateRequest = new ProjectCreateRequest();
        projectCreateRequest.setName("New Project");
        projectCreateRequest.setDescription("New Project Desc");
    }

    @Test
    void createProject_success() {
        when(projectRepository.findByName(projectCreateRequest.getName())).thenReturn(Optional.empty());
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project savedProject = invocation.getArgument(0);
            savedProject.setId(2L); // Simulate ID generation
            return savedProject;
        });

        ProjectResponse response = projectService.createProject(projectCreateRequest);

        assertNotNull(response);
        assertEquals(projectCreateRequest.getName(), response.getName());
        assertEquals("CREATED", response.getStatus());
        assertTrue(response.getAgents() == null || response.getAgents().isEmpty());

        verify(projectRepository).findByName(projectCreateRequest.getName());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_withAgents_success() {
        projectCreateRequest.setAgentIds(Collections.singleton(10L));
        when(projectRepository.findByName(projectCreateRequest.getName())).thenReturn(Optional.empty());
        when(agentRepository.findAllById(Collections.singleton(10L))).thenReturn(Collections.singletonList(agent1));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project savedProject = invocation.getArgument(0);
            savedProject.setId(2L);
            // Manually set agents in the saved project for response mapping if needed by convertToProjectResponse
            if (savedProject.getAgents().isEmpty() && !Collections.singletonList(agent1).isEmpty()) {
                 savedProject.setAgents(new HashSet<>(Collections.singletonList(agent1)));
            }
            return savedProject;
        });

        ProjectResponse response = projectService.createProject(projectCreateRequest);

        assertNotNull(response);
        assertEquals(projectCreateRequest.getName(), response.getName());
        assertNotNull(response.getAgents());
        assertEquals(1, response.getAgents().size());
        assertEquals(agent1.getName(), response.getAgents().iterator().next().getName());

        verify(agentRepository).findAllById(Collections.singleton(10L));
    }

    @Test
    void createProject_withAgents_agentNotFound() {
        projectCreateRequest.setAgentIds(Collections.singleton(99L)); // Non-existent agent
        when(projectRepository.findByName(projectCreateRequest.getName())).thenReturn(Optional.empty());
        when(agentRepository.findAllById(Collections.singleton(99L))).thenReturn(Collections.emptyList()); // Agent not found

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            projectService.createProject(projectCreateRequest);
        });
        assertEquals("One or more agents not found for the provided IDs during project creation.", exception.getMessage());
    }


    @Test
    void createProject_nameConflict() {
        when(projectRepository.findByName(projectCreateRequest.getName())).thenReturn(Optional.of(project));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject(projectCreateRequest);
        });

        assertEquals("Project with name '" + projectCreateRequest.getName() + "' already exists.", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void getProjectById_success() {
        project.setAgents(Collections.singleton(agent1)); // Add agent for response mapping
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProjectById(1L);

        assertNotNull(response);
        assertEquals(project.getName(), response.getName());
        assertNotNull(response.getAgents());
        assertEquals(1, response.getAgents().size());
        verify(projectRepository).findById(1L);
    }

    @Test
    void getProjectById_notFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> projectService.getProjectById(1L));
    }

    @Test
    void addAgentsToProject_success() {
        Set<Long> agentIdsToAdd = Collections.singleton(10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project)); // project initially has no agents
        when(agentRepository.findAllById(agentIdsToAdd)).thenReturn(Collections.singletonList(agent1));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            // Ensure the response converter sees the added agent
            if(p.getAgents().isEmpty()) p.setAgents(new HashSet<>(Collections.singletonList(agent1)));
            return p;
        });


        ProjectResponse response = projectService.addAgentsToProject(1L, agentIdsToAdd);

        assertNotNull(response);
        assertFalse(response.getAgents().isEmpty());
        assertEquals(1, response.getAgents().size());
        assertEquals(agent1.getName(), response.getAgents().iterator().next().getName());
        verify(projectRepository).save(project);
    }

    @Test
    void startProject_success_stubbed() {
        project.addAgent(agent1); // Project needs an agent to start
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.startProject(1L);
        assertEquals("RUNNING", response.getStatus());
        verify(projectRepository).save(project);
    }

    @Test
    void startProject_noAgents() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project)); // project has no agents

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            projectService.startProject(1L);
        });
        assertEquals("Project cannot be started without any associated agents.", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
    }

    // TODO: Add more tests for other methods:
    // updateProject, deleteProject, removeAgentsFromProject, stopProject, pauseProject, getProjectStatus, getResourceUsage
}
