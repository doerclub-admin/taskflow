package com.example.agentplatform.backend.dto;

import lombok.Data;
import javax.validation.constraints.Size;

@Data
public class ProjectUpdateRequest {
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    private String name;

    private String description;

    // Agent associations will be handled by a separate endpoint
    // Status updates will be handled by separate execution control endpoints
}
