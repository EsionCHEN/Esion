package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 *
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-20
 */
@Data
@TableName("dynamic_tower_day")
public class DynamicTowerDayEntity {

    /**
     * 
     */
	private Long id;
    /**
     * 
     */
	private Integer stationId;
    /**
     * 台站名
     */
	private String stationName;
    /**
     * 塔名
     */
	private String towerName;
    /**
     * 综合风险值
     */
	private String val;
    /**
     * 
     */
	private Date createDate;
}