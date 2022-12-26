package org.vrex.recognito.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.entity.ResourceIndex;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Fetch all the ResourceIndex for db query
     *
     * @return
     */
    @JsonIgnore
    public List<ResourceIndex> getResourceIndices() {
        List<ResourceIndex> indices = new LinkedList<>();
        if (mappings != null) {
            mappings.stream().forEach(mapping -> {
                Map<String, String> resources = mapping.getResources();
                if (resources != null)
                    resources.keySet().stream().forEach(resource -> indices.add(new ResourceIndex(appUUID, resource)));
            });
        }
        return indices;
    }
}
