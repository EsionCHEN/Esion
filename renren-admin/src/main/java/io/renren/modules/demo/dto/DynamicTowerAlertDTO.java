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
public class DynamicTowerAlertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "")
	private Long id;

	@ApiModelProperty(value = "")
	private Integer stationId;

	@ApiModelProperty(value = "台站名")
	private String stationName;

	@ApiModelProperty(value = "塔名")
	private String towerName;

	@ApiModelProperty(value = "编号")
	private String no;

	@ApiModelProperty(value = "描述")
	private String description;

	@ApiModelProperty(value = "预警级别")
	private String grade;

	@ApiModelProperty(value = "时间")
	private Date createDate;


}