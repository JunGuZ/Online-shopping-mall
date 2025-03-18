package com.example.controller;

import com.example.common.Result;
import com.example.entity.Address;
import com.example.entity.Pets;
import com.example.service.AddressService;
import com.example.service.PetsService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/pets")
public class PetsController {

    @Resource
    private PetsService petsService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Pets pets) {
        petsService.add(pets);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        petsService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        petsService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Pets pets) {
        petsService.updateById(pets);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Pets pets = petsService.selectById(id);
        return Result.success(pets);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Pets pets) {
        List<Pets> list = petsService.selectAll(pets);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Pets pets,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Pets> page = petsService.selectPage(pets, pageNum, pageSize);
        return Result.success(page);
    }

}