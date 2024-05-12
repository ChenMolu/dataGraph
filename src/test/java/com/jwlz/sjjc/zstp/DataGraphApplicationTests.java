package com.jwlz.sjjc.zstp;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jwlz.sjjc.zstp.entity.Item;
import com.jwlz.sjjc.zstp.entity.Relation;
import com.jwlz.sjjc.zstp.mapper.ItemMapper;
import com.jwlz.sjjc.zstp.mapper.RelationMapper;
import com.jwlz.sjjc.zstp.service.NlpService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DataGraphApplicationTests {

    @Resource
    private NlpService nlpService;

    @Resource
    private RelationMapper relationMapper;

    @Resource
    private ItemMapper itemMapper;

    @Test
    void ItemTest(){
        List<Item> items = itemMapper.selectList(null);
        System.out.println(items);
    }

    @Test
    void relationTest(){
        List<Relation> relations = relationMapper.selectList(Wrappers.emptyWrapper());
//        Relation relation = relationMapper.selectById(2);
        System.out.println(relations);
    }

    @Test
    void nlpTest(){
        List<String> structuredQueryList = nlpService.getStructuredQueryList("美国麦克迪尔空军基地的位置");
//        String s1 = "俄乌冲突是怎么造成的";
//        String s2 = "俄乌冲突";
//        System.out.println(s1.contains(s2));
        System.out.println(structuredQueryList);
    }

    @Test
    void contextLoads() {
    }

}
