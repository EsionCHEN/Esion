package io.renren.modules.demo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 
 *
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-20
 */
@Data
@ApiModel(value = "")
public class DynamicTowerStaitcDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "")
	private Long id;

	@ApiModelProperty(value = "")
	private Integer stationId;

	@ApiModelProperty(value = "塔名")
	private String towerName;

	@ApiModelProperty(value = "")
	private Integer alertCount;

	@ApiModelProperty(value = "")
	private Integer alertDuration;

	@ApiModelProperty(value = "垂直度")
	private String verticality;

	@ApiModelProperty(value = "震动周期")
	private String vibrationPeriod;

	@ApiModelProperty(value = "应力")
	private String stress;

	@ApiModelProperty(value = "风速")
	private String windSpeed;

	@ApiModelProperty(value = "风险值")
	private String riskScore;

	@ApiModelProperty(value = "一级告警")
	private Integer alertLevel1;

	@ApiModelProperty(value = "二级告警")
	private Integer alertLevel2;

	@ApiModelProperty(value = "三级告警")
	private Integer alertLevel3;

	@ApiModelProperty(value = "")
	private Date staticDay;

	@ApiModelProperty(value = "")
	private Date createDate;


}