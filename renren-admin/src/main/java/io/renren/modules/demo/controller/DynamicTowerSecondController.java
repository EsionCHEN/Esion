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
import io.renren.modules.demo.dto.DynamicTowerSecondDTO;
import io.renren.modules.demo.dto.ResponseJsonDTO;
import io.renren.modules.demo.entity.DynamicTowerSecondEntity;
import io.renren.modules.demo.excel.DynamicTowerSecondExcel;
import io.renren.modules.demo.service.DynamicTowerSecondService;
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
 * @since 1.0.0 2023-05-21
 */
@RestController
@RequestMapping("demo/dynamictowersecond")
@Api(tags="A-秒数据采集")
public class DynamicTowerSecondController {
    @Autowired
    private DynamicTowerSecondService dynamicTowerSecondService;

    @GetMapping("page")
    @ApiOperation("分页")
    @ApiImplicitParams({
        @ApiImplicitParam(name = Constant.PAGE, value = "当前页码，从1开始", paramType = "query", required = true, dataType="int") ,
        @ApiImplicitParam(name = Constant.LIMIT, value = "每页显示记录数", paramType = "query",required = true, dataType="int") ,
        @ApiImplicitParam(name = Constant.ORDER_FIELD, value = "排序字段", paramType = "query", dataType="String") ,
        @ApiImplicitParam(name = Constant.ORDER, value = "排序方式，可选值(asc、desc)", paramType = "query", dataType="String")
    })
    @RequiresPermissions("demo:dynamictowersecond:page")
    public Result<PageData<DynamicTowerSecondDTO>> page(@ApiIgnore @RequestParam Map<String, Object> params){
        PageData<DynamicTowerSecondDTO> page = dynamicTowerSecondService.page(params);

        return new Result<PageData<DynamicTowerSecondDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @ApiOperation("信息")
    @RequiresPermissions("demo:dynamictowersecond:info")
    public Result<DynamicTowerSecondDTO> get(@PathVariable("id") Long id){
        DynamicTowerSecondDTO data = dynamicTowerSecondService.get(id);

        return new Result<DynamicTowerSecondDTO>().ok(data);
    }

    @PostMapping
    @ApiOperation("保存")
    @LogOperation("保存")
    @RequiresPermissions("demo:dynamictowersecond:save")
    public Result save(@RequestBody DynamicTowerSecondDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        dynamicTowerSecondService.save(dto);

        return new Result();
    }

    @PutMapping
    @ApiOperation("修改")
    @LogOperation("修改")
    @RequiresPermissions("demo:dynamictowersecond:update")
    public Result update(@RequestBody DynamicTowerSecondDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        dynamicTowerSecondService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @ApiOperation("删除")
    @LogOperation("删除")
    @RequiresPermissions("demo:dynamictowersecond:delete")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        dynamicTowerSecondService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @ApiOperation("导出")
    @LogOperation("导出")
    @RequiresPermissions("demo:dynamictowersecond:export")
    public void export(@ApiIgnore @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<DynamicTowerSecondDTO> list = dynamicTowerSecondService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, list, DynamicTowerSecondExcel.class);
    }

    @PostMapping(value = "collect",consumes = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation("数据采集")
    public Result<List<DynamicTowerSecondEntity>> collect(@RequestBody String json){
        List<DynamicTowerSecondEntity> collecct = dynamicTowerSecondService.collecct(json);
        return new Result<List<DynamicTowerSecondEntity>>().ok(collecct);
    }

}