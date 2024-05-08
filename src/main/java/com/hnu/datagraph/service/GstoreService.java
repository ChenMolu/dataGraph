package com.hnu.datagraph.service;

import com.hnu.datagraph.entity.GstoreResult;

public interface GstoreService {
    GstoreResult query(String database, String sparQL);

}
