package org.vrex.recognito.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.model.ApplicationException;

import java.io.Serializable;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApplicationIdentifier implements Serializable {

    private String appName;
    private String appUUID;

    public ApplicationIdentifier(Map<String, String> params) {
        this.appName = params.getOrDefault("appName", null);
        this.appUUID = params.getOrDefault("appUUID", null);

        if (StringUtils.isEmpty(appName) && StringUtils.isEmpty(appUUID))
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.EMPTY_APPLICATION_IDENTIFIER).
                    status(HttpStatus.BAD_REQUEST).
                    build();
    }
}
