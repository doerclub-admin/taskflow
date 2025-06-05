package com.example.agentplatform.backend.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class AgentVersionCreateRequest {
    @NotBlank(message = "Version tag cannot be blank")
    private String versionTag;

    private String modelDependencies;
    private String capabilitiesDescription;
    private String configurationDetails;
    private boolean makeActive = false; // Option to make this new version active upon creation
}
