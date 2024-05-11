package com.hnu.datagraph.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hnu.datagraph.common.R;
import com.hnu.datagraph.entity.Item;
import com.hnu.datagraph.entity.vo.Statistics;
import com.hnu.datagraph.mapper.ItemMapper;
import com.hnu.datagraph.mapper.RelationMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Resource
    private RelationMapper relationMapper;

    @Resource
    private ItemMapper itemMapper;

    @GetMapping(value = "/get")
    public R<Statistics> getStatistics() {
        Statistics statistics = new Statistics();
        statistics.setSubjectAmount(itemMapper.selectCount(new QueryWrapper<>()));
        statistics.setTripleAmount(relationMapper.selectCount(new QueryWrapper<>()));
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("TYPE", "COUNT(*) AS count");
        queryWrapper.groupBy("TYPE");
        List<Map<String, Object>> maps = itemMapper.selectMaps(queryWrapper);
        log.info(maps.toString());
        for (Map<String, Object> map : maps) {
            String type = (String) map.get("TYPE");
//            if(type.equals("机构")){
//                statistics.setInstitutionAmount((Long) map.get("COUNT"));
//            }else if()
            switch (type) {
                case "机构":
                    statistics.setInstitutionAmount((Long) map.get("COUNT"));
                    break;
                case "人员":
                    statistics.setPersonnelAmount((Long) map.get("COUNT"));
                    break;
                case "装备":
                    statistics.setEquipmentAmount((Long) map.get("COUNT"));
                    break;
                case "物资":
                    statistics.setMaterialAmount((Long) map.get("COUNT"));
                    break;
                case "设施":
                    statistics.setFacilityAmount((Long) map.get("COUNT"));
                    break;
                case "环境":
                    statistics.setEnvAmount((Long) map.get("COUNT"));
                    break;
                case "事件":
                    statistics.setEventAmount((Long) map.get("COUNT")) ;
                    break;
                case "行动":
                    statistics.setActionAmount((Long) map.get("COUNT")) ;
                    break;
            }

        }
        return R.success(statistics);
    }
}
