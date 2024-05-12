package com.jwlz.sjjc.zstp.service.impl;


import com.jwlz.sjjc.zstp.service.NlpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NlpServiceImpl implements NlpService, InitializingBean {

    public static boolean isLoaded = false;

    public Map<String, String> markWordMap = new HashMap<>();

    public static Map<String, List<String>> markRelationMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!isLoaded) {
            loadMarkWordMap();
            loadMarkRelationMap();
            isLoaded = true;
        }
    }

    public void loadMarkWordMap() {
        InputStream inputFile= Thread.currentThread().getContextClassLoader().getResourceAsStream("data/mark/mark_word.txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                String key = parts[0].trim();
                String value = parts[1].trim();
                markWordMap.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMarkRelationMap() {
        InputStream inputFile= Thread.currentThread().getContextClassLoader().getResourceAsStream("data/mark/mark_relation.txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 2); // 使用 "<" 作为分隔符，最多分成两部分
                String key = parts[0].trim();
                String valueString = parts[1].trim();

                String[] values = valueString.split(","); // 使用逗号作为分隔符

                List<String> valueList = new ArrayList<>();
                for (String value : values) {
                    valueList.add(value.trim());
                }
                markRelationMap.put(key, valueList);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<String> getStructuredQueryList(String sentence) {
        ArrayList<Triple> tripleList = null;

        List<String> result = new ArrayList<>();
        for(String key : markWordMap.keySet()) {
            if(sentence.contains(key)) {
                result.add(markWordMap.get(key));
                break;
            }
        }
        if(result.isEmpty()) {
            return result;
        }
        List<String> relations = markRelationMap.get(result.get(0));
        for(String value : relations) {
            if(sentence.contains(value)) {
                result.add(value);
            }
        }
        return result;
    }


}
