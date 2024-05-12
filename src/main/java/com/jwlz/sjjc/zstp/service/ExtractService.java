package com.jwlz.sjjc.zstp.service;

import org.springframework.web.multipart.MultipartFile;

public interface ExtractService {

    public boolean loadExtractInfoExcel(MultipartFile file);
}
