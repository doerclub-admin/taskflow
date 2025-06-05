package com.example.agentplatform.backend.dto;

import lombok.Data;
import javax.validation.constraints.Size;

@Data
public class AgentUpdateRequest {
    @Size(min = 3, max = 100, message = "Agent name must be between 3 and 100 characters")
    private String name; // Optional: allow name change if needed, though sometimes restricted

    private String description;
    // currentVersionTag might be updated via a separate version activation endpoint
}
