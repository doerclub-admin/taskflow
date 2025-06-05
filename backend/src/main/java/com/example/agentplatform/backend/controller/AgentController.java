package com.example.agentplatform.backend.controller;

import com.example.agentplatform.backend.dto.*;
import com.example.agentplatform.backend.service.AgentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping
    public ResponseEntity<AgentResponse> createAgent(@Valid @RequestBody AgentCreateRequest request) {
        AgentResponse createdAgent = agentService.createAgent(request);
        return new ResponseEntity<>(createdAgent, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AgentResponse>> getAllAgents() {
        List<AgentResponse> agents = agentService.getAllAgents();
        return ResponseEntity.ok(agents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentResponse> getAgentById(@PathVariable Long id) {
        AgentResponse agent = agentService.getAgentById(id);
        return ResponseEntity.ok(agent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgentResponse> updateAgent(@PathVariable Long id, @Valid @RequestBody AgentUpdateRequest request) {
        AgentResponse updatedAgent = agentService.updateAgent(id, request);
        return ResponseEntity.ok(updatedAgent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgent(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{agentId}/versions")
    public ResponseEntity<AgentVersionResponse> createAgentVersion(@PathVariable Long agentId,
                                                                   @Valid @RequestBody AgentVersionCreateRequest request) {
        AgentVersionResponse createdVersion = agentService.createAgentVersion(agentId, request);
        return new ResponseEntity<>(createdVersion, HttpStatus.CREATED);
    }

    @GetMapping("/{agentId}/versions")
    public ResponseEntity<List<AgentVersionResponse>> getAgentVersions(@PathVariable Long agentId) {
        List<AgentVersionResponse> versions = agentService.getAgentVersions(agentId);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/{agentId}/versions/{versionTag}")
    public ResponseEntity<AgentVersionResponse> getAgentVersionByTag(@PathVariable Long agentId, @PathVariable String versionTag) {
        AgentVersionResponse version = agentService.getAgentVersionByTag(agentId, versionTag);
        return ResponseEntity.ok(version);
    }

    @PostMapping("/{agentId}/versions/{versionTag}/activate")
    public ResponseEntity<AgentVersionResponse> setActiveAgentVersion(@PathVariable Long agentId, @PathVariable String versionTag) {
        AgentVersionResponse activatedVersion = agentService.setActiveAgentVersion(agentId, versionTag);
        return ResponseEntity.ok(activatedVersion);
    }
}
