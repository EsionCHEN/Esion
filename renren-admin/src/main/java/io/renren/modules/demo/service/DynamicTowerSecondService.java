package io.renren.modules.demo.service;

import io.renren.common.service.CrudService;
import io.renren.modules.demo.dto.DynamicTowerSecondDTO;
import io.renren.modules.demo.dto.ResponseJsonDTO;
import io.renren.modules.demo.entity.DynamicTowerSecondEntity;

import java.util.List;

/**
 * 
 *
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-21
 */
public interface DynamicTowerSecondService extends CrudService<DynamicTowerSecondEntity, DynamicTowerSecondDTO> {

    List<DynamicTowerSecondEntity> collecct(String json);
}