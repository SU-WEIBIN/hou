package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.entity.Tables;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class ExcelController {

    /**
     * POST /api/tables：上传新表格
     * GET /api/tables/{id}：获取特定表格的信息
     * GET /api/tables：获取所有表格列表
     * DELETE /api/tables/{id}：删除特定表格
     *
     */

        private final List<List<String>> data = new ArrayList<>();

    /**
     * 获取所有表格
     * @return
     */
    @GetMapping
        public BaseResponse<List<Tables>> getAllData() {
            List<Tables> list = new ArrayList<>();
            return ResultUtils.success(list);
        }

    /**
     *
     * @param row
     * @return
     */
    @PostMapping
        public BaseResponse<Boolean> addData(@RequestBody List<String> row) {
            data.add(row);
            return ResultUtils.success(true);
        }

//    /**
//     * 更新表格--进入编辑页面
//     * @param index
//     * @param row
//     * @return
//     */
//        @PutMapping("/{index}")
//        public BaseResponse<Boolean> updateData(@PathVariable int index, @RequestBody List<String> row) {
//            data.set(index, row);
//            return ResultUtils.success(true);
//        }

    /**
     * 删除表格
     * @param index
     * @return
     */
    @DeleteMapping("/{index}")
        public BaseResponse<Boolean> deleteData(@PathVariable int index) {
            data.remove(index);
            return ResultUtils.success(true);
        }
    }

