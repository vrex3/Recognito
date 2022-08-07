package org.vrex.recognito.utility;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HttpResponseUtil {

    private HttpResponseUtil() {
        //enforces singleton pattern
    }

    /**
     * Wraps a payload in a HttpStatus 200 OK response package
     *
     * @param payload
     * @return
     */
    public static <T> ResponseEntity<T> wrapInHttpStatusOkResponse(T payload) {
        return new ResponseEntity<>(payload, HttpStatus.OK);
    }

    /**
     * Checks whether payload os null or not
     * If null returns red status code e.g. 404
     * or else returns wrapped payload with green status code e.g. 200
     *
     * @param payLoad
     * @param greenStatus
     * @param redStatus
     * @param <T>
     * @return
     */
    public static <T> ResponseEntity<T> returnRawPackageWithStatusOrElse(T payLoad, HttpStatus greenStatus, HttpStatus redStatus) {
        return ObjectUtils.isEmpty(payLoad) ? new ResponseEntity<>(redStatus) : new ResponseEntity<T>(payLoad, greenStatus);
    }
}
