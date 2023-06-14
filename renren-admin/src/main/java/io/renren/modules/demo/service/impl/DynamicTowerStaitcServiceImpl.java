package io.renren.modules.demo.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.common.utils.TowerEnum;
import io.renren.common.utils.XmlToMap;
import io.renren.modules.demo.dao.DynamicTowerAlertDao;
import io.renren.modules.demo.dao.DynamicTowerStaitcDao;
import io.renren.modules.demo.dto.DynamicTowerSecondDTO;
import io.renren.modules.demo.dto.DynamicTowerStaitcDTO;
import io.renren.modules.demo.entity.DynamicTowerAlertEntity;
import io.renren.modules.demo.entity.DynamicTowerSecondEntity;
import io.renren.modules.demo.entity.DynamicTowerStaitcEntity;
import io.renren.modules.demo.service.DynamicTowerDayService;
import io.renren.modules.demo.service.DynamicTowerSecondService;
import io.renren.modules.demo.service.DynamicTowerStaitcService;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

/**
 * 
 *
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-20
 */
@Service
@Slf4j
public class DynamicTowerStaitcServiceImpl extends CrudServiceImpl<DynamicTowerStaitcDao, DynamicTowerStaitcEntity, DynamicTowerStaitcDTO> implements DynamicTowerStaitcService {

    @Autowired
    private DynamicTowerAlertDao alarmDao;
    @Autowired
    private DynamicTowerDayService dayService;
    @Autowired
    private DynamicTowerSecondService secondService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public QueryWrapper<DynamicTowerStaitcEntity> getWrapper(Map<String, Object> params){
        String id = (String)params.get("id");

        QueryWrapper<DynamicTowerStaitcEntity> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(id), "id", id);

        return wrapper;
    }

    /**
     @Description: 汇总
     @Author: ESION.CT
     @Date: 2023/5/20 20:12
     */
    @Override
    @Transactional(rollbackFor=Exception.class)
    public List<DynamicTowerStaitcDTO> sum() {
      
        //秒数据
        List<DynamicTowerSecondDTO> secondList = secondService.list(new HashMap<>());

        secondList.stream().forEach(i->{
            Long min = 0L;
            Long max = 0L;
            DynamicTowerStaitcEntity entity = new DynamicTowerStaitcEntity();
            entity.setStationId(i.getStationId());
            entity.setTowerName(i.getTowerName());
            entity.setCreateDate(new Date());
            entity.setStaticDay(i.getCreateDate());
            entity.setRiskScore(i.getRiskScore());
            entity.setStress(i.getStress());
            entity.setWindSpeed(i.getWindSpeed());
            entity.setVerticality(i.getVerticality());
            entity.setVibrationPeriod(i.getVibrationPeriod());
            LambdaQueryWrapper<DynamicTowerAlertEntity> qw = new LambdaQueryWrapper<>();
            qw.eq(DynamicTowerAlertEntity::getStationId,i.getStationId())
                    .eq(DynamicTowerAlertEntity::getTowerName,i.getTowerName()).orderByDesc(DynamicTowerAlertEntity::getCreateDate);
            //当前台站-塔台报警记录
            List<DynamicTowerAlertEntity> dynamicTowerAlertEntities = alarmDao.selectList(qw);
            entity.setAlertCount(dynamicTowerAlertEntities.size());
            if(!dynamicTowerAlertEntities.isEmpty()){
                Integer code = TowerEnum.AlertType.getCodeByDescription(dynamicTowerAlertEntities.get(0).getDescription());
                LambdaUpdateWrapper<DynamicTowerStaitcEntity> up = new LambdaUpdateWrapper<>();
                switch (code){
                    case 1:{
                        //查询一级报警
                        LambdaQueryWrapper<DynamicTowerAlertEntity> a = new LambdaQueryWrapper<>();
                        a.eq(DynamicTowerAlertEntity::getStationId,i.getStationId())
                                .eq(DynamicTowerAlertEntity::getTowerName,i.getTowerName())
                                .eq(DynamicTowerAlertEntity::getGrade,TowerEnum.AlertType.ONE.getName());
                        //更新数据
                        Long aLong = alarmDao.selectCount(a);
                        if(aLong != 0 ){
                            entity.setAlertLevel1(aLong.intValue());
                        }

                        break;
                    }
                    case 2:{
                        //查询二级报警
                        LambdaQueryWrapper<DynamicTowerAlertEntity> a = new LambdaQueryWrapper<>();
                        a.eq(DynamicTowerAlertEntity::getStationId,i.getStationId())
                                .eq(DynamicTowerAlertEntity::getTowerName,i.getTowerName())
                                .eq(DynamicTowerAlertEntity::getGrade,TowerEnum.AlertType.TWO.getName());
                        //更新数据
                        Long aLong = alarmDao.selectCount(a);
                        if(aLong != 0 ){
                            entity.setAlertLevel2(aLong.intValue());
                        }
                        break;
                    }
                    case 3:{
                        //查询三级报警
                        LambdaQueryWrapper<DynamicTowerAlertEntity> a = new LambdaQueryWrapper<>();
                        a.eq(DynamicTowerAlertEntity::getStationId,i.getStationId())
                                .eq(DynamicTowerAlertEntity::getTowerName,i.getTowerName())
                                .eq(DynamicTowerAlertEntity::getGrade,TowerEnum.AlertType.THREE.getName());
                        //更新数据
                        Long aLong = alarmDao.selectCount(a);
                        if(aLong != 0 ){
                            entity.setAlertLevel3(aLong.intValue());
                        }
                        break;
                    }
                    default:
                        break;
                }

            }
            Optional<DynamicTowerAlertEntity> minEntity = dynamicTowerAlertEntities.stream().min(Comparator.comparing(DynamicTowerAlertEntity::getCreateDate));
            if(minEntity.isPresent()){
                DynamicTowerAlertEntity dynamicTowerAlertEntity = minEntity.get();
                //获取最早时间
                 min = dynamicTowerAlertEntity.getCreateDate().getTime();
            }
            Optional<DynamicTowerAlertEntity> maxEntity = dynamicTowerAlertEntities.stream().max(Comparator.comparing(DynamicTowerAlertEntity::getCreateDate));
            if(maxEntity.isPresent()){
                DynamicTowerAlertEntity dynamicTowerAlertEntity = maxEntity.get();
                //获取最晚时间
                max= dynamicTowerAlertEntity.getCreateDate().getTime();
            }
            if(min!=null && max!=null){
                Long jg = (max - min) / 1000;
                entity.setAlertDuration(jg.intValue());
            }
            baseDao.insert(entity);
        });
        return null;
    }

    /**
     * @Description: 采集秒级数据
     * @Author: ESION.CT
     * @Date: 2023/5/20 20:12
     */
    @Override
    public List<DynamicTowerSecondEntity> collecct(String json) {
        try {
            // 创建文档解析的对象
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
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
                        Map<String, List> second = (Map) it.get("Minute");
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
                    redisSb.append("分:");
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
                    DynamicTowerSecondEntity dynamicTowerSecondEntity = secondService.getOne(qw);
                    if(Objects.isNull(dynamicTowerSecondEntity)){
                        //新增
                        if(!StringUtils.equalsIgnoreCase(it.getTowerName(),TowerEnum.TowerType.FECH_T.getName())){
                            it.setCreateDate(new Date());
                            if(Objects.nonNull(it.getStationId()) && Objects.nonNull(it.getTowerName())){
                                secondService.insert(it);
                            }
                        }else{
                            LambdaUpdateWrapper<DynamicTowerSecondEntity> up = new LambdaUpdateWrapper<>();
                            up.set(DynamicTowerSecondEntity::getWindSpeed,it.getWindSpeed());
                            up.eq(DynamicTowerSecondEntity::getStationId,it.getStationId());
                            secondService.update(null,up);
                        }
                    }else{
                        //更新
                        if(!StringUtils.equalsIgnoreCase(it.getTowerName(),TowerEnum.TowerType.FECH_T.getName())){
                            it.setId(dynamicTowerSecondEntity.getId());
                            secondService.updateById(it);
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
                            Map<String, List> comparison = (Map) it.get("Minute");
                            String vert = "0";
                            if (Objects.nonNull(comparison) && comparison.size() != 0) {
                                List<Map<String, String>> paramList = new ArrayList<>();
                                try {
                                    paramList = (List) comparison.get("Param");
                                } catch (Exception e) {
                                    Map<String,String> paramMap = (Map)comparison.get("Param");
                                    paramList.add(paramMap);
                                }
                                if (!paramList.isEmpty()) {
                                    for (int i = 0; i < paramList.size(); i++) {
                                        Map<String, String> mapKey = paramList.get(i);
                                        String s = mapKey.get("0");
//                                        if(s.contains("塔顶垂直度")){
//                                            System.out.println(s);
//                                        }
                                        if (StringUtils.isNotBlank(s) && s.contains("塔顶垂直度")) {
                                            String[] split = s.split("\\|");
//                                            if("H2H".equals(split[3])){
                                            //塔顶垂直度
                                            vert = split[8];
                                            redisSb.append(split[2]);
                                            redisTemplate.opsForValue().set(redisSb.toString(),vert);
//                                            if (!"0.0".equals(vert)) {
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
                                            LambdaQueryWrapper<DynamicTowerSecondEntity> upp = new LambdaQueryWrapper<>();
//                                                upp.set(DynamicTowerSecondEntity::getVerticality, vert);
                                            //查询是否已经存在此塔
                                            upp.eq(DynamicTowerSecondEntity::getStationId, stationId);
                                            upp.eq(DynamicTowerSecondEntity::getTowerName, dto.getTowerName()).last("limit 1");
                                            DynamicTowerSecondEntity dynamicTowerSecondEntity = secondService.getOne(upp);
                                            if (Objects.nonNull(dynamicTowerSecondEntity)) {
                                                if(StringUtils.isNotBlank(vert) && !"0".equals(vert)){
                                                    dynamicTowerSecondEntity.setVerticality(vert);
                                                    secondService.updateById(dynamicTowerSecondEntity);
                                                }
                                            }else{
                                                DynamicTowerSecondEntity newEntity = new DynamicTowerSecondEntity();
                                                BeanUtils.copyProperties(newEntity,dto);
                                                newEntity.setStationId(stationId);
                                                newEntity.setVerticality(vert);
                                                secondService.insert(newEntity);
                                            }
                                        }

                                    }
                                }
                            }
                            try {
                                //阻塞一秒
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    //dom子节点无内容默认跳过
                }
            });
            System.out.println("================="+stationInfoStr+"数据解析完毕,等待下次指令===================");
            return retList;

        } catch (SAXException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage());
        }finally {
            System.out.println("检测是否存在可释放资源....");
        }
        return null;
    }
}