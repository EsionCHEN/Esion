package io.renren.modules.demo.service;

import io.renren.common.service.CrudService;
import io.renren.modules.demo.dto.DynamicTowerDayDTO;
import io.renren.modules.demo.dto.DynamicTowerSecondDTO;
import io.renren.modules.demo.entity.DynamicTowerDayEntity;

import java.util.List;

/**
 * 
 *
 * @author ESION tintuu@163.com
 * @since 1.0.0 2023-05-20
 */
public interface DynamicTowerDayService extends CrudService<DynamicTowerDayEntity, DynamicTowerDayDTO> {

    List<DynamicTowerDayEntity> collecct(String json);

}