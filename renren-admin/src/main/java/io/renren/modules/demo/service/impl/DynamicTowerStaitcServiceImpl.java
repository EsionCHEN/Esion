package io.renren.modules.demo.service.impl;

import cn.hutool.json.JSONUtil;
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
import io.renren.modules.demo.entity.DynamicTowerStaitcEntity;
import io.renren.modules.demo.service.DynamicTowerDayService;
import io.renren.modules.demo.service.DynamicTowerSecondService;
import io.renren.modules.demo.service.DynamicTowerStaitcService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DynamicTowerStaitcServiceImpl extends CrudServiceImpl<DynamicTowerStaitcDao, DynamicTowerStaitcEntity, DynamicTowerStaitcDTO> implements DynamicTowerStaitcService {

    @Autowired
    private DynamicTowerAlertDao alarmDao;
    @Autowired
    private DynamicTowerDayService dayService;
    @Autowired
    private DynamicTowerSecondService secondService;

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


    @Override
    public List<DynamicTowerStaitcDTO> collecct(String json) {
        String path = this.getClass().getClassLoader().getResource("file/xml.txt").getPath();
        try{
            // 创建文档解析的对象
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            // 解析文档，形成文档树，也就是生成Document对象
//            Document doc = builder.parse( new File(path));
            //TODO 静态测试文件数据
            json  = XmlToMap.readFileToString(path);
            if(StringUtils.isNotBlank(json)){
                //json数据切割
                List<String> jsonList = XmlToMap.jsonToSplit(json);
                for (String xml : jsonList) {
                    //数据解析
                    dataParseAndSax(xml);
                }
            }

        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }


    public void dataParseAndSax(String json){
        try {
            List<DynamicTowerStaitcDTO> retList = Lists.newArrayList();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(json)));
            Map<String, Object> map = XmlToMap.xmlToMap(doc.getDocumentElement());
            //            Map<String, Object> map = XmlUtil.xmlToMap(path);
            //测试json数据
            json = JSONUtil.toJsonStr(map);
            DynamicTowerStaitcDTO dto = new DynamicTowerStaitcDTO();
            Map<String, Object>  station = (Map)map.get("Station");
            //台站信息
            Map<String,String> stationInfo = (Map) station.get("StationInfo");
            if(Objects.nonNull(stationInfo) && stationInfo.size()!=0){
                String stationInfoStr = stationInfo.get("0");
                String[] split = stationInfoStr.split("\\|");
                //台站id
                String id = split[0];
                String name = split[1];
                if(Objects.nonNull(id)){
                    String[] splitStation = id.split("-");
//                    dto.setStationId(Integer.parseInt(splitStation[1]));
                    Integer code = Integer.parseInt(splitStation[1]);
                    if(TowerEnum.TowerType.DOUBLE_P.getCode().equals(code)){
                        //917->3
                        dto.setStationId(3);
                    }
                    if(TowerEnum.TowerType.DAN_T.getCode().equals(code)){
                        //930->16
                        dto.setStationId(16);
                    }
                    if(TowerEnum.TowerType.LA_T.getCode().equals(code)){
                        //924->10
                        dto.setStationId(10);
                    }
                }
            }
            List<Map<String, Object>> towers = (List)station.get("Tower");
            towers.stream().forEach(it->{
                try{
                    //根据每个信号源进行组装
                    Map<String,String> towerInfo = (Map)it.get("TowerInfo");
                    if(Objects.nonNull(towerInfo) && towerInfo.size()!=0){
                        String towoerInfoStr = towerInfo.get("0");
                        dto.setTowerName(towoerInfoStr.split("\\|")[1]);
                        if (towoerInfoStr.contains(TowerEnum.TowerType.DAN_T.getName()) ||
                                towoerInfoStr.contains(TowerEnum.TowerType.DOUBLE_T.getName()) ||
                                towoerInfoStr.contains(TowerEnum.TowerType.SIG_T.getName()) ||
                                towoerInfoStr.contains(TowerEnum.TowerType.LA_T.getName()) ||
                                towoerInfoStr.contains(TowerEnum.TowerType.DAN_P.getName()) ||
                                towoerInfoStr.contains(TowerEnum.TowerType.DOUBLE_P.getName())
                        ) {
                            Map<String, List>  second = (Map)it.get("Second");
                            if(Objects.nonNull(second) && second.size()!=0){
                                List<Map<String,String>> paramList = (List)second.get("Param");
                                if(!paramList.isEmpty()){
                                    //开始解析

                                    for (int i = 0; i <paramList.size() ; i++) {

                                        Map<String, String> mapKey = paramList.get(i);

                                        if(Objects.nonNull(mapKey) && mapKey.size()!=0){

                                            String param = (String) mapKey.get("0");

                                            if(param.contains(TowerEnum.TowerScondType.CZD.getName())||
                                                    param.contains(TowerEnum.TowerScondType.YL.getName()) ||
                                                    param.contains(TowerEnum.TowerScondType.LL.getName()) ||
                                                    param.contains(TowerEnum.TowerScondType.FS.getName()) ||
                                                    param.contains(TowerEnum.TowerScondType.FXZ.getName())
                                            ){
                                                String[] splitArr = param.split("\\|");
                                                List<String> collect = Arrays.stream(splitArr).collect(Collectors.toList());
                                                if(!collect.isEmpty()){
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
                                                        if(month.length() == 1){
                                                            dateStr = splitDate[0] + "/" + "0" + month + "/" + splitDate[2];
                                                        }
                                                        dto.setCreateDate(dateFormat.parse(dateStr));
                                                    } catch (Exception e) {
                                                        dto.setCreateDate(new Date());
                                                    }
                                                    if(scondType.contains(TowerEnum.TowerScondType.CZD.getName())){
                                                        //垂直度
                                                        dto.setVerticality(avg);
                                                    }
                                                    if(scondType.contains(TowerEnum.TowerScondType.YL.getName())){
                                                        //应力
                                                        dto.setStress(avg);
                                                    }
                                                    if(scondType.contains(TowerEnum.TowerScondType.LL.getName())){
                                                        //拉力
                                                        dto.setVibrationPeriod(avg);
                                                    }
                                                    if(scondType.contains(TowerEnum.TowerScondType.FS.getName())){
                                                        //风速
                                                        dto.setWindSpeed(avg);
                                                    }
                                                    if(scondType.contains(TowerEnum.TowerScondType.FXZ.getName())){
                                                        //风险值
                                                        dto.setRiskScore(avg);
                                                    }
                                                    DynamicTowerStaitcEntity dynamicTowerStaitcEntity = new DynamicTowerStaitcEntity();
                                                    try {
                                                        BeanUtils.copyProperties(dynamicTowerStaitcEntity,dto);
                                                        LambdaQueryWrapper<DynamicTowerStaitcEntity> lambdaQueryWrapper = new LambdaQueryWrapper();
                                                        lambdaQueryWrapper.eq(DynamicTowerStaitcEntity::getCreateDate,dto.getCreateDate())
                                                                .eq(DynamicTowerStaitcEntity::getStationId,dto.getStationId())
                                                                .eq(DynamicTowerStaitcEntity::getTowerName,dto.getTowerName())
                                                                .orderByAsc(DynamicTowerStaitcEntity::getCreateDate)
                                                                .last("limit 1")
                                                        ;
                                                        DynamicTowerStaitcEntity entity = baseDao.selectOne(lambdaQueryWrapper);
                                                        if(Objects.isNull(entity)){
                                                            //落表
                                                            baseDao.insert(dynamicTowerStaitcEntity);
                                                        }
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
                }catch (Exception e){
                    //dom子节点无内容默认跳过
                    log.error(e.getMessage());
                }
            });
            System.out.println("读取完毕，开始写入...");
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}