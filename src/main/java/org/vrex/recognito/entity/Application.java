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

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

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

    //AUTO GENERATED
    @Indexed(unique = true)
    @Field("appUUID")
    private String appUUID;

    @Indexed(unique = true)
    @Field("publicKey")
    private String publicKey;

    @Field("privateKey")
    private String privateKey;

    @Field("onboardedOn")
    private LocalDateTime onboardedOn;

    @Field("updatedOn")
    private LocalDateTime updatedOn;

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
