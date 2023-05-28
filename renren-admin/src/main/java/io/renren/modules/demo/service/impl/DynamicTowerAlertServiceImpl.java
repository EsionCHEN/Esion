package io.renren.modules.demo.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiniu.util.Json;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.common.utils.JsonUtils;
import io.renren.common.utils.TowerEnum;
import io.renren.common.utils.XmlToMap;
import io.renren.modules.demo.dao.DynamicTowerAlertDao;
import io.renren.modules.demo.dto.DynamicTowerAlertDTO;
import io.renren.modules.demo.dto.DynamicTowerDayDTO;
import io.renren.modules.demo.entity.DynamicTowerAlertEntity;
import io.renren.modules.demo.entity.DynamicTowerDayEntity;
import io.renren.modules.demo.service.DynamicTowerAlertService;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-20
 */
@Service
@Slf4j
public class DynamicTowerAlertServiceImpl extends CrudServiceImpl<DynamicTowerAlertDao, DynamicTowerAlertEntity, DynamicTowerAlertDTO> implements DynamicTowerAlertService {

    @Override
    public QueryWrapper<DynamicTowerAlertEntity> getWrapper(Map<String, Object> params) {
        String id = (String) params.get("id");

        QueryWrapper<DynamicTowerAlertEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(id), "id", id);

        return wrapper;
    }


    /**
     * @Description: 采集报警数据
     * @Author: ESION.CT
     * @Date: 2023/5/20 20:12
     */
    @Override
    public List<DynamicTowerAlertEntity> collecct(String json) {
        if ("FALSE".equals(json)) {
            //TODO 静态测试文件数据
            String folder = "file/";
            String fileName = "924x.txt";
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
    public List<DynamicTowerAlertEntity> dataParseAndSax(String json) {
        try {
            List<DynamicTowerAlertEntity> retList = Lists.newArrayList();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(json)));
            Map<String, Object> map = XmlToMap.xmlToMap(doc.getDocumentElement());
            //            Map<String, Object> map = XmlUtil.xmlToMap(path);
            //测试json数据
            json = JSONUtil.toJsonStr(map);
            log.info("===================数据解析成功......=======================");
            DynamicTowerAlertDTO dto = new DynamicTowerAlertDTO();
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
                            Map<String, List> alert = (Map) it.get("Alert");
                            if (Objects.nonNull(alert) && alert.size() != 0) {

                                List<Map<String,String>> messageList = new ArrayList<>();
                                try {
                                    messageList = (List) alert.get("Message");
                                } catch (Exception e) {
                                    Map<String,String> message1 = (Map)alert.get("Message");
                                    messageList.add(message1);
                                }

//                                List<Map<String, String>> messageList = (List) alert.get("Message");
                                if (!messageList.isEmpty()) {
                                    //开始解析

                                    for (int i = 0; i < messageList.size(); i++) {

                                        Map<String, String> mapKey = messageList.get(i);

                                        if (Objects.nonNull(mapKey) && mapKey.size() != 0) {
                                            String message = (String) mapKey.get("0");
                                            String[] splitArr = message.split("\\|");
                                            List<String> collect = Arrays.stream(splitArr).collect(Collectors.toList());
                                            if (!collect.isEmpty()) {
                                                //判断所属类型
                                                String id = collect.get(1);
                                                String desc = collect.get(2);
                                                String dateStr = collect.get(3);
                                                dto.setNo(id);
                                                String grade = TowerEnum.AlertType.getNameByDescription(desc);
                                                if(Objects.nonNull(grade)){
                                                    dto.setDescription(desc);
                                                    dto.setGrade(grade);
                                                }
                                                SimpleDateFormat dateFormat =
                                                        new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                                                //时间戳
                                                String[] splitDate = dateStr.split("/");
                                                String month = splitDate[1];
                                                String day = splitDate[2];
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
                                                DynamicTowerAlertEntity dynamicTowerStaitcEntity = new DynamicTowerAlertEntity();

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
                                                    LambdaQueryWrapper<DynamicTowerAlertEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
                                                    lambdaQueryWrapper.eq(DynamicTowerAlertEntity::getStationName, dto.getStationName())
                                                            .eq(DynamicTowerAlertEntity::getStationId, stationId)
                                                            .eq(DynamicTowerAlertEntity::getNo, dto.getNo())
                                                            .orderByAsc(DynamicTowerAlertEntity::getCreateDate)
                                                            .last("limit 1")
                                                    ;
                                                    DynamicTowerAlertEntity entity = baseDao.selectOne(lambdaQueryWrapper);
                                                    dynamicTowerStaitcEntity.setStationId(stationId);
                                                    if (Objects.isNull(entity)) {
                                                        //落表
                                                        log.info("===================数据正在写入......=======================");
                                                        baseDao.insert(dynamicTowerStaitcEntity);
                                                        log.info("===================数据写入成功=======================");
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


    @Override
    public void deleteAll() {
        //清理一月前的数据
       baseDao.deleteAll();
    }
}