package org.vrex.recognito.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.vrex.recognito.entity.User;
import org.vrex.recognito.model.TokenPayload;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO implements Serializable {

    private String username;
    private String email;
    private String role;
    private String appUUID;
    private String appName;
    private String version;
    private LocalDateTime onboardedOn;
    private LocalDateTime updatedOn;

    //TO BE REMOVED
    private String secret;
    //secret to be removed from here
    //return in email


    public UserDTO(User user) {
        if (!ObjectUtils.isEmpty(user)) {
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.appName = ObjectUtils.isEmpty(user.getApplication()) ? null : user.getApplication().getName();
            this.appUUID = ObjectUtils.isEmpty(user.getApplication()) ? null : user.getApplication().getAppUUID();
            this.role = ObjectUtils.isEmpty(user.getApplication()) || !user.getApplication().isRolesEnabled() ? null : user.getRole();
            this.version = user.getVersion();
            this.onboardedOn = user.getOnboardedOn();
            this.updatedOn = user.getUpdatedOn();

            //TO BE REMOVED
            this.secret = user.getSecret();
        }
    }

    public UserDTO(TokenPayload payload) {
        if (!ObjectUtils.isEmpty(payload)) {
            this.username = payload.getUsername();
            this.email = payload.getEmail();
            this.version = payload.getProfileVersion();
            this.role = payload.getRole();
        }
    }
}
