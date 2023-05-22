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
public class DynamicTowerDayExcel {
    @Excel(name = "")
    private Long id;
    @Excel(name = "")
    private Integer stationId;
    @Excel(name = "台站名")
    private String stationName;
    @Excel(name = "塔名")
    private String towerName;
    @Excel(name = "综合风险值")
    private String val;
    @Excel(name = "")
    private Date createDate;

}