package org.vrex.recognito.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.vrex.recognito.config.ApplicationConstants;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "user_identity")
public class User implements Serializable {

    @Id
    @Field("userId")
    private String id;

    @EqualsAndHashCode.Include
    @Indexed(unique = true)
    @Field("userName")
    private String username;

    @Indexed(unique = true)
    @Field("secret")
    private String secret; //password

    @Field("email")
    private String email;

    @DBRef
    private Application application;

    @Field("profileVersion")
    private String version; //vN

    @Field("onboardedOn")
    private LocalDateTime onboardedOn;

    @Field("updatedOn")
    private LocalDateTime updatedOn;

    /**
     * Custom constructor to create entity from user input
     *
     * @param userName
     * @param secret
     * @param email
     */
    public User(String userName,
                String secret,
                String email) {

        this.id = username;
        this.username = userName;
        this.email = email;
        this.secret = secret;
        this.version = getNextVersion();
        this.onboardedOn = ApplicationConstants.currentTime();
        this.updatedOn = onboardedOn;
    }

    /**
     * Upgrades the profile version number from vN to v(N+1)
     *
     * @return
     */
    private String getNextVersion() {
        return "v" + ((version != null ? Integer.parseInt(version.substring(1)) : 0) + 1);
    }

}
