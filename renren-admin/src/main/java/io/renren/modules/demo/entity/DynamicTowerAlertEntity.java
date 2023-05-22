package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("dynamic_tower_alert")
public class DynamicTowerAlertEntity {

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
     * 编号
     */
	private String no;
    /**
     * 描述
     */
    @TableField("description")
	private String description;
    /**
     * 预警级别
     */
	private String grade;
    /**
     * 时间
     */
	private Date createDate;
}