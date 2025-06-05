package com.example.agentplatform.backend.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_versions")
@Data
public class AgentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(name = "version_tag", nullable = false)
    private String versionTag; // e.g., "v1.0", "v1.1-alpha"

    @Lob
    @Column(name = "model_dependencies")
    private String modelDependencies; // JSON or structured text

    @Lob
    @Column(name = "capabilities_description")
    private String capabilitiesDescription;

    @Lob
    @Column(name = "configuration_details") // JSON or structured text
    private String configurationDetails;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean active = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
