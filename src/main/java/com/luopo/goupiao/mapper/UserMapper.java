package com.luopo.goupiao.mapper;

import com.luopo.goupiao.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("select * from user_ where user_name = #{userName}")
    public User getByUserName(@Param("userName") String userName);

    @Insert("insert into user_ (user_name, password, id_card, state, salt, " +
            "telephone, real_name) " +
            "values (#{user.userName}, #{user.password}, " +
            "#{user.idCard}, #{user.state}, #{user.salt}," +
            " #{user.telephone}, #{user.realName} )")
    public void setUser(@Param("user") User user);
}
