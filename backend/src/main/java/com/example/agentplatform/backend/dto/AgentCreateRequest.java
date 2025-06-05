package com.example.agentplatform.backend.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class AgentCreateRequest {
    @NotBlank(message = "Agent name cannot be blank")
    @Size(min = 3, max = 100, message = "Agent name must be between 3 and 100 characters")
    private String name;

    private String description;

    private Long derivedFromAgentId; // Optional: for creating a new agent based on another

    // Initial version details
    @NotBlank(message = "Initial version tag cannot be blank")
    private String initialVersionTag;

    private String modelDependencies;
    private String capabilitiesDescription;
    private String configurationDetails;
}
