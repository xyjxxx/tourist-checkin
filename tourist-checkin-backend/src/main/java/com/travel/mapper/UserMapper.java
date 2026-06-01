package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 物理删除被软删除的用户（绕过 @TableLogic），用于释放账号唯一键
     */
    @Delete("DELETE FROM user WHERE account = #{account} AND deleted = 1")
    int physicallyDeleteByAccount(@Param("account") String account);
}
