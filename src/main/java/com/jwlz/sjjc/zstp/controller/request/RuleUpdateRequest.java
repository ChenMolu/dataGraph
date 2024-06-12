package com.jwlz.sjjc.zstp.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author luhuachen
 */
@Data
public class RuleUpdateRequest {

    @JsonProperty("GZID")
    private String ID;

    @JsonProperty("GZLX")
    private int GZLX;

    @JsonProperty("GZ")
    private String GZ;

    @JsonProperty("GZMS")
    private String GZMS;

    @JsonProperty("CJSJ")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date CZSJ;

}
