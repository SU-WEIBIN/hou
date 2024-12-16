package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.entity.Cells;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/cells")
public class CellsController {

    /**
     * PUT /api/cells/{tableId}/{row}/{column}：更新或插入单元格数据
     * GET /api/cells/{tableId}：获取特定表格的所有单元格数据
     * DELETE /api/cells/{tableId}/{row}/{column}：删除特定单元格数据
     */

    /**
     * 更新或插入单元格数据
     * @return
     */
    @PutMapping("/{tableId}/{row}/{col}")
    public BaseResponse<Boolean> update(@PathVariable long tableId, @PathVariable long row, @PathVariable long col, @RequestBody Cells cell){

        return ResultUtils.success(true);
    }

    /**
     * 获取特定表格的所有单元格数据
     * @return
     */
    @GetMapping("/{tableId}")
    public BaseResponse<Cells> select(long tableId){
        Cells cells = new Cells();
        return ResultUtils.success(cells);
    }

    /**
     * 返回所有的单元格 TODO
     *
     */
//    @GetMapping("/{tableId}")
//    public BaseResponse<String>
//    /**
//     * 删除特定单元格数据
//     * @return
//     */
//    @DeleteMapping("/{tableId}/{row}/{column}}")
//    public String del(long tableId,long row,long col){
//
//        return "";
//    }
}
