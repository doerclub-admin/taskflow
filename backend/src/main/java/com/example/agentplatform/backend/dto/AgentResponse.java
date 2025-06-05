package com.example.agentplatform.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgentResponse {
    private Long id;
    private String name;
    private String description;
    private String currentVersionTag;
    private Long derivedFromAgentId;
    private String derivedFromAgentName; // For easier display
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AgentVersionResponse> versions; // Optionally include all versions or just the active one
}
