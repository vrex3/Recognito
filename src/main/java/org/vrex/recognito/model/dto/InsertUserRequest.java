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
public class InsertUserRequest implements Serializable {

    @NotNull(message = ApplicationConstants.EMPTY_USERNAME)
    private String username;

    private String email;

    @NotNull(message = ApplicationConstants.EMPTY_APPLICATION_IDENTIFIER)
    private String appIdentifier;

}
