package io.renren.modules.demo.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.common.utils.TowerEnum;
import io.renren.common.utils.XmlToMap;
import io.renren.modules.demo.dao.DynamicTowerDayDao;
import io.renren.modules.demo.dto.DynamicTowerDayDTO;
import io.renren.modules.demo.dto.DynamicTowerSecondDTO;
import io.renren.modules.demo.entity.DynamicTowerDayEntity;
import io.renren.modules.demo.entity.DynamicTowerSecondEntity;
import io.renren.modules.demo.service.DynamicTowerDayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 
 *
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-20
 */
@Service
@Slf4j
public class DynamicTowerDayServiceImpl extends CrudServiceImpl<DynamicTowerDayDao, DynamicTowerDayEntity, DynamicTowerDayDTO> implements DynamicTowerDayService {

    @Override
    public QueryWrapper<DynamicTowerDayEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");

        QueryWrapper<DynamicTowerDayEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(id), "id", id);

        return wrapper;
    }

    /**
     * @Description: 采集日级数据
     * @Author: ESION.CT
     * @Date: 2023/5/20 20:12
     */
    @Override
    public List<DynamicTowerDayEntity> collecct(String json) {
        if ("FALSE".equals(json)) {
            //TODO 静态测试文件数据
            String folder = "file/";
            String fileName = "917x.txt";
            String path = this.getClass().getClassLoader().getResource(folder + fileName).getPath();
            json = XmlToMap.readFileToString(path);
            log.info("=================获取数据文件解析JSON格式完成==================");
        }
        try {
            // 创建文档解析的对象
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            // 解析文档，形成文档树，也就是生成Document对象
//            Document doc = builder.parse( new File(path));
            if (StringUtils.isNotBlank(json)) {
                //json数据切割
                List<String> jsonList = XmlToMap.jsonToSplit(json);
                for (String xml : jsonList) {
                    //数据解析
                    return dataParseAndSax(xml);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }


    @Transactional(rollbackFor=Exception.class)
    public List<DynamicTowerDayEntity> dataParseAndSax(String json) {
        try {
            List<DynamicTowerDayEntity> retList = Lists.newArrayList();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(json)));
            Map<String, Object> map = XmlToMap.xmlToMap(doc.getDocumentElement());
            //            Map<String, Object> map = XmlUtil.xmlToMap(path);
            //测试json数据
            json = JSONUtil.toJsonStr(map);
            log.info("===================数据解析成功......=======================");
            DynamicTowerDayDTO dto = new DynamicTowerDayDTO();
            Map<String, Object> station = (Map) map.get("Station");
            //台站信息
            Map<String, String> stationInfo = (Map) station.get("StationInfo");
            if (Objects.nonNull(stationInfo) && stationInfo.size() != 0) {
                String stationInfoStr = stationInfo.get("0");
                String[] split = stationInfoStr.split("\\|");
                //台站id
                String id = split[0];
                String name = split[1];
                if (Objects.nonNull(id)) {
                    String[] splitStation = id.split("-");
//                    dto.setStationId(Integer.parseInt(splitStation[1]));
                    Integer code = Integer.parseInt(splitStation[1]);
                    dto.setStationId(code);
                    dto.setStationName(name);
                }
            }
            List<Map<String, Object>> towers = (List) station.get("Tower");
            towers.stream().forEach(it -> {
                try {
                    //根据每个信号源进行组装
                    Map<String, String> towerInfo = (Map) it.get("TowerInfo");
                    if (Objects.nonNull(towerInfo) && towerInfo.size() != 0) {
                        String towoerInfoStr = towerInfo.get("0");
                        dto.setTowerName(towoerInfoStr.split("\\|")[1]);
                        if (((towoerInfoStr.contains(TowerEnum.TowerType.DAN_T.getName()) || towoerInfoStr.contains(TowerEnum.TowerType.DAN_T.getCname())) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.DAN_T.getTzname())) ||
                                ((towoerInfoStr.contains(TowerEnum.TowerType.DOUBLE_T.getCname()) || towoerInfoStr.contains(TowerEnum.TowerType.DOUBLE_T.getCname())) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.DOUBLE_T.getTzname())) ||
                                (towoerInfoStr.contains(TowerEnum.TowerType.SIG_T.getName()) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.SIG_T.getTzname())) ||
                                (towoerInfoStr.contains(TowerEnum.TowerType.LA_T.getName()) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.LA_T.getTzname())) ||
                                (towoerInfoStr.contains(TowerEnum.TowerType.DAN_P.getName()) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.DAN_P.getTzname())) ||
                                (towoerInfoStr.contains(TowerEnum.TowerType.DOUBLE_P.getName()) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.DOUBLE_P.getTzname()))
                        ) {
                            Map<String, List> day = (Map) it.get("Day");
                            if (Objects.nonNull(day) && day.size() != 0) {
                                List<Map<String, String>> paramList = (List) day.get("Param");
                                if (!paramList.isEmpty()) {
                                    //开始解析

                                    for (int i = 0; i < paramList.size(); i++) {

                                        Map<String, String> mapKey = paramList.get(i);

                                        if (Objects.nonNull(mapKey) && mapKey.size() != 0) {

                                            String param = (String) mapKey.get("0");

                                            if (param.contains(TowerEnum.TowerScondType.CZD.getName()) ||
                                                    param.contains(TowerEnum.TowerScondType.YL.getName()) ||
                                                    param.contains(TowerEnum.TowerScondType.LL.getName()) ||
                                                    param.contains(TowerEnum.TowerScondType.FS.getName()) ||
                                                    param.contains(TowerEnum.TowerScondType.FXZ.getName())
                                            ) {
                                                String[] splitArr = param.split("\\|");
                                                List<String> collect = Arrays.stream(splitArr).collect(Collectors.toList());
                                                if (!collect.isEmpty()) {
                                                    //判断所属类型
                                                    String scondType = collect.get(2);
                                                    //平均值
                                                    String avg = collect.get(8);
                                                    dto.setVal(avg);
                                                    String dateStr = collect.get(4);
                                                    SimpleDateFormat dateFormat =
                                                            new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                                                    String[] splitDate = dateStr.split("/");
                                                    String month = splitDate[1];
                                                    try {
                                                        if (month.length() == 1) {
                                                            month = "0" + month;
                                                        }
                                                        dateStr = splitDate[0] + "/" +  month + "/" + splitDate[2];
                                                        dto.setCreateDate(dateFormat.parse(dateStr));
                                                    } catch (Exception e) {
                                                        if (month.length() == 1) {
                                                            month = "0" + month;
                                                        }
                                                        dateStr = splitDate[0] + "/" +  month + "/" + splitDate[2].substring(1);
                                                        dto.setCreateDate(new Date());
                                                    }
                                                    DynamicTowerDayEntity dynamicTowerStaitcEntity = new DynamicTowerDayEntity();


                                                    Integer stationId = dto.getStationId();

                                                    if(Integer.parseInt(TowerEnum.TowerType.DOUBLE_P.getTzname()) == stationId){
                                                        //917->3
                                                        stationId = 3;
                                                    }
                                                    if(Integer.parseInt(TowerEnum.TowerType.DAN_T.getTzname()) == stationId){
                                                        //930->16
                                                        stationId = 16;
                                                    }
                                                    if(Integer.parseInt(TowerEnum.TowerType.LA_T.getTzname())==stationId){
                                                        //924->10
                                                        stationId = 10;
                                                    }

                                                    try {
                                                        BeanUtils.copyProperties(dynamicTowerStaitcEntity, dto);
                                                        LambdaQueryWrapper<DynamicTowerDayEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
                                                        lambdaQueryWrapper.eq(DynamicTowerDayEntity::getStationName, dto.getStationName())
                                                                .eq(DynamicTowerDayEntity::getStationId, stationId)
                                                                .eq(DynamicTowerDayEntity::getTowerName, dto.getTowerName())
                                                                .orderByAsc(DynamicTowerDayEntity::getCreateDate)
                                                                .last("limit 1")
                                                        ;
                                                        DynamicTowerDayEntity entity = baseDao.selectOne(lambdaQueryWrapper);
                                                        dynamicTowerStaitcEntity.setStationId(stationId);
                                                        if (Objects.isNull(entity)) {
                                                            //落表
                                                            log.info("===================数据正在写入......=======================");
                                                            baseDao.insert(dynamicTowerStaitcEntity);
                                                            log.info("===================数据写入成功=======================");
                                                        } else {
                                                            //更新
                                                            log.info("===================数据正在更新......=======================");
                                                            baseDao.update(dynamicTowerStaitcEntity, lambdaQueryWrapper);
                                                            log.info("===================数据更新成功=======================");
                                                        }
                                                        retList.add(dynamicTowerStaitcEntity);
                                                    } catch (IllegalAccessException e) {
                                                        log.error(e.getMessage());
                                                    } catch (InvocationTargetException e) {
                                                        log.error(e.getMessage());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    //dom子节点无内容默认跳过
                    log.error(e.getMessage());
                }
            });
            System.out.println("=================当前数据处理完成...等待服务下个指令===================");
            return retList;

        } catch (SAXException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage());
        }
        return null;
    }

}