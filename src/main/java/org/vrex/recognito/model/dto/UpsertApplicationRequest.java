package org.vrex.recognito.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vrex.recognito.config.ApplicationConstants;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpsertApplicationRequest implements Serializable {

    @NotNull(message = ApplicationConstants.EMPTY_APPLICATION_NAME)
    private String name;

    private String description;

    private boolean resourcesEnabled = true;

    private String email;

}
