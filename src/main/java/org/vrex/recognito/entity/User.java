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

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collation = "user_identity")
public class User {

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

    @Field("publicKey")
    private byte[] publicKey;

    @Field("privateKey")
    private byte[] privateKey;

}
