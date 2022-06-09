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
    private long id;

    @EqualsAndHashCode.Include
    @Indexed(unique = true)
    @Field("userName")
    private String username;

    @Indexed(unique = true)
    @Field("secret")
    private String secret;

    @Field("email")
    private String email;

    @Indexed(unique = true)
    @Field("publicKey")
    private String publicKey;

    @Field("privateKey")
    private String privateKey;

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
     * @param publicKey
     * @param privateKey
     */
    public User(String userName,
                String secret,
                String email,
                byte[] publicKey,
                byte[] privateKey) {

        this.username = userName;
        this.email = email;
        this.secret = secret;
        this.version = getNextVersion();
        this.onboardedOn = ApplicationConstants.currentTime();
        this.updatedOn = onboardedOn;
        this.publicKey = readKeyToString(publicKey);
        this.privateKey = readKeyToString(privateKey);

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

    /**
     * Upgrades the profile version number from vN to v(N+1)
     *
     * @return
     */
    private String getNextVersion() {
        return "v" + ((version != null ? Integer.parseInt(version.substring(1)) : 0) + 1);
    }

}
