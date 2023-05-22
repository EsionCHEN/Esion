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
 * @since 1.0.0 2023-05-21
 */
@Data
@ApiModel(value = "")
public class DynamicTowerSecondDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "")
	private Long id;

	@ApiModelProperty(value = "")
	private Integer stationId;

	@ApiModelProperty(value = "")
	private String stationName;

	@ApiModelProperty(value = "塔名")
	private String towerName;

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

	@ApiModelProperty(value = "")
	private Date createDate;


}