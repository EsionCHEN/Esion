package io.renren.modules.demo.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.util.Date;

/**
 * 
 *
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-20
 */
@Data
public class DynamicTowerStaitcExcel {
    @Excel(name = "")
    private Long id;
    @Excel(name = "")
    private Integer stationId;
    @Excel(name = "塔名")
    private String towerName;
    @Excel(name = "")
    private Integer alertCount;
    @Excel(name = "")
    private Integer alertDuration;
    @Excel(name = "垂直度")
    private String verticality;
    @Excel(name = "震动周期")
    private String vibrationPeriod;
    @Excel(name = "应力")
    private String stress;
    @Excel(name = "风速")
    private String windSpeed;
    @Excel(name = "风险值")
    private String riskScore;
    @Excel(name = "一级告警")
    private Integer alertLevel1;
    @Excel(name = "二级告警")
    private Integer alertLevel2;
    @Excel(name = "三级告警")
    private Integer alertLevel3;
    @Excel(name = "")
    private Date staticDay;
    @Excel(name = "")
    private Date createDate;

}