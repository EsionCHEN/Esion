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
public class DynamicTowerAlertExcel {
    @Excel(name = "")
    private Long id;
    @Excel(name = "")
    private Integer stationId;
    @Excel(name = "台站名")
    private String stationName;
    @Excel(name = "塔名")
    private String towerName;
    @Excel(name = "编号")
    private String no;
    @Excel(name = "描述")
    private String description;
    @Excel(name = "预警级别")
    private String grade;
    @Excel(name = "时间")
    private Date createDate;

}