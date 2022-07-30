package org.vrex.recognito.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.http.HttpStatus;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.model.ApplicationException;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
@SuppressWarnings("unused")
public class ResourceIndex implements Serializable {

    private static final String RESOURCE_STRING_REGEX = "/^[a-zA-Z0-9_]{0,40}$/";
    private static final Pattern RESOURCE_PATTERN = Pattern.compile(RESOURCE_STRING_REGEX);

    @Field("appUUID")
    private String appUUID;

    @Field("res_identifier")
    private String resourceId;

    public ResourceIndex(String appUUID, String resourceId) {
        this.appUUID = appUUID;
        this.resourceId = validateResourceString(resourceId);
    }

    /**
     * Validates a resource string according to the following rules
     * Cannot be empty
     * Cannot have whitespaces
     * Alphanumeric and underscore are the only chars allowed
     * Cannot be more than 40 characters
     *
     * @param resource
     * @return
     */
    private String validateResourceString(String resource) {
        if (StringUtils.isEmpty(resource) || !RESOURCE_PATTERN.matcher(resource).matches())
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.INVALID_RESOURCE).
                    status(HttpStatus.BAD_REQUEST).
                    build();

        return resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceIndex that = (ResourceIndex) o;
        return Objects.equals(getAppUUID(), that.getAppUUID()) && Objects.equals(getResourceId(), that.getResourceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAppUUID(), getResourceId());
    }
}
