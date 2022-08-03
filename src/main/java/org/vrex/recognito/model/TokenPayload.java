package org.vrex.recognito.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.utility.TokenUtil;

import java.io.Serializable;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TokenPayload implements Serializable {

    private String issuer;
    private String username;
    private String email;
    private String role;
    private String profileVersion;
    private boolean resourcesEnabled;
    private Date issuedAt;
    private Date expiryOn;

    /**
     * Populates the body of a token from a set of JWT token claims
     *
     * @param claims
     * @throws ApplicationException
     */
    public void populatePayload(JWTClaimsSet claims) throws ApplicationException {
        try {
            if (!ObjectUtils.isEmpty(claims)) {
                this.issuer = claims.getIssuer();
                this.username = claims.getSubject();
                this.email = claims.getClaim(TokenUtil.EMAIL).toString();
                this.profileVersion = claims.getClaim(TokenUtil.PROFILE_VERSION).toString();
                this.issuedAt = claims.getIssueTime();
                this.expiryOn = claims.getExpirationTime();
                this.resourcesEnabled = Boolean.parseBoolean(claims.getClaim(TokenUtil.RESOURCES_ENABLED).toString());

                Object claimRole = claims.getClaim(TokenUtil.ROLE);
                this.role = claimRole != null ? claimRole.toString() : null;
            }
        } catch (Exception exception) {
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.INVALID_TOKEN_PAYLOAD).
                    status(HttpStatus.UNAUTHORIZED).
                    build();
        }
    }
}
