package org.vrex.recognito.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.http.HttpStatus;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.model.ApplicationException;
import org.vrex.recognito.utility.RoleUtil;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "Resource_To_Role_Mapping")
public class ResourceAppMap implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    private ResourceIndex id;

    @Field("description")
    private String description;

    @Field("ownerRoles")
    private Set<String> roles;

    @Field("onboardedOn")
    private LocalDateTime onboardedOn;

    @Field("updatedOn")
    private LocalDateTime updatedOn;

    public ResourceAppMap(Application application, String resourceId, String description, Set<String> roles) {

        if (application.isResourcesEnabled()) {
            this.id = new ResourceIndex(application.getAppUUID(), resourceId);
            this.description = description;
            this.roles = new HashSet<>();
            this.onboardedOn = ApplicationConstants.currentTime();
            addRoles(roles);
        } else
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.INVALID_APP_FOR_RESOURCES).
                    status(HttpStatus.BAD_REQUEST).
                    build();
    }

    /**
     * Checks whether this resource is mapped to a role or not
     *
     * @param role
     * @return
     */
    public boolean belongsToRole(String role) {
        return validateRole(role) && this.roles.contains(role);
    }

    /**
     * Verifies and adds a list of non system role to a resource mapping
     *
     * @param roles
     */
    public void addRoles(Set<String> roles) {
        for (String role : roles) {
            if (validateRole(role)) {
                this.roles.add(role);
            }
        }

        updatedOn = ApplicationConstants.currentTime();
    }

    /**
     * Verifies and adds a non system role to a resource mapping
     *
     * @param role
     */
    public void addRole(String role) {
        if (validateRole(role)) {
            roles.add(role);
            updatedOn = ApplicationConstants.currentTime();
        }
    }

    /**
     * Verifies and validates a role
     * Returns true if it is a valid NON SYSTEM role
     *
     * @param role
     * @return
     */
    private boolean validateRole(String role) {
        if (!RoleUtil.isValidRole(role))
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.INVALID_ROLE).
                    status(HttpStatus.BAD_REQUEST).
                    build();

        if (RoleUtil.isSystemRole(role))
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.SYSTEM_ROLE_RESOURCE_MAPPING_ATTEMPT).
                    status(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS).
                    build();

        return true;
    }


}
