package com.hnu.datagraph.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.hnu.datagraph.entity.dto.ExcelExtractFileDto;
import com.hnu.datagraph.service.ExtractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class ExtractServiceImpl implements ExtractService {


    @Override
    public boolean loadExtractInfoExcel(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), ExcelExtractFileDto.class, new ReadListener<ExcelExtractFileDto>() {
                @Override
                public void invoke(ExcelExtractFileDto excelExtractFileDto, AnalysisContext analysisContext) {

                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {

                }
            }).sheet().headRowNumber(0).doRead();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
