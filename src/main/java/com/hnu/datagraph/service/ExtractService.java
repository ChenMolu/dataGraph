package com.hnu.datagraph.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface ExtractService {

    public boolean loadExtractInfoExcel(MultipartFile file);
}
