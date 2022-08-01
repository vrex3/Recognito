package org.vrex.recognito.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vrex.recognito.config.ApplicationConstants;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoleResourceMapping implements Serializable {

    @NotNull(message = ApplicationConstants.EMPTY_APPLICATION_IDENTIFIER)
    private String appUUID;
    private List<RoleResourceMap> mappings;

    public RoleResourceMapping(String appUUID) {
        this.appUUID = appUUID;
        this.mappings = new LinkedList<>();
    }

    /**
     * Adds a role - resource list mapping
     *
     * @param role
     * @param resources
     */
    public void addMapping(String role, Set<String> resources) {
        this.mappings.add(new RoleResourceMap(role, resources));
    }
}
