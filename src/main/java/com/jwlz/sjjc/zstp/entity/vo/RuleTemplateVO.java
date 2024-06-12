package com.jwlz.sjjc.zstp.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luhuachen
 */
@Data
public class RuleTemplateVO {

    @JsonProperty(value = "GZID")
    private String GZID;

    @JsonProperty("GZLX")
    private int GZLX;

    @JsonProperty("GZMB")
    private String GZMB;

    @JsonProperty("GZMS")
    private String GZMS;

}
