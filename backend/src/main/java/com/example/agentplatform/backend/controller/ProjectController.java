package com.example.agentplatform.backend.controller;

import com.example.agentplatform.backend.dto.ProjectAgentAssociationRequest;
import com.example.agentplatform.backend.dto.ProjectCreateRequest;
import com.example.agentplatform.backend.dto.ProjectResponse;
import com.example.agentplatform.backend.dto.ProjectUpdateRequest;
import com.example.agentplatform.backend.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse createdProject = projectService.createProject(request);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        ProjectResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        ProjectResponse updatedProject = projectService.updateProject(id, request);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/agents")
    public ResponseEntity<ProjectResponse> addAgentsToProject(@PathVariable Long projectId,
                                                              @Valid @RequestBody ProjectAgentAssociationRequest request) {
        ProjectResponse project = projectService.addAgentsToProject(projectId, request.getAgentIds());
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{projectId}/agents") // Using RequestBody for DELETE to pass a list of agent IDs
    public ResponseEntity<ProjectResponse> removeAgentsFromProject(@PathVariable Long projectId,
                                                                   @Valid @RequestBody ProjectAgentAssociationRequest request) {
        ProjectResponse project = projectService.removeAgentsFromProject(projectId, request.getAgentIds());
        return ResponseEntity.ok(project);
    }

    // --- Execution Control Endpoints ---
    @PostMapping("/{projectId}/start")
    public ResponseEntity<ProjectResponse> startProject(@PathVariable Long projectId) {
        ProjectResponse project = projectService.startProject(projectId);
        return ResponseEntity.ok(project);
    }

    @PostMapping("/{projectId}/stop")
    public ResponseEntity<ProjectResponse> stopProject(@PathVariable Long projectId) {
        ProjectResponse project = projectService.stopProject(projectId);
        return ResponseEntity.ok(project);
    }

    @PostMapping("/{projectId}/pause")
    public ResponseEntity<ProjectResponse> pauseProject(@PathVariable Long projectId) {
        ProjectResponse project = projectService.pauseProject(projectId);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/{projectId}/status")
    public ResponseEntity<String> getProjectStatus(@PathVariable Long projectId) {
        String status = projectService.getProjectStatus(projectId);
        return ResponseEntity.ok(status);
    }

    // --- Resource Usage Endpoint ---
    @GetMapping("/{projectId}/resources")
    public ResponseEntity<Object> getResourceUsage(@PathVariable Long projectId) {
        Object usage = projectService.getResourceUsage(projectId);
        return ResponseEntity.ok(usage);
    }
}
