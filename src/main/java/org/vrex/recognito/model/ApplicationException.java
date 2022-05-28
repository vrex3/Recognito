package org.vrex.recognito.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = 8159934445993571899L;

    private String errorMessage;
    private HttpStatus status;

}