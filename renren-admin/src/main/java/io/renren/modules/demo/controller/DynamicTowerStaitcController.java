package io.renren.modules.demo.controller;

import cn.hutool.json.JSONUtil;
import io.renren.common.annotation.LogOperation;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.ExcelUtils;
import io.renren.common.utils.Result;
import io.renren.common.utils.XmlToMap;
import io.renren.common.validator.AssertUtils;
import io.renren.common.validator.ValidatorUtils;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.renren.modules.demo.dto.DynamicTowerDayDTO;
import io.renren.modules.demo.dto.DynamicTowerStaitcDTO;
import io.renren.modules.demo.entity.DynamicTowerDayEntity;
import io.renren.modules.demo.excel.DynamicTowerStaitcExcel;
import io.renren.modules.demo.service.DynamicTowerStaitcService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.compress.utils.Lists;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;


/**
 * 
 *
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-20
 */
@RestController
@RequestMapping("demo/dynamictowerstaitc")
@Api(tags="A-静态数据汇总")
public class DynamicTowerStaitcController {
    @Autowired
    private DynamicTowerStaitcService dynamicTowerStaitcService;

    @GetMapping("page")
    @ApiOperation("分页")
    @ApiImplicitParams({
        @ApiImplicitParam(name = Constant.PAGE, value = "当前页码，从1开始", paramType = "query", required = true, dataType="int") ,
        @ApiImplicitParam(name = Constant.LIMIT, value = "每页显示记录数", paramType = "query",required = true, dataType="int") ,
        @ApiImplicitParam(name = Constant.ORDER_FIELD, value = "排序字段", paramType = "query", dataType="String") ,
        @ApiImplicitParam(name = Constant.ORDER, value = "排序方式，可选值(asc、desc)", paramType = "query", dataType="String")
    })
    @RequiresPermissions("demo:dynamictowerstaitc:page")
    public Result<PageData<DynamicTowerStaitcDTO>> page(@ApiIgnore @RequestParam Map<String, Object> params){
        PageData<DynamicTowerStaitcDTO> page = dynamicTowerStaitcService.page(params);

        return new Result<PageData<DynamicTowerStaitcDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @ApiOperation("信息")
    @RequiresPermissions("demo:dynamictowerstaitc:info")
    public Result<DynamicTowerStaitcDTO> get(@PathVariable("id") Long id){
        DynamicTowerStaitcDTO data = dynamicTowerStaitcService.get(id);

        return new Result<DynamicTowerStaitcDTO>().ok(data);
    }

    @PostMapping
    @ApiOperation("保存")
    @LogOperation("保存")
    @RequiresPermissions("demo:dynamictowerstaitc:save")
    public Result save(@RequestBody DynamicTowerStaitcDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        dynamicTowerStaitcService.save(dto);

        return new Result();
    }

    @PutMapping
    @ApiOperation("修改")
    @LogOperation("修改")
    @RequiresPermissions("demo:dynamictowerstaitc:update")
    public Result update(@RequestBody DynamicTowerStaitcDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        dynamicTowerStaitcService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @ApiOperation("删除")
    @LogOperation("删除")
    @RequiresPermissions("demo:dynamictowerstaitc:delete")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        dynamicTowerStaitcService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @ApiOperation("导出")
    @LogOperation("导出")
    @RequiresPermissions("demo:dynamictowerstaitc:export")
    public void export(@ApiIgnore @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<DynamicTowerStaitcDTO> list = dynamicTowerStaitcService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, list, DynamicTowerStaitcExcel.class);
    }



    @GetMapping("sum")
    @ApiOperation("数据汇总")
    public Result<List<DynamicTowerStaitcDTO>> sum(){
        return new Result<List<DynamicTowerStaitcDTO>>().ok(dynamicTowerStaitcService.sum());
    }


    @GetMapping("test")
    @ApiOperation("数据测试")
    public String test(){
        String json = null;
        try {
            String folder = "file/";
            String fileName = "917x.txt";
            String path = this.getClass().getClassLoader().getResource(folder + fileName).getPath();
            json = XmlToMap.readFileToString(path);
        }  catch (Exception e) {
           return null;
        }
        return json;
    }
}