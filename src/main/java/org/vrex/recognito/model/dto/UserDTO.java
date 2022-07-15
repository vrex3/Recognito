package org.vrex.recognito.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.vrex.recognito.entity.User;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO implements Serializable {

    private String username;
    private String email;
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
            this.version = user.getVersion();
            this.onboardedOn = user.getOnboardedOn();
            this.updatedOn = user.getUpdatedOn();

            //TO BE REMOVED
            this.secret = user.getSecret();
        }
    }
}
