package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 *
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-21
 */
@Data
@TableName("dynamic_tower_second")
public class DynamicTowerSecondEntity {

    /**
     * 
     */
	private Long id;
    /**
     * 
     */
	private Integer stationId;
    /**
     * 
     */
	private String stationName;
    /**
     * 塔名
     */
	private String towerName;
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
     * 
     */
	private Date createDate;
}