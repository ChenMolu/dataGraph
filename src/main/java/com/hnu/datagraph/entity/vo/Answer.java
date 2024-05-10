package com.hnu.datagraph.entity.vo;

import com.hnu.datagraph.entity.Graph;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    private Map<String,String> answer;

    private Graph graph;

}