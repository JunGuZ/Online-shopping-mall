package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Pets;
import com.example.mapper.PetsMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 收藏业务处理
 **/
@Service
public class PetsService {

    @Resource
    private PetsMapper petsMapper;

    /**
     * 新增
     */
    public void add(Pets pets) {
        petsMapper.insert(pets);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        petsMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            petsMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Pets pets) {
        petsMapper.updateById(pets);
    }

    /**
     * 根据ID查询
     */
    public Pets selectById(Integer id) {

        return petsMapper.selectById(id);

    }

    /**
     * 查询所有
     */
    public List<Pets> selectAll(Pets pets) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            pets.setUserId(currentUser.getId());
        }
        return petsMapper.selectAll(pets);
    }

    /**
     * 分页查询
     */
    public PageInfo<Pets> selectPage(Pets pets, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            pets.setUserId(currentUser.getId());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Pets> list = petsMapper.selectAll(pets);
        return PageInfo.of(list);
    }
}