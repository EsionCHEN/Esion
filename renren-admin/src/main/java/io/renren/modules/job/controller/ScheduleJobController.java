/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.job.controller;

import io.renren.common.annotation.LogOperation;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.common.validator.ValidatorUtils;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.renren.modules.job.dto.ScheduleJobDTO;
import io.renren.modules.job.service.ScheduleJobService;
import io.renren.modules.job.task.XmlDayThread;
import io.renren.modules.job.task.XmlMonthThread;
import io.renren.modules.job.task.XmlThread;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

/**
 * 定时任务
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("/sys/schedule")
@Api(tags="定时任务")
@Slf4j
public class ScheduleJobController {
	@Autowired
	private ScheduleJobService scheduleJobService;

	@GetMapping("page")
	@ApiOperation("分页")
	@ApiImplicitParams({
		@ApiImplicitParam(name = Constant.PAGE, value = "当前页码，从1开始", paramType = "query", required = true, dataType="int") ,
		@ApiImplicitParam(name = Constant.LIMIT, value = "每页显示记录数", paramType = "query",required = true, dataType="int") ,
		@ApiImplicitParam(name = Constant.ORDER_FIELD, value = "排序字段", paramType = "query", dataType="String") ,
		@ApiImplicitParam(name = Constant.ORDER, value = "排序方式，可选值(asc、desc)", paramType = "query", dataType="String") ,
		@ApiImplicitParam(name = "beanName", value = "beanName", paramType = "query", dataType="String")
	})
	public Result<PageData<ScheduleJobDTO>> page(@ApiIgnore @RequestParam Map<String, Object> params){
		PageData<ScheduleJobDTO> page = scheduleJobService.page(params);

		return new Result<PageData<ScheduleJobDTO>>().ok(page);
	}

	@GetMapping("{id}")
	@ApiOperation("信息")
	public Result<ScheduleJobDTO> info(@PathVariable("id") Long id){
		ScheduleJobDTO schedule = scheduleJobService.get(id);
		
		return new Result<ScheduleJobDTO>().ok(schedule);
	}

	@PostMapping
	@ApiOperation("保存")
	@LogOperation("保存")
	public Result save(@RequestBody ScheduleJobDTO dto){
		ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);
		
		scheduleJobService.save(dto);
		
		return new Result();
	}

	@PutMapping
	@ApiOperation("修改")
	@LogOperation("修改")
	public Result update(@RequestBody ScheduleJobDTO dto){
		ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);
				
		scheduleJobService.update(dto);
		
		return new Result();
	}

	@DeleteMapping
	@ApiOperation("删除")
	@LogOperation("删除")
	public Result delete(@RequestBody Long[] ids){
		scheduleJobService.deleteBatch(ids);
		
		return new Result();
	}

	@PutMapping("/run")
	@ApiOperation("立即执行")
	@LogOperation("立即执行")
	public Result run(@RequestBody Long[] ids){
		scheduleJobService.run(ids);
		
		return new Result();
	}

	@PutMapping("/pause")
	@ApiOperation("暂停")
	@LogOperation("暂停")
	public Result pause(@RequestBody Long[] ids){
		scheduleJobService.pause(ids);
		
		return new Result();
	}

	@PutMapping("/resume")
	@ApiOperation("恢复")
	@LogOperation("恢复")
	public Result resume(@RequestBody Long[] ids){
		scheduleJobService.resume(ids);
		
		return new Result();
	}

	@GetMapping("/ms")
	@ApiOperation("采集SECOND/ALERT")
	public Result collect(){
		XmlThread xmlThread = new XmlThread("http://192.168.33.92");
		XmlThread xmlThread2 = new XmlThread("http://192.168.14.92");
		XmlThread xmlThread3 = new XmlThread("http://192.168.17.92");
		xmlThread.run();
		xmlThread2.run();
		xmlThread3.run();
		return new Result();
	}

	@GetMapping("/day")
	@ApiOperation("采集DAY")
	public Result collectDay(){
		log.info("");
		XmlDayThread xmlDayThread = new XmlDayThread("http://192.168.33.92");
		XmlDayThread xmlDayThread2 = new XmlDayThread("http://192.168.14.92");
		XmlDayThread xmlDayThread3 = new XmlDayThread("http://192.168.17.92");
		xmlDayThread.run();
		xmlDayThread2.run();
		xmlDayThread3.run();
		return new Result();
	}


}