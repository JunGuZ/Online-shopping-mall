package com.example.mapper;

import com.example.entity.Address;
import com.example.entity.Pets;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 操作address相关数据接口
*/
public interface PetsMapper {

    /**
     * 新增
     */
    @Insert("INSERT INTO pets (userId, petName, petKind, age, health, info) " +
            "VALUES (#{userId}, #{petName}, #{petKind}, #{age}, #{health}, #{info})")
    int insert(Pets pets);

    /**
     * 删除
     */
    @Delete("DELETE FROM pets WHERE id = #{id}")
    int deleteById(Integer id);

    /**
     * 修改
     */
    @Update("UPDATE pets SET " +
            "petName = #{petName}, " +
            "petKind = #{petKind}, " +
            "age = #{age}, " +
            "health = #{health}, " +
            "info = #{info} " +
            "WHERE id = #{id}")
    int updateById(Pets pets);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM pets WHERE id = #{id}")
    Pets selectById(Integer id);

    /**
     * 动态条件查询
     */
    @Select("<script>" +
            "SELECT * FROM pets WHERE userId = #{userId}" +
            "<if test='petName != null'> AND petName LIKE CONCAT('%', #{petName}, '%')</if>" +
            "<if test='petKind != null'> AND petKind = #{petKind}</if>" +
            "<if test='age != null'> AND age = #{age}</if>" +
            "<if test='health != null'> AND health = #{health}</if>" +
            "</script>")
    List<Pets> selectAll(Pets pets);
}
