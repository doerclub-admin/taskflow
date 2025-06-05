package com.example.agentplatform.backend.service;

import com.example.agentplatform.backend.dto.*;
import com.example.agentplatform.backend.model.Agent;
import com.example.agentplatform.backend.model.AgentVersion;
import com.example.agentplatform.backend.repository.AgentRepository;
import com.example.agentplatform.backend.repository.AgentVersionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgentService {

    private final AgentRepository agentRepository;
    private final AgentVersionRepository agentVersionRepository;

    public AgentService(AgentRepository agentRepository, AgentVersionRepository agentVersionRepository) {
        this.agentRepository = agentRepository;
        this.agentVersionRepository = agentVersionRepository;
    }

    @Transactional
    public AgentResponse createAgent(AgentCreateRequest request) {
        if (agentRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Agent with name '" + request.getName() + "' already exists.");
        }

        Agent agent = new Agent();
        agent.setName(request.getName());
        agent.setDescription(request.getDescription());

        if (request.getDerivedFromAgentId() != null) {
            Agent parentAgent = agentRepository.findById(request.getDerivedFromAgentId())
                    .orElseThrow(() -> new EntityNotFoundException("Derived from Agent with ID " + request.getDerivedFromAgentId() + " not found."));
            agent.setDerivedFromAgent(parentAgent);
        }

        // Save agent first to get ID for AgentVersion
        Agent savedAgent = agentRepository.save(agent);

        // Create initial version
        AgentVersion initialVersion = new AgentVersion();
        initialVersion.setAgent(savedAgent);
        initialVersion.setVersionTag(request.getInitialVersionTag());
        initialVersion.setModelDependencies(request.getModelDependencies());
        initialVersion.setCapabilitiesDescription(request.getCapabilitiesDescription());
        initialVersion.setConfigurationDetails(request.getConfigurationDetails());
        initialVersion.setActive(true); // First version is active by default
        agentVersionRepository.save(initialVersion);

        savedAgent.setCurrentVersionTag(initialVersion.getVersionTag());
        agentRepository.save(savedAgent); // Update agent with current version tag

        return convertToAgentResponse(savedAgent, List.of(initialVersion));
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> getAllAgents() {
        return agentRepository.findAll().stream()
                .map(agent -> {
                    List<AgentVersion> versions = agentVersionRepository.findByAgent(agent);
                    return convertToAgentResponse(agent, versions);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AgentResponse getAgentById(Long id) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agent with ID " + id + " not found."));
        List<AgentVersion> versions = agentVersionRepository.findByAgent(agent);
        return convertToAgentResponse(agent, versions);
    }

    @Transactional(readOnly = true)
    public Optional<AgentResponse> getAgentByName(String name) {
        return agentRepository.findByName(name)
                .map(agent -> {
                    List<AgentVersion> versions = agentVersionRepository.findByAgent(agent);
                    return convertToAgentResponse(agent, versions);
                });
    }

    @Transactional
    public AgentResponse updateAgent(Long id, AgentUpdateRequest request) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agent with ID " + id + " not found."));

        if (request.getName() != null && !request.getName().equals(agent.getName())) {
            if(agentRepository.findByName(request.getName()).isPresent()){
                 throw new IllegalArgumentException("Agent with name '" + request.getName() + "' already exists.");
            }
            agent.setName(request.getName());
        }
        if (request.getDescription() != null) {
            agent.setDescription(request.getDescription());
        }

        Agent updatedAgent = agentRepository.save(agent);
        List<AgentVersion> versions = agentVersionRepository.findByAgent(updatedAgent);
        return convertToAgentResponse(updatedAgent, versions);
    }

    @Transactional
    public void deleteAgent(Long id) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agent with ID " + id + " not found."));
        // Versions are deleted by cascade if configured on Agent entity, otherwise:
        // agentVersionRepository.deleteAll(agentVersionRepository.findByAgent(agent));
        agentRepository.delete(agent);
    }

    @Transactional
    public AgentVersionResponse createAgentVersion(Long agentId, AgentVersionCreateRequest request) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new EntityNotFoundException("Agent with ID " + agentId + " not found."));

        if (agentVersionRepository.findByAgentAndVersionTag(agent, request.getVersionTag()).isPresent()) {
            throw new IllegalArgumentException("Version '" + request.getVersionTag() + "' already exists for agent '" + agent.getName() + "'.");
        }

        AgentVersion newVersion = new AgentVersion();
        newVersion.setAgent(agent);
        newVersion.setVersionTag(request.getVersionTag());
        newVersion.setModelDependencies(request.getModelDependencies());
        newVersion.setCapabilitiesDescription(request.getCapabilitiesDescription());
        newVersion.setConfigurationDetails(request.getConfigurationDetails());

        if (request.isMakeActive()) {
            agentVersionRepository.findByAgentAndActive(agent, true)
                .ifPresent(activeVersion -> {
                    activeVersion.setActive(false);
                    agentVersionRepository.save(activeVersion);
                });
            newVersion.setActive(true);
            agent.setCurrentVersionTag(newVersion.getVersionTag());
            agentRepository.save(agent);
        } else {
            newVersion.setActive(false);
        }

        AgentVersion savedVersion = agentVersionRepository.save(newVersion);
        return convertToAgentVersionResponse(savedVersion);
    }

    @Transactional(readOnly = true)
    public List<AgentVersionResponse> getAgentVersions(Long agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new EntityNotFoundException("Agent with ID " + agentId + " not found."));
        return agentVersionRepository.findByAgent(agent).stream()
                .map(this::convertToAgentVersionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AgentVersionResponse getAgentVersionByTag(Long agentId, String versionTag) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new EntityNotFoundException("Agent with ID " + agentId + " not found."));
        return agentVersionRepository.findByAgentAndVersionTag(agent, versionTag)
                .map(this::convertToAgentVersionResponse)
                .orElseThrow(() -> new EntityNotFoundException("Version '" + versionTag + "' not found for agent ID " + agentId));
    }

    @Transactional
    public AgentVersionResponse setActiveAgentVersion(Long agentId, String versionTag) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new EntityNotFoundException("Agent with ID " + agentId + " not found."));

        AgentVersion versionToActivate = agentVersionRepository.findByAgentAndVersionTag(agent, versionTag)
                .orElseThrow(() -> new EntityNotFoundException("Version '" + versionTag + "' not found for agent ID " + agentId));

        agentVersionRepository.findByAgentAndActive(agent, true)
            .filter(currentActive -> !currentActive.getId().equals(versionToActivate.getId()))
            .ifPresent(currentActive -> {
                currentActive.setActive(false);
                agentVersionRepository.save(currentActive);
            });

        versionToActivate.setActive(true);
        AgentVersion updatedVersion = agentVersionRepository.save(versionToActivate);

        agent.setCurrentVersionTag(updatedVersion.getVersionTag());
        agentRepository.save(agent);

        return convertToAgentVersionResponse(updatedVersion);
    }


    // --- Helper Conversion Methods ---
    private AgentResponse convertToAgentResponse(Agent agent, List<AgentVersion> versions) {
        AgentResponse response = new AgentResponse();
        BeanUtils.copyProperties(agent, response);
        if (agent.getDerivedFromAgent() != null) {
            response.setDerivedFromAgentId(agent.getDerivedFromAgent().getId());
            response.setDerivedFromAgentName(agent.getDerivedFromAgent().getName());
        }
        if (versions != null) {
            response.setVersions(versions.stream().map(this::convertToAgentVersionResponse).collect(Collectors.toList()));
        }
        // Ensure currentVersionTag is from the agent entity itself, which should be kept up-to-date
        response.setCurrentVersionTag(agent.getCurrentVersionTag());
        return response;
    }

    private AgentVersionResponse convertToAgentVersionResponse(AgentVersion version) {
        AgentVersionResponse response = new AgentVersionResponse();
        BeanUtils.copyProperties(version, response);
        response.setAgentId(version.getAgent().getId());
        return response;
    }
}
