package io.renren.modules.demo.controller;

import io.renren.common.annotation.LogOperation;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.ExcelUtils;
import io.renren.common.utils.Result;
import io.renren.common.validator.AssertUtils;
import io.renren.common.validator.ValidatorUtils;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.renren.modules.demo.dto.DynamicTowerDayDTO;
import io.renren.modules.demo.dto.DynamicTowerSecondDTO;
import io.renren.modules.demo.entity.DynamicTowerDayEntity;
import io.renren.modules.demo.excel.DynamicTowerDayExcel;
import io.renren.modules.demo.service.DynamicTowerDayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * 
 *
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-20
 */
@RestController
@RequestMapping("demo/dynamictowerday")
@Api(tags="A-日数据采集")
public class DynamicTowerDayController {
    @Autowired
    private DynamicTowerDayService dynamicTowerDayService;

    @GetMapping("page")
    @ApiOperation("分页")
    @ApiImplicitParams({
        @ApiImplicitParam(name = Constant.PAGE, value = "当前页码，从1开始", paramType = "query", required = true, dataType="int") ,
        @ApiImplicitParam(name = Constant.LIMIT, value = "每页显示记录数", paramType = "query",required = true, dataType="int") ,
        @ApiImplicitParam(name = Constant.ORDER_FIELD, value = "排序字段", paramType = "query", dataType="String") ,
        @ApiImplicitParam(name = Constant.ORDER, value = "排序方式，可选值(asc、desc)", paramType = "query", dataType="String")
    })
    @RequiresPermissions("demo:dynamictowerday:page")
    public Result<PageData<DynamicTowerDayDTO>> page(@ApiIgnore @RequestParam Map<String, Object> params){
        PageData<DynamicTowerDayDTO> page = dynamicTowerDayService.page(params);

        return new Result<PageData<DynamicTowerDayDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @ApiOperation("信息")
    public Result<DynamicTowerDayDTO> get(@PathVariable("id") Long id){
        DynamicTowerDayDTO data = dynamicTowerDayService.get(id);

        return new Result<DynamicTowerDayDTO>().ok(data);
    }

    @PostMapping
    @ApiOperation("保存")
    @LogOperation("保存")
    public Result save(@RequestBody DynamicTowerDayDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        dynamicTowerDayService.save(dto);

        return new Result();
    }

    @PutMapping
    @ApiOperation("修改")
    @LogOperation("修改")
    public Result update(@RequestBody DynamicTowerDayDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        dynamicTowerDayService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @ApiOperation("删除")
    @LogOperation("删除")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        dynamicTowerDayService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @ApiOperation("导出")
    @LogOperation("导出")
    public void export(@ApiIgnore @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<DynamicTowerDayDTO> list = dynamicTowerDayService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, list, DynamicTowerDayExcel.class);
    }

    @PostMapping(value = "collect",consumes = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation("数据采集")
    public Result<List<DynamicTowerDayEntity>> collect(@RequestBody String json){
        List<DynamicTowerDayEntity> collecct = dynamicTowerDayService.collecct(json);
        return new Result<List<DynamicTowerDayEntity>>().ok(collecct);
    }

}