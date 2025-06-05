package com.example.agentplatform.backend.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class ProjectAgentAssociationRequest {
    @NotNull(message = "Agent IDs cannot be null")
    private Set<Long> agentIds;
}
