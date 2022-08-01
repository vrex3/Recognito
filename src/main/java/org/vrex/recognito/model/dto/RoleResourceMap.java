package org.vrex.recognito.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoleResourceMap implements Serializable {

    private String role;
    private Map<String, String> resources;
    //resourceID -> resource description
    private Set<String> resourceList;

    public RoleResourceMap(String role, Set<String> resourceList) {
        this.role = role;
        this.resourceList = resourceList;
    }


}
