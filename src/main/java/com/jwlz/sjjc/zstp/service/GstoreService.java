package com.jwlz.sjjc.zstp.service;

import com.jwlz.sjjc.zstp.entity.GstoreResult;

public interface GstoreService {
    GstoreResult query(String database, String sparQL);

}
