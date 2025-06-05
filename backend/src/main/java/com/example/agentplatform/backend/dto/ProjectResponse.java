package com.example.agentplatform.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<SimpleAgentInfo> agents; // Using a simplified DTO for agent info

    @Data
    public static class SimpleAgentInfo {
        private Long id;
        private String name;
        private String currentVersionTag;
    }
}
