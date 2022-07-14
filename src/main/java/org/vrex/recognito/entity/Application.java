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
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "user_identity")
public class Application implements Serializable {

    @Id
    @Field("appId")
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

    //private boolean hasRoles;

    @Indexed(unique = true)
    @Field("publicKey")
    private String publicKey;

    @Field("privateKey")
    private String privateKey;

    @Field("onboardedOn")
    private LocalDateTime onboardedOn;

    @Field("updatedOn")
    private LocalDateTime updatedOn;

    public Application(UpsertApplicationRequest request, KeyPair keyPair){
        this.appUUID = UUID.randomUUID().toString();
        this.id = request.getName();
        this.name = request.getName();
        this.description = request.getDescription();
        this.publicKey = readKeyToString(keyPair.getPublic().getEncoded());
        this.privateKey = readKeyToString(keyPair.getPrivate().getEncoded());
        this.onboardedOn = ApplicationConstants.currentTime();
        this.updatedOn = onboardedOn;
    }

    public void updateApp(){
        this.updatedOn = ApplicationConstants.currentTime();
    }

    /**
     * Reads public key into byte array
     *
     * @return
     */
    public byte[] readPublicKey() {
        return readKeyToBytes(publicKey);
    }

    /**
     * Reads private key into byte array
     *
     * @return
     */
    public byte[] readPrivateKey() {
        return readKeyToBytes(privateKey);
    }

    /**
     * Converts a key from byte[] to string
     *
     * @param key
     * @return
     */
    private String readKeyToString(byte[] key) {
        return key.length != 0 ? new String(key, StandardCharsets.UTF_8) : null;
    }

    /**
     * Converts a string of a key to byte[]
     *
     * @param text
     * @return
     */
    private byte[] readKeyToBytes(String text) {
        return text != null ? text.getBytes(StandardCharsets.UTF_8) : new byte[0];
    }

}
