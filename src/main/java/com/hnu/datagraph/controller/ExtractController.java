package com.hnu.datagraph.controller;

import com.hnu.datagraph.service.ExtractService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Api(tags = "知识抽取相关接口")
@RestController
@RequestMapping("/extract")
@Slf4j
public class ExtractController {

    @Autowired
    private ExtractService extractService;

    @ApiOperation("导入结构化数据Excel表")
    @PostMapping("/api/upload")
    public ResponseEntity<String> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        boolean loadExtractInfoExcel = extractService.loadExtractInfoExcel(file);
        if (loadExtractInfoExcel) {
            return ResponseEntity.ok("File uploaded and processed successfully");
        } else {
            return ResponseEntity.badRequest().body("Error uploading file!");
        }
    }
}
