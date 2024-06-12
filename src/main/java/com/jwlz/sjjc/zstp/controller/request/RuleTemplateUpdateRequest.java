package com.jwlz.sjjc.zstp.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luhuachen
 */
@Data
public class RuleTemplateUpdateRequest {

    @JsonProperty("GZID")
    private String GZID;

    @JsonProperty("GZLX")
    private int GZLX;

    @JsonProperty("GZMB")
    private String GZMB;

    @JsonProperty("GZMS")
    private String GZMS;

}
