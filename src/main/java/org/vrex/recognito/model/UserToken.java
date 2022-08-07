package org.vrex.recognito.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nimbusds.jose.JWEObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vrex.recognito.entity.Application;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserToken implements Serializable {

    private String appId;
    private String token;

    public UserToken(Application application, JWEObject token) {
        this.appId = application != null ? application.getAppUUID() : null;
        this.token = token != null ? token.serialize() : null;
    }
}
