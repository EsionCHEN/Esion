package io.renren.modules.demo.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.util.Date;

/**
 * 
 *
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-21
 */
@Data
public class DynamicTowerSecondExcel {
    @Excel(name = "")
    private Long id;
    @Excel(name = "")
    private Integer stationId;
    @Excel(name = "")
    private String stationName;
    @Excel(name = "塔名")
    private String towerName;
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
    @Excel(name = "")
    private Date createDate;

}