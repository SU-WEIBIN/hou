package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.model.entity.Cells;
import com.yupi.springbootinit.service.CellsService;
import com.yupi.springbootinit.mapper.CellsMapper;
import org.springframework.stereotype.Service;

/**
* @author 70928
* @description 针对表【cells】的数据库操作Service实现
* @createDate 2024-12-14 16:10:25
*/
@Service
public class CellsServiceImpl extends ServiceImpl<CellsMapper, Cells>
    implements CellsService{

}




