package org.vrex.recognito.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class Message implements Serializable {

    private static final long serialVersionUID = -2315282080748649157L;

    private String text;
    private Object data;
}
