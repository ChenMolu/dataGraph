package com.jwlz.sjjc.zstp.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer {

    private Map<String,String> answer;

    private List<Point> graph;

}
