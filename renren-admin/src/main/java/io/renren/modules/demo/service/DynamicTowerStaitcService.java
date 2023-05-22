package io.renren.modules.demo.service;

import io.renren.common.service.CrudService;
import io.renren.modules.demo.dto.DynamicTowerStaitcDTO;
import io.renren.modules.demo.entity.DynamicTowerStaitcEntity;

import java.util.List;

/**
 * 
 *
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-20
 */
public interface DynamicTowerStaitcService extends CrudService<DynamicTowerStaitcEntity, DynamicTowerStaitcDTO> {
    List<DynamicTowerStaitcDTO> collecct(String json);
    List<DynamicTowerStaitcDTO> sum();
}