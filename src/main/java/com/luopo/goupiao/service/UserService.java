package com.luopo.goupiao.service;

import com.luopo.goupiao.exception.GlobalException;
import com.luopo.goupiao.mapper.UserMapper;
import com.luopo.goupiao.pojo.User;
import com.luopo.goupiao.redis.RedisService;
import com.luopo.goupiao.redis.UserKey;
import com.luopo.goupiao.result.CodeMsg;
import com.luopo.goupiao.util.MD5Util;
import com.luopo.goupiao.util.UUIDUtil;
import com.luopo.goupiao.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class UserService {

    public static final String COOKIE_TOKEN_NAME = "token";

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisService redisService;

    public User getByUserName(String userName) {
        //取缓存 key:User_userName_qaq
        User user = redisService.get(UserKey.getByUserName, ""+userName, User.class);
        if(user != null) {  //缓存若有则直接返回，不许要再从数据库查询
            return user;
        }

        //设置账号缓存
        //取数据库
        user = userMapper.getByUserName(userName);
        if(user != null) {  //从数据库select user信息，说明缓存没有，则先存入缓存，再返回
                            // user的默认有效期为7天，在UseKey中用TOKEN_EXPIRE设定
            redisService.set(UserKey.getByUserName, ""+userName, user);
        }
        return user;
    }

//    public boolean update(String token, long id, String formPass) {
//        //取user
//        MiaoshaUser user = getById(id);
//        if(user == null) {
//            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
//        }
//        //更新数据库
//        MiaoshaUser toBeUpdate = new MiaoshaUser();
//        toBeUpdate.setId(id);
//        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
//        miaoshaUserDao.update(toBeUpdate);
//        //处理缓存
//        redisService.delete(MiaoshaUserKey.getById, ""+id);
//        user.setPassword(toBeUpdate.getPassword());
//        redisService.set(MiaoshaUserKey.token, token, user);
//        return true;
//    }

    public User getByToken(HttpServletResponse response, String token) {
        //无token说明未登录
        if(StringUtils.isEmpty(token)) {
            return null;
        }

        //根据token值取user信息
        User user = redisService.get(UserKey.token, token, User.class);

        //重置缓存有效期为初始值（3天）
        if(user != null) {
            this.addCookie(response, token, user);
        }
        return user;
    }

    public String login(HttpServletResponse response, LoginVo loginVo) {
        if(loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }

        String userName = loginVo.getUserName();
        String formPass = loginVo.getPassword();

        //判断用户是否存在
        User user = getByUserName(userName);
        if(user == null) {
            throw new GlobalException(CodeMsg.USER_NAME_NOT_EXIST);
        }

        //如果查询存在该用户名的用户，则验证密码
        String saltDB = user.getSalt();
        String fromPassAfterMD5 = MD5Util.formPassToDBPass(formPass, saltDB);
//        System.out.println("    USER_NAME_NOT_EXIST:" + fromPassAfterMD5);
        if(!fromPassAfterMD5.equals( user.getPassword() )) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //密码验证成功生成token(是一个随机生成的唯一识别码)
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return token;
    }

    //根据token值缓存user信息
    //key:UserKey_Token_token值(UUID)
    private void addCookie(HttpServletResponse response, String token, User user) {
        redisService.set(UserKey.token, token, user);

        Cookie cookie = new Cookie(COOKIE_TOKEN_NAME, token);
        cookie.setMaxAge(UserKey.token.getExpireSeconds());    //cookie最长有效期(三天)
        cookie.setPath("/");    //对于所有路径
        response.addCookie(cookie); //将cookie添加至response上并返回
    }

    public void setUser(User user) {
        userMapper.setUser(user);
    }
}