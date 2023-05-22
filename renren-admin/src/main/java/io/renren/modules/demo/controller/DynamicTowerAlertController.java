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
import io.renren.modules.demo.dto.DynamicTowerAlertDTO;
import io.renren.modules.demo.entity.DynamicTowerAlertEntity;
import io.renren.modules.demo.entity.DynamicTowerDayEntity;
import io.renren.modules.demo.excel.DynamicTowerAlertExcel;
import io.renren.modules.demo.service.DynamicTowerAlertService;
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
@RequestMapping("demo/dynamictoweralert")
@Api(tags="A-预警采集")
public class DynamicTowerAlertController {
    @Autowired
    private DynamicTowerAlertService dynamicTowerAlertService;

    @GetMapping("page")
    @ApiOperation("分页")
    @ApiImplicitParams({
        @ApiImplicitParam(name = Constant.PAGE, value = "当前页码，从1开始", paramType = "query", required = true, dataType="int") ,
        @ApiImplicitParam(name = Constant.LIMIT, value = "每页显示记录数", paramType = "query",required = true, dataType="int") ,
        @ApiImplicitParam(name = Constant.ORDER_FIELD, value = "排序字段", paramType = "query", dataType="String") ,
        @ApiImplicitParam(name = Constant.ORDER, value = "排序方式，可选值(asc、desc)", paramType = "query", dataType="String")
    })
    @RequiresPermissions("demo:dynamictoweralert:page")
    public Result<PageData<DynamicTowerAlertDTO>> page(@ApiIgnore @RequestParam Map<String, Object> params){
        PageData<DynamicTowerAlertDTO> page = dynamicTowerAlertService.page(params);

        return new Result<PageData<DynamicTowerAlertDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @ApiOperation("信息")
    @RequiresPermissions("demo:dynamictoweralert:info")
    public Result<DynamicTowerAlertDTO> get(@PathVariable("id") Long id){
        DynamicTowerAlertDTO data = dynamicTowerAlertService.get(id);

        return new Result<DynamicTowerAlertDTO>().ok(data);
    }

    @PostMapping
    @ApiOperation("保存")
    @LogOperation("保存")
    @RequiresPermissions("demo:dynamictoweralert:save")
    public Result save(@RequestBody DynamicTowerAlertDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        dynamicTowerAlertService.save(dto);

        return new Result();
    }

    @PutMapping
    @ApiOperation("修改")
    @LogOperation("修改")
    @RequiresPermissions("demo:dynamictoweralert:update")
    public Result update(@RequestBody DynamicTowerAlertDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        dynamicTowerAlertService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @ApiOperation("删除")
    @LogOperation("删除")
    @RequiresPermissions("demo:dynamictoweralert:delete")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        dynamicTowerAlertService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @ApiOperation("导出")
    @LogOperation("导出")
    @RequiresPermissions("demo:dynamictoweralert:export")
    public void export(@ApiIgnore @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<DynamicTowerAlertDTO> list = dynamicTowerAlertService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, list, DynamicTowerAlertExcel.class);
    }

    @PostMapping("collect")
    @ApiOperation(value = "数据采集",consumes = MediaType.TEXT_PLAIN_VALUE)
    public Result<List<DynamicTowerAlertEntity>> collect(@RequestBody String json){
        List<DynamicTowerAlertEntity> collecct = dynamicTowerAlertService.collecct(json);
        return new Result<List<DynamicTowerAlertEntity>>().ok(collecct);
    }


    @GetMapping("deleteAll")
    @ApiOperation("清空一月前的数据")
    public Result deleteAll(){
        //效验数据
        dynamicTowerAlertService.deleteAll();
        return new Result();
    }

}