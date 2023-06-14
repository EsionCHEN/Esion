package io.renren.modules.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.renren.common.redis.RedisUtils;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.common.utils.TowerEnum;
import io.renren.common.utils.XmlToMap;
import io.renren.modules.demo.dao.DynamicTowerSecondDao;
import io.renren.modules.demo.dto.DynamicTowerSecondDTO;
import io.renren.modules.demo.entity.DynamicTowerSecondEntity;
import io.renren.modules.demo.service.DynamicTowerSecondService;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-21
 */
@Service
@Slf4j
public class DynamicTowerSecondServiceImpl extends CrudServiceImpl<DynamicTowerSecondDao, DynamicTowerSecondEntity, DynamicTowerSecondDTO> implements DynamicTowerSecondService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public QueryWrapper<DynamicTowerSecondEntity> getWrapper(Map<String, Object> params) {
        String id = (String) params.get("id");

        QueryWrapper<DynamicTowerSecondEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(id), "id", id);

        return wrapper;
    }

    public String hello() {
        return "===================================";
    }

    @Override
    public DynamicTowerSecondEntity getOne(Wrapper<DynamicTowerSecondEntity> queryWrapper) {
        return baseDao.selectOne(queryWrapper);
    }

    /**
     * @Description: 采集秒级数据
     * @Author: ESION.CT
     * @Date: 2023/5/20 20:12
     */
    @Override
    public List<DynamicTowerSecondEntity> collecct(String json) {
        if ("FALSE".equals(json)) {
            //TODO 静态测试文件数据
            String folder = "file/";
            String fileName = "930x.txt";
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
                    dataParseAndSaxNew(xml);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }


    @Transactional(rollbackFor = Exception.class)
    @Synchronized
    public List<DynamicTowerSecondEntity> dataParseAndSaxNew(String json) {
        try {
            List<DynamicTowerSecondEntity> retList = Lists.newArrayList();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(json)));
            Map<String, Object> map = XmlToMap.xmlToMap(doc.getDocumentElement());
            //测试json数据
            json = JSONUtil.toJsonStr(map);
            log.info("===================数据解析成功......=======================");
            Map<String, Object> station = (Map) map.get("Station");
            String stationName = null;
            Integer stationCode = null;
            Integer stationId = null;
            //台站信息
            Map<String, String> stationInfo = (Map) station.get("StationInfo");
            String stationInfoStr = null;
            if (Objects.nonNull(stationInfo) && stationInfo.size() != 0) {
                stationInfoStr = stationInfo.get("0");
                String[] split = stationInfoStr.split("\\|");
                //台站id
                String id = split[0];
                String name = split[1];
                if (Objects.nonNull(id)) {
                    String[] splitStation = id.split("-");
                    Integer code = Integer.parseInt(splitStation[1]);
                    stationName = name;
                    stationCode = code;
                    stationId = code;

                }
            }
            List<Map<String, Object>> towers = (List) station.get("Tower");
            Integer finalStationId = stationId;
            String finalStationName = stationName;
            towers.stream().forEach(it -> {
                try {
                    DynamicTowerSecondDTO dto = new DynamicTowerSecondDTO();
                    StringBuffer redisSb = new StringBuffer();
                    redisSb.append(finalStationId);
                    redisSb.append(":");
                    dto.setStationId(finalStationId);
                    dto.setStationName(finalStationName);
                    //根据每个信号源进行组装
                    Map<String, String> towerInfo = (Map) it.get("TowerInfo");
                    //新记录
                    DynamicTowerSecondEntity nEntity = new DynamicTowerSecondEntity();
                    if (Objects.nonNull(towerInfo) && towerInfo.size() != 0) {
                        String towoerInfoStr = towerInfo.get("0");
                        dto.setTowerName(towoerInfoStr.split("\\|")[1]);
                        Map<String, List> second = (Map) it.get("Second");
                        String vert = "0";
                        try {
                            //阻塞一秒
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (Objects.nonNull(second) && second.size() != 0) {

                            List<Map<String, String>> paramList = new ArrayList<>();

                            try {
                                paramList = (List) second.get("Param");
                            } catch (Exception e) {
                                Map<String, String> paramMap = (Map) second.get("Param");
                                paramList.add(paramMap);
                            }

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
                                                String dateStr = collect.get(4);
                                                SimpleDateFormat dateFormat =
                                                        new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                                                try {
                                                    String[] splitDate = dateStr.split("/");
                                                    String month = splitDate[1];
                                                    if (month.length() == 1) {
                                                        dateStr = splitDate[0] + "/" + "0" + month + "/" + splitDate[2];
                                                    }
                                                    dto.setCreateDate(dateFormat.parse(dateStr));
                                                } catch (Exception e) {
                                                    dto.setCreateDate(new Date());
                                                }
                                                if (scondType.contains(TowerEnum.TowerScondType.CZD.getName())) {
                                                    //塔顶垂直度
                                                    dto.setVerticality(avg);
                                                }
                                                if (scondType.contains(TowerEnum.TowerScondType.YL.getName())) {
                                                    //应力
                                                    dto.setStress(avg);
                                                }
                                                if (scondType.contains(TowerEnum.TowerScondType.LL.getName())) {
                                                    //拉力
                                                    dto.setVibrationPeriod(avg);
                                                }
                                                if (scondType.contains(TowerEnum.TowerScondType.FXZ.getName())) {
                                                    //风险值
                                                    dto.setRiskScore(avg);
                                                }
                                                if (scondType.contains(TowerEnum.TowerScondType.FS.getName())) {
                                                    //风速
                                                    dto.setWindSpeed(avg);

                                                }
                                                nEntity.setTowerName(dto.getTowerName());
                                                nEntity.setStationId(TowerEnum.CodeType.getZCodeByCode(dto.getStationId()));
                                                nEntity.setStationName(dto.getStationName());
                                                parseEntity(nEntity,dto);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    String jsonStr = JSON.toJSONString(nEntity);
                    System.out.println(jsonStr);
                    redisSb.append("秒:");
                    redisSb.append(nEntity.getTowerName());
                    if(Objects.nonNull(nEntity.getTowerName())){
                        redisTemplate.opsForValue().set(redisSb.toString(),jsonStr);
                        retList.add(nEntity);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                    //dom子节点无内容默认跳过
                }
            });
            if(CollectionUtils.isNotEmpty(retList)){
                retList.stream().forEach(it->{
                    LambdaQueryWrapper<DynamicTowerSecondEntity> qw = new LambdaQueryWrapper<>();
                    qw.eq(DynamicTowerSecondEntity::getStationId,it.getStationId())
                            .eq(DynamicTowerSecondEntity::getTowerName,it.getTowerName())
                            .last("limit 1");
                    DynamicTowerSecondEntity dynamicTowerSecondEntity = baseDao.selectOne(qw);
                    if(Objects.isNull(dynamicTowerSecondEntity)){
                        //新增
                        if(!StringUtils.equalsIgnoreCase(it.getTowerName(),TowerEnum.TowerType.FECH_T.getName())){
                            it.setCreateDate(new Date());
                            if(Objects.nonNull(it.getStationId()) && Objects.nonNull(it.getTowerName())){
                                baseDao.insert(it);
                            }
                        }else{
                            LambdaUpdateWrapper<DynamicTowerSecondEntity> up = new LambdaUpdateWrapper<>();
                            up.set(DynamicTowerSecondEntity::getWindSpeed,it.getWindSpeed());
                            up.eq(DynamicTowerSecondEntity::getStationId,it.getStationId());
                            baseDao.update(null,up);
                        }
                    }else{
                        //更新
                        if(!StringUtils.equalsIgnoreCase(it.getTowerName(),TowerEnum.TowerType.FECH_T.getName())){
                            it.setId(dynamicTowerSecondEntity.getId());
                            baseDao.updateById(it);
                        }
                    }

                });
            }
            System.out.println("=================" + stationInfoStr + "数据解析完毕,等待下次指令===================");
            return retList;

        } catch (SAXException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            System.out.println("检测是否存在可释放资源....");
        }
        return null;
    }

    public DynamicTowerSecondEntity parseEntity(DynamicTowerSecondEntity source,DynamicTowerSecondDTO target){
        if(StringUtils.isNotBlank(target.getStress())){
            source.setStress(target.getStress());
        }
        if(StringUtils.isNotBlank(target.getRiskScore())){
            source.setRiskScore(target.getRiskScore());
        }
        if(StringUtils.isNotBlank(target.getVerticality())){
            source.setVerticality(target.getVerticality());
        }
        if(StringUtils.isNotBlank(target.getVibrationPeriod())){
            source.setVibrationPeriod(target.getVibrationPeriod());
        }
        if(StringUtils.isNotBlank(target.getWindSpeed())){
            source.setWindSpeed(target.getWindSpeed());
        }

        return source;
    }

    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    @Synchronized
    public List<DynamicTowerSecondEntity> dataParseAndSax(String json) {
        try {
            List<DynamicTowerSecondEntity> retList = Lists.newArrayList();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(json)));
            Map<String, Object> map = XmlToMap.xmlToMap(doc.getDocumentElement());
            //            Map<String, Object> map = XmlUtil.xmlToMap(path);
            //测试json数据
            json = JSONUtil.toJsonStr(map);
            log.info("===================数据解析成功......=======================");
            DynamicTowerSecondDTO dto = new DynamicTowerSecondDTO();
            Map<String, Object> station = (Map) map.get("Station");
            StringBuffer redisSb = new StringBuffer();
            //台站信息
            Map<String, String> stationInfo = (Map) station.get("StationInfo");
            String stationInfoStr = null;
            if (Objects.nonNull(stationInfo) && stationInfo.size() != 0) {
                stationInfoStr = stationInfo.get("0");
                String[] split = stationInfoStr.split("\\|");
                //台站id
                String id = split[0];
                String name = split[1];
                if (Objects.nonNull(id)) {
                    String[] splitStation = id.split("-");
                    Integer code = Integer.parseInt(splitStation[1]);
                    dto.setStationId(code);
                    dto.setStationName(name);
                    redisSb.append(code);
                    redisSb.append(":");
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
                        if (
                                (towoerInfoStr.contains(TowerEnum.TowerType.DAN_T.getCname()) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.DAN_T.getTzname())) ||
                                        (towoerInfoStr.contains(TowerEnum.TowerType.DOUBLE_T.getCname()) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.DOUBLE_T.getTzname())) ||
//                                ((towoerInfoStr.contains(TowerEnum.TowerType.DAN_T.getName()) || towoerInfoStr.contains(TowerEnum.TowerType.DAN_T.getCname())) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.DAN_T.getTzname())) ||
//                                ((towoerInfoStr.contains(TowerEnum.TowerType.DOUBLE_T.getCname()) || towoerInfoStr.contains(TowerEnum.TowerType.DOUBLE_T.getCname())) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.DOUBLE_T.getTzname())) ||
                                        (towoerInfoStr.contains(TowerEnum.TowerType.SIG_T.getName()) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.SIG_T.getTzname())) ||
                                        (towoerInfoStr.contains(TowerEnum.TowerType.LA_T.getName()) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.LA_T.getTzname())) ||
                                        (towoerInfoStr.contains(TowerEnum.TowerType.DAN_P.getName()) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.DAN_P.getTzname())) ||
                                        (towoerInfoStr.contains(TowerEnum.TowerType.DOUBLE_P.getName()) && String.valueOf(dto.getStationId()).equals(TowerEnum.TowerType.DOUBLE_P.getTzname())) ||
                                        towoerInfoStr.contains(TowerEnum.TowerType.FECH_T.getName())
                        ) {
                            Map<String, List> second = (Map) it.get("Second");
                            String vert = "0";
                            try {
                                //阻塞一秒
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (Objects.nonNull(second) && second.size() != 0) {

                                List<Map<String, String>> paramList = new ArrayList<>();

                                try {
                                    paramList = (List) second.get("Param");
                                } catch (Exception e) {
                                    Map<String, String> paramMap = (Map) second.get("Param");
                                    paramList.add(paramMap);
                                }

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
                                                    redisSb.append(scondType);
                                                    //平均值
                                                    String avg = collect.get(8);
                                                    String dateStr = collect.get(4);
                                                    SimpleDateFormat dateFormat =
                                                            new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                                                    try {
                                                        String[] splitDate = dateStr.split("/");
                                                        String month = splitDate[1];
                                                        if (month.length() == 1) {
                                                            dateStr = splitDate[0] + "/" + "0" + month + "/" + splitDate[2];
                                                        }
                                                        dto.setCreateDate(dateFormat.parse(dateStr));
                                                    } catch (Exception e) {
                                                        dto.setCreateDate(new Date());
                                                    }
                                                    if (scondType.contains(TowerEnum.TowerScondType.CZD.getName())) {
                                                        //塔顶垂直度
                                                        dto.setVerticality(avg);
                                                    }
                                                    if (scondType.contains(TowerEnum.TowerScondType.YL.getName())) {
                                                        //应力
                                                        dto.setStress(avg);
                                                    }
                                                    if (scondType.contains(TowerEnum.TowerScondType.LL.getName())) {
                                                        //拉力
                                                        dto.setVibrationPeriod(avg);
                                                    }
                                                    if (scondType.contains(TowerEnum.TowerScondType.FXZ.getName())) {
                                                        //风险值
                                                        dto.setRiskScore(avg);
                                                    }
                                                    if (scondType.contains(TowerEnum.TowerScondType.FS.getName())) {
                                                        //风速
                                                        dto.setWindSpeed(avg);

                                                    }

                                                    DynamicTowerSecondEntity dynamicTowerStaitcEntity = new DynamicTowerSecondEntity();


                                                    if (Objects.nonNull(dto.getWindSpeed())) {
                                                        //多塔
                                                        try {
                                                            Integer stationId = dto.getStationId();

                                                            if (Integer.parseInt(TowerEnum.TowerType.DOUBLE_P.getTzname()) == stationId) {
                                                                //917->3
                                                                stationId = 3;
                                                            }
                                                            if (Integer.parseInt(TowerEnum.TowerType.DAN_T.getTzname()) == stationId) {
                                                                //930->16
                                                                stationId = 16;
                                                            }
                                                            if (Integer.parseInt(TowerEnum.TowerType.LA_T.getTzname()) == stationId) {
                                                                //924->10
                                                                stationId = 10;
                                                            }
                                                            BeanUtils.copyProperties(dynamicTowerStaitcEntity, dto);
                                                            LambdaQueryWrapper<DynamicTowerSecondEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
                                                            lambdaQueryWrapper.eq(DynamicTowerSecondEntity::getStationName, dto.getStationName())
                                                                    .eq(DynamicTowerSecondEntity::getStationId, stationId)
                                                                    .orderByAsc(DynamicTowerSecondEntity::getCreateDate)
                                                            ;
                                                            List<DynamicTowerSecondEntity> entity = baseDao.selectList(lambdaQueryWrapper);
                                                            dynamicTowerStaitcEntity.setStationId(stationId);
                                                            if (entity.isEmpty()) {
                                                                //落表
                                                                log.info("===================数据正在写入......=======================");
                                                                //双塔写入
                                                                saveTwoTower(dto);
                                                                log.info("===================数据写入成功=======================");
                                                            } else {
                                                                //多塔更新
                                                                LambdaUpdateWrapper<DynamicTowerSecondEntity> up = new LambdaUpdateWrapper<>();
                                                                up.set(DynamicTowerSecondEntity::getWindSpeed, dto.getWindSpeed());
                                                                up.eq(DynamicTowerSecondEntity::getStationName, dto.getStationName())
                                                                        .eq(DynamicTowerSecondEntity::getStationId, stationId);
                                                                log.info("===================数据正在更新......=======================");
                                                                baseDao.update(null, up);
                                                                log.info("===================数据更新成功=======================");
                                                            }
                                                        } catch (IllegalAccessException e) {
                                                            log.error(e.getMessage());
                                                        } catch (InvocationTargetException e) {
                                                            log.error(e.getMessage());
                                                        }
                                                    } else {
                                                        //单塔
                                                        try {

                                                            Integer stationId = dto.getStationId();

                                                            if (Integer.parseInt(TowerEnum.TowerType.DOUBLE_P.getTzname()) == stationId) {
                                                                //917->3
                                                                stationId = 3;
                                                            }
                                                            if (Integer.parseInt(TowerEnum.TowerType.DAN_T.getTzname()) == stationId) {
                                                                //930->16
                                                                stationId = 16;
                                                            }
                                                            if (Integer.parseInt(TowerEnum.TowerType.LA_T.getTzname()) == stationId) {
                                                                //924->10
                                                                stationId = 10;
                                                            }


                                                            BeanUtils.copyProperties(dynamicTowerStaitcEntity, dto);
                                                            LambdaQueryWrapper<DynamicTowerSecondEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
                                                            lambdaQueryWrapper.eq(DynamicTowerSecondEntity::getStationName, dto.getStationName())
                                                                    .eq(DynamicTowerSecondEntity::getStationId, stationId)
                                                                    .eq(DynamicTowerSecondEntity::getTowerName, dto.getTowerName())
                                                                    .orderByAsc(DynamicTowerSecondEntity::getCreateDate)
                                                                    .last("limit 1")
                                                            ;
                                                            DynamicTowerSecondEntity entity = baseDao.selectOne(lambdaQueryWrapper);
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
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                    //dom子节点无内容默认跳过
                }
            });
            System.out.println("=================" + stationInfoStr + "数据解析完毕,等待下次指令===================");
            return retList;

        } catch (SAXException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            System.out.println("检测是否存在可释放资源....");
        }
        return null;
    }


    public void saveTwoTower(DynamicTowerSecondDTO dto) {
        //自定义双塔
//        List<DynamicTowerSecondEntity> addList = Lists.newArrayList();
        try {
            //取多塔数据赋予单塔,双塔
            DynamicTowerSecondEntity dp = new DynamicTowerSecondEntity();
            DynamicTowerSecondEntity sp = new DynamicTowerSecondEntity();
            BeanUtils.copyProperties(dp, dto);
            BeanUtils.copyProperties(sp, dto);
            if (TowerEnum.TowerType.DAN_T.getTzname().equals(String.valueOf(dto.getStationId()))) {
                //930
                dp.setStationId(16);
                sp.setStationId(16);
                dp.setTowerName(TowerEnum.TowerType.DAN_T.getName());
                sp.setTowerName(TowerEnum.TowerType.DOUBLE_T.getName());
            }
            if (TowerEnum.TowerType.SIG_T.getTzname().equals(String.valueOf(dto.getStationId()))) {
                //924
                dp.setStationId(10);
                sp.setStationId(10);
                dp.setTowerName(TowerEnum.TowerType.SIG_T.getName());
                sp.setTowerName(TowerEnum.TowerType.LA_T.getName());
            }
            if (TowerEnum.TowerType.DAN_P.getTzname().equals(String.valueOf(dto.getStationId()))) {
                //917
                dp.setStationId(3);
                sp.setStationId(3);
                dp.setTowerName(TowerEnum.TowerType.DAN_P.getName());
                sp.setTowerName(TowerEnum.TowerType.DOUBLE_P.getName());
            }
            sp.setVerticality("0");
            dp.setVerticality("0");
            baseDao.insert(dp);
            baseDao.insert(sp);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage());
        } catch (InvocationTargetException e) {
            log.error(e.getMessage());
        }

    }


}