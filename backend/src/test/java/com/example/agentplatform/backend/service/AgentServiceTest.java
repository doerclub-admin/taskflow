package com.example.agentplatform.backend.service;

import com.example.agentplatform.backend.dto.AgentCreateRequest;
import com.example.agentplatform.backend.dto.AgentResponse;
import com.example.agentplatform.backend.dto.AgentUpdateRequest;
import com.example.agentplatform.backend.dto.AgentVersionCreateRequest;
import com.example.agentplatform.backend.dto.AgentVersionResponse;
import com.example.agentplatform.backend.model.Agent;
import com.example.agentplatform.backend.model.AgentVersion;
import com.example.agentplatform.backend.repository.AgentRepository;
import com.example.agentplatform.backend.repository.AgentVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AgentVersionRepository agentVersionRepository;

    @InjectMocks
    private AgentService agentService;

    private Agent agent;
    private AgentVersion agentVersion;
    private AgentCreateRequest agentCreateRequest;

    @BeforeEach
    void setUp() {
        agent = new Agent();
        agent.setId(1L);
        agent.setName("Test Agent");
        agent.setDescription("Test Description");
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        agent.setCurrentVersionTag("v1.0");

        agentVersion = new AgentVersion();
        agentVersion.setId(1L);
        agentVersion.setAgent(agent);
        agentVersion.setVersionTag("v1.0");
        agentVersion.setActive(true);
        agentVersion.setCreatedAt(LocalDateTime.now());
        agentVersion.setCapabilitiesDescription("Capabilities");
        agentVersion.setModelDependencies("Dependencies");
        agentVersion.setConfigurationDetails("Config");

        // Set the versions list in agent
        List<AgentVersion> versions = new ArrayList<>();
        versions.add(agentVersion);
        agent.setVersions(versions);


        agentCreateRequest = new AgentCreateRequest();
        agentCreateRequest.setName("New Agent");
        agentCreateRequest.setDescription("New Description");
        agentCreateRequest.setInitialVersionTag("v1.0");
        agentCreateRequest.setCapabilitiesDescription("Caps");
        agentCreateRequest.setModelDependencies("Deps");
        agentCreateRequest.setConfigurationDetails("Conf");
    }

    @Test
    void createAgent_success() {
        when(agentRepository.findByName(agentCreateRequest.getName())).thenReturn(Optional.empty());
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> {
            Agent savedAgent = invocation.getArgument(0);
            savedAgent.setId(2L); // Simulate ID generation
            return savedAgent;
        });
        when(agentVersionRepository.save(any(AgentVersion.class))).thenAnswer(invocation -> {
            AgentVersion savedVersion = invocation.getArgument(0);
            savedVersion.setId(2L); // Simulate ID generation
            // Link back to agent if necessary for the test response mapping
            Agent parentAgent = savedVersion.getAgent();
            if(parentAgent.getVersions() == null || parentAgent.getVersions().isEmpty()){
                 parentAgent.setVersions(List.of(savedVersion));
            }
            return savedVersion;
        });
        // Mock the second save for agent (updating currentVersionTag)
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));


        AgentResponse response = agentService.createAgent(agentCreateRequest);

        assertNotNull(response);
        assertEquals(agentCreateRequest.getName(), response.getName());
        assertEquals(agentCreateRequest.getInitialVersionTag(), response.getCurrentVersionTag());
        assertNotNull(response.getVersions());
        assertFalse(response.getVersions().isEmpty());
        assertEquals(agentCreateRequest.getInitialVersionTag(), response.getVersions().get(0).getVersionTag());
        assertTrue(response.getVersions().get(0).isActive());

        verify(agentRepository).findByName(agentCreateRequest.getName());
        verify(agentRepository, times(2)).save(any(Agent.class)); // Called twice: once for agent, once for version tag update
        verify(agentVersionRepository).save(any(AgentVersion.class));
    }

    @Test
    void createAgent_nameConflict() {
        when(agentRepository.findByName(agentCreateRequest.getName())).thenReturn(Optional.of(agent));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            agentService.createAgent(agentCreateRequest);
        });

        assertEquals("Agent with name '" + agentCreateRequest.getName() + "' already exists.", exception.getMessage());
        verify(agentRepository).findByName(agentCreateRequest.getName());
        verify(agentRepository, never()).save(any(Agent.class));
        verify(agentVersionRepository, never()).save(any(AgentVersion.class));
    }

    @Test
    void createAgent_derivedAgentNotFound() {
        agentCreateRequest.setDerivedFromAgentId(99L);
        when(agentRepository.findByName(agentCreateRequest.getName())).thenReturn(Optional.empty());
        when(agentRepository.findById(99L)).thenReturn(Optional.empty());
        // Simulate the first save of the agent before derived check
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));


        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            agentService.createAgent(agentCreateRequest);
        });

        assertEquals("Derived from Agent with ID 99 not found.", exception.getMessage());
        verify(agentRepository).findById(99L);
    }

    @Test
    void getAgentById_success() {
        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(agentVersionRepository.findByAgent(agent)).thenReturn(Collections.singletonList(agentVersion));

        AgentResponse response = agentService.getAgentById(1L);

        assertNotNull(response);
        assertEquals(agent.getName(), response.getName());
        assertEquals(agentVersion.getVersionTag(), response.getCurrentVersionTag());
        verify(agentRepository).findById(1L);
        verify(agentVersionRepository).findByAgent(agent);
    }

    @Test
    void getAgentById_notFound() {
        when(agentRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            agentService.getAgentById(1L);
        });

        assertEquals("Agent with ID 1 not found.", exception.getMessage());
        verify(agentRepository).findById(1L);
    }

    @Test
    void updateAgent_success() {
        AgentUpdateRequest updateRequest = new AgentUpdateRequest();
        updateRequest.setName("Updated Agent Name");
        updateRequest.setDescription("Updated Description");

        Agent existingAgent = new Agent();
        existingAgent.setId(1L);
        existingAgent.setName("Old Agent Name");

        // Agent in response needs versions
        List<AgentVersion> versions = new ArrayList<>();
        AgentVersion v = new AgentVersion();
        v.setId(1L); v.setAgent(existingAgent); v.setVersionTag("v1.0"); v.setActive(true);
        versions.add(v);
        existingAgent.setVersions(versions);
        existingAgent.setCurrentVersionTag("v1.0");


        when(agentRepository.findById(1L)).thenReturn(Optional.of(existingAgent));
        when(agentRepository.findByName("Updated Agent Name")).thenReturn(Optional.empty());
        when(agentRepository.save(any(Agent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(agentVersionRepository.findByAgent(any(Agent.class))).thenReturn(versions);


        AgentResponse response = agentService.updateAgent(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Agent Name", response.getName());
        assertEquals("Updated Description", response.getDescription());
        verify(agentRepository).findById(1L);
        verify(agentRepository).findByName("Updated Agent Name");
        verify(agentRepository).save(any(Agent.class));
    }

    @Test
    void updateAgent_nameConflict() {
        AgentUpdateRequest updateRequest = new AgentUpdateRequest();
        updateRequest.setName("Existing Name");

        Agent agentToUpdate = new Agent();
        agentToUpdate.setId(1L);
        agentToUpdate.setName("Original Name");

        Agent conflictingAgent = new Agent();
        conflictingAgent.setId(2L);
        conflictingAgent.setName("Existing Name");

        when(agentRepository.findById(1L)).thenReturn(Optional.of(agentToUpdate));
        when(agentRepository.findByName("Existing Name")).thenReturn(Optional.of(conflictingAgent));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            agentService.updateAgent(1L, updateRequest);
        });
        assertEquals("Agent with name 'Existing Name' already exists.", exception.getMessage());
    }


    @Test
    void createAgentVersion_success() {
        AgentVersionCreateRequest versionRequest = new AgentVersionCreateRequest();
        versionRequest.setVersionTag("v1.1");
        versionRequest.setCapabilitiesDescription("New Caps");
        versionRequest.setMakeActive(true);

        when(agentRepository.findById(1L)).thenReturn(Optional.of(agent));
        when(agentVersionRepository.findByAgentAndVersionTag(agent, "v1.1")).thenReturn(Optional.empty());
        // Mock finding the currently active version to deactivate it
        when(agentVersionRepository.findByAgentAndActive(agent, true)).thenReturn(Optional.of(agentVersion));
        when(agentVersionRepository.save(any(AgentVersion.class))).thenAnswer(invocation -> {
            AgentVersion newVersion = invocation.getArgument(0);
            newVersion.setId(2L); // Simulate ID generation
            newVersion.setAgent(agent); // Ensure agent is set for response conversion
            return newVersion;
        });
        // Mock save for agent (to update currentVersionTag)
        when(agentRepository.save(any(Agent.class))).thenReturn(agent);


        AgentVersionResponse versionResponse = agentService.createAgentVersion(1L, versionRequest);

        assertNotNull(versionResponse);
        assertEquals("v1.1", versionResponse.getVersionTag());
        assertTrue(versionResponse.isActive());
        assertEquals("New Caps", versionResponse.getCapabilitiesDescription());

        verify(agentVersionRepository).save(agentVersion); // Saving old active version (to deactivate)
        verify(agentVersionRepository).save(any(AgentVersion.class)); // Saving new version
        verify(agentRepository).save(agent); // Saving agent to update currentVersionTag
        assertEquals("v1.1", agent.getCurrentVersionTag()); // Verify agent's current version tag was updated
    }

    // TODO: Add more tests for other methods:
    // deleteAgent, getAllAgents, getAgentByName, getAgentVersions, getAgentVersionByTag, setActiveAgentVersion
}
