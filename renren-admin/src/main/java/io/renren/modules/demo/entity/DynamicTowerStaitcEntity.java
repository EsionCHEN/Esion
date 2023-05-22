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
@TableName("dynamic_tower_staitc")
public class DynamicTowerStaitcEntity {

    /**
     * 
     */
	private Long id;
    /**
     * 
     */
	private Integer stationId;
    /**
     * 塔名
     */
	private String towerName;
    /**
     * 
     */
	private Integer alertCount;
    /**
     * 
     */
	private Integer alertDuration;
    /**
     * 垂直度
     */
	private String verticality;
    /**
     * 震动周期
     */
	private String vibrationPeriod;
    /**
     * 应力
     */
	private String stress;
    /**
     * 风速
     */
	private String windSpeed;
    /**
     * 风险值
     */
	private String riskScore;
    /**
     * 一级告警
     */
    @TableField(value = "alert_level_1")
	private Integer alertLevel1;
    /**
     * 二级告警
     */
    @TableField(value = "alert_level_2")
    private Integer alertLevel2;
    /**
     * 三级告警
     */
    @TableField(value = "alert_level_3")
    private Integer alertLevel3;
    /**
     * 
     */
	private Date staticDay;
    /**
     * 
     */
	private Date createDate;
}