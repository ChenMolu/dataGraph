package com.jwlz.sjjc.zstp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GstoreQueryDataRequest {

    private String dbname;

    private String sparql;

    private String limit;

    private String type;

//    private boolean isStandard;

}
