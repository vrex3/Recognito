package org.vrex.recognito.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.model.dto.UpsertApplicationRequest;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "Application")
public class Application implements Serializable {

    @Id
    private String id;

    @EqualsAndHashCode.Include
    @Indexed(unique = true)
    @Field("appName")
    private String name;

    @Field("description")
    private String description;

    //AUTO GENERATED
    @Indexed(unique = true)
    @Field("appUUID")
    private String appUUID;

    // encrypted version of appUUID
    // encrypted by app public key
    // decrypted by app private key
    @Field("appSecret")
    private String appSecret;

    // Allows of mapping of roles to resources
    // NOT ALLOWED FOR SYSTEM ROLES
    @Field("resourcesEnabled")
    private boolean resourcesEnabled;

    @Field("publicKey")
    private byte[] publicKey;

    @Field("privateKey")
    private byte[] privateKey;

    @Field("onboardedOn")
    private LocalDateTime onboardedOn;

    @Field("updatedOn")
    private LocalDateTime updatedOn;

    public Application(UpsertApplicationRequest request) {
        this.appUUID = UUID.randomUUID().toString();
        String appName = formatAppName(request.getName());
        this.id = appName;
        this.name = appName;
        this.description = request.getDescription();
        this.onboardedOn = ApplicationConstants.currentTime();
        this.updatedOn = onboardedOn;
        this.resourcesEnabled = request.isResourcesEnabled();
    }

    /**
     * Formats an app name for better indexing
     * All whitespaces are replaced by underscore
     *
     * @param name
     * @return
     */
    public String formatAppName(String name) {
        String formattedName = null;
        if (name != null) {
            formattedName = name.trim().replaceAll("\\s", "_");
        }
        return formattedName;
    }

    /**
     * Updates the timestamp to current time
     */
    public void updateApp() {
        this.updatedOn = ApplicationConstants.currentTime();
    }

}
