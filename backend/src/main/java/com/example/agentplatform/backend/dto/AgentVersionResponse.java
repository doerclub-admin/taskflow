package com.example.agentplatform.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AgentVersionResponse {
    private Long id;
    private Long agentId;
    private String versionTag;
    private String modelDependencies;
    private String capabilitiesDescription;
    private String configurationDetails;
    private boolean active;
    private LocalDateTime createdAt;
}
