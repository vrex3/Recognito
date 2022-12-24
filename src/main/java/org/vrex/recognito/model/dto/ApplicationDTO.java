package org.vrex.recognito.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.vrex.recognito.entity.Application;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApplicationDTO implements Serializable {

    private String id;
    private String name;
    private String description;
    private String appUUID;
    private LocalDateTime onboardedOn;
    private LocalDateTime updatedOn;
    private RoleResourceMapping roleMappings;

    private UserDTO rootUser;

    public ApplicationDTO(Application application) {
        if (!ObjectUtils.isEmpty(application)) {
            this.id = application.getId();
            this.appUUID = application.getAppUUID();
            this.name = application.getName();
            this.description = application.getDescription();
            this.onboardedOn = application.getOnboardedOn();
            this.updatedOn = application.getUpdatedOn();
        }
    }

    public ApplicationDTO(Application application, UserDTO rootUser) {
        if (!ObjectUtils.isEmpty(application)) {
            this.id = application.getId();
            this.appUUID = application.getAppUUID();
            this.name = application.getName();
            this.description = application.getDescription();
            this.onboardedOn = application.getOnboardedOn();
            this.updatedOn = application.getUpdatedOn();
            this.rootUser = rootUser;
        }
    }

}
