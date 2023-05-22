package io.renren.common.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 @Description: 枚举
 @Author: ESION.CT
 @Date: 2023/5/20 19:43
 */

public interface TowerEnum {

    @Getter
    @AllArgsConstructor
    enum TowerType{
        DAN_T(1,"2#单频天线塔","930","2#单频塔"),
        DOUBLE_T(2,"1#双频天线塔","930","1#双频塔"),
        SIG_T(3,"100m自立塔","924",""),
        LA_T(4,"76m拉线塔","924",""),
        DAN_P(5,"单频塔","917",""),
        DOUBLE_P(6,"双频塔","917",""),
        FECH_T(7,"多塔","","")
        ;
        private Integer code;
        private String name;
        private String tzname;
        private String cname;

    }

    @Getter
    @AllArgsConstructor
    enum TowerScondType{
        CZD(1,"垂直度"),
        YL(2,"应力"),
        LL(3,"拉力"),
        FS(4,"风速"),
        FXZ(5,"风险值"),
        ;
        private Integer code;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    enum AlertType{
        ONE(1,"一级预警","alert_level_1"),
        TWO(2,"二级预警","alert_level_2"),
        THREE(3,"三级预警","alert_level_3"),
        ;
        private Integer code;
        private String name;
        private String column;

        public static String getNameByDescription(String desc){
            List<AlertType> alertTypes = Arrays.asList(AlertType.values());
            for (AlertType alertType : alertTypes) {
                if (desc.contains(alertType.getName())) {
                    return alertType.getName();
                }
            }
            return null;
        }

        public static Integer getCodeByDescription(String desc){
            List<AlertType> alertTypes = Arrays.asList(AlertType.values());
            for (AlertType alertType : alertTypes) {
                if (desc.contains(alertType.getName())) {
                    return alertType.getCode();
                }
            }
            return null;
        }
    }

    
}
