package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.model.entity.Tables;
import com.yupi.springbootinit.service.TablesService;
import com.yupi.springbootinit.mapper.TablesMapper;
import org.springframework.stereotype.Service;

/**
* @author 70928
* @description 针对表【tables】的数据库操作Service实现
* @createDate 2024-12-14 16:09:47
*/
@Service
public class TablesServiceImpl extends ServiceImpl<TablesMapper, Tables>
    implements TablesService{

}




