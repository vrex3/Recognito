package org.vrex.recognito.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.vrex.recognito.config.ApplicationConstants;

import javax.validation.constraints.AssertTrue;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApplicationIdentifier implements Serializable {

    private String appName;
    private String appUUID;

    @AssertTrue(message = ApplicationConstants.EMPTY_APPLICATION_IDENTIFIER)
    public boolean isValid() {
        return !(StringUtils.isEmpty(appName) && StringUtils.isEmpty(appUUID));
    }
}
