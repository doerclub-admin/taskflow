package com.example.agentplatform.backend.service;

import com.example.agentplatform.backend.dto.ProjectCreateRequest;
import com.example.agentplatform.backend.dto.ProjectResponse;
import com.example.agentplatform.backend.dto.ProjectUpdateRequest;
import com.example.agentplatform.backend.model.Agent;
import com.example.agentplatform.backend.model.Project;
import com.example.agentplatform.backend.repository.AgentRepository;
import com.example.agentplatform.backend.repository.ProjectRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AgentRepository agentRepository;

    public ProjectService(ProjectRepository projectRepository, AgentRepository agentRepository) {
        this.projectRepository = projectRepository;
        this.agentRepository = agentRepository;
    }

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        if (projectRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Project with name '" + request.getName() + "' already exists.");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus("CREATED"); // Initial status

        if (request.getAgentIds() != null && !request.getAgentIds().isEmpty()) {
            Set<Agent> agentsToAssociate = new HashSet<>(agentRepository.findAllById(request.getAgentIds()));
            if (agentsToAssociate.size() != request.getAgentIds().size()) {
                throw new EntityNotFoundException("One or more agents not found for the provided IDs during project creation.");
            }
            project.setAgents(agentsToAssociate);
        }

        Project savedProject = projectRepository.save(project);
        return convertToProjectResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToProjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project with ID " + id + " not found."));
        return convertToProjectResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectUpdateRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project with ID " + id + " not found."));

        if (request.getName() != null && !request.getName().equals(project.getName())) {
             if(projectRepository.findByName(request.getName()).isPresent()){
                 throw new IllegalArgumentException("Project with name '" + request.getName() + "' already exists.");
            }
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        Project updatedProject = projectRepository.save(project);
        return convertToProjectResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project with ID " + id + " not found."));
        // Dissociate agents to avoid issues if agents are not deleted with the project
        project.getAgents().clear();
        projectRepository.save(project); // Save dissociation
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse addAgentsToProject(Long projectId, Set<Long> agentIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project with ID " + projectId + " not found."));

        List<Agent> agentsToAdd = agentRepository.findAllById(agentIds);
        if (agentsToAdd.size() != agentIds.size()) {
            throw new EntityNotFoundException("One or more agents not found for the provided IDs.");
        }

        project.getAgents().addAll(agentsToAdd);
        Project updatedProject = projectRepository.save(project);
        return convertToProjectResponse(updatedProject);
    }

    @Transactional
    public ProjectResponse removeAgentsFromProject(Long projectId, Set<Long> agentIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project with ID " + projectId + " not found."));

        List<Agent> agentsToRemove = agentRepository.findAllById(agentIds);
        // No need to check size here, removing non-associated agents is fine

        project.getAgents().removeAll(agentsToRemove);
        Project updatedProject = projectRepository.save(project);
        return convertToProjectResponse(updatedProject);
    }

    // --- Stubbed Execution Control Methods ---
    @Transactional
    public ProjectResponse startProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project with ID " + projectId + " not found."));
        // TODO: Implement actual start logic with Docker
        if(project.getAgents().isEmpty()){
            throw new IllegalStateException("Project cannot be started without any associated agents.");
        }
        project.setStatus("RUNNING");
        Project updatedProject = projectRepository.save(project);
        // Simulate some action: System.out.println("Project " + projectId + " started (stub).");
        return convertToProjectResponse(updatedProject);
    }

    @Transactional
    public ProjectResponse stopProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project with ID " + projectId + " not found."));
        // TODO: Implement actual stop logic with Docker
        project.setStatus("STOPPED");
        Project updatedProject = projectRepository.save(project);
        // Simulate some action: System.out.println("Project " + projectId + " stopped (stub).");
        return convertToProjectResponse(updatedProject);
    }

    @Transactional
    public ProjectResponse pauseProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project with ID " + projectId + " not found."));
        // TODO: Implement actual pause logic with Docker
        if (!"RUNNING".equals(project.getStatus())) {
            throw new IllegalStateException("Project must be in RUNNING state to be paused.");
        }
        project.setStatus("PAUSED");
        Project updatedProject = projectRepository.save(project);
        // Simulate some action: System.out.println("Project " + projectId + " paused (stub).");
        return convertToProjectResponse(updatedProject);
    }

    @Transactional(readOnly = true)
    public String getProjectStatus(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project with ID " + projectId + " not found."));
        // TODO: Later, this might query Docker for real-time status
        return project.getStatus();
    }

    // --- Stubbed Resource Usage Statistics ---
    @Transactional(readOnly = true)
    public Object getResourceUsage(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project with ID " + projectId + " not found."));
        // TODO: Implement actual resource usage statistics logic
        // Simulate some action: System.out.println("Fetching resource usage for project " + projectId + " (stub).");
        return "{\"cpuUsage\": \"0%\", \"memoryUsage\": \"0MB\"}"; // Placeholder
    }


    // --- Helper Conversion Method ---
    private ProjectResponse convertToProjectResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        BeanUtils.copyProperties(project, response, "agents"); // Exclude agents for manual mapping

        if (project.getAgents() != null) {
            response.setAgents(project.getAgents().stream().map(agent -> {
                ProjectResponse.SimpleAgentInfo simpleAgentInfo = new ProjectResponse.SimpleAgentInfo();
                simpleAgentInfo.setId(agent.getId());
                simpleAgentInfo.setName(agent.getName());
                simpleAgentInfo.setCurrentVersionTag(agent.getCurrentVersionTag());
                return simpleAgentInfo;
            }).collect(Collectors.toSet()));
        }
        return response;
    }
}
