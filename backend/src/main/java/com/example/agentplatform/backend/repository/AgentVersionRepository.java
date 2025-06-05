package com.example.agentplatform.backend.repository;

import com.example.agentplatform.backend.model.Agent;
import com.example.agentplatform.backend.model.AgentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentVersionRepository extends JpaRepository<AgentVersion, Long> {
    List<AgentVersion> findByAgent(Agent agent);
    Optional<AgentVersion> findByAgentAndVersionTag(Agent agent, String versionTag);
    Optional<AgentVersion> findByAgentAndActive(Agent agent, boolean active);
}
