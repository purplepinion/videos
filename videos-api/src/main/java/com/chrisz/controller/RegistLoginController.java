package com.chrisz.controller;

import com.chrisz.pojo.Users;
import com.chrisz.pojo.vo.UsersVO;
import com.chrisz.service.UserService;
import com.chrisz.utils.JSONResult;
import com.chrisz.utils.MD5Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Api(value = "用户注册登录API" ,tags = {"用户注册登录的Controller"})
public class RegistLoginController extends BasicController{

    @Autowired
    private UserService userService;


    @PostMapping("/regist")
    @ApiOperation(value = "用户登录",notes = "用户注册的接口")
    public JSONResult regist(@RequestBody Users user) throws Exception {

        /*用户名和密码不能为空*/
        if(StringUtils.isBlank(user.getUsername())||StringUtils.isBlank(user.getPassword())){
            return JSONResult.errorMsg("用户名和密码不能为空");
        }

        /*判断与用户名是否存在*/
        boolean usernameIsExist = userService.queryUsernameIsExist(user.getUsername());

        /*判断用户信息是否存入数据库*/
        boolean userIsSaved = false;

        /*保存新注册信息 or 用户名重复*/
        if(!usernameIsExist){
            /*基础信息设置*/

            user.setNickname(user.getUsername());
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            user.setFansCounts(0);
            user.setReceiveLikeCounts(0);
            user.setFollowCounts(0);
            userService.saveUser(user);
            userIsSaved = true;

        }else{
            return JSONResult.errorMsg("用户名已经存在！");
        }

        /*用户存入数据库后取出数据库中用户信息*/
        UsersVO userVO = null;

        if(userIsSaved){
            /*返回到前端用户信息隐藏密码*/
            user.setPassword("");

            /*添加用户信息缓存,30分钟过期*//*
            String uniqueToken = UUID.randomUUID().toString();
            redis.set(USER_REDIS_SESSION + ":" + user.getId(),uniqueToken,1000 * 60 * 30);

            *//*定义UsersVO返回做需要的数据对象*//*
            UsersVO userVO = new UsersVO();
            BeanUtils.copyProperties(user,userVO);
            userVO.setUserToken(uniqueToken);*/

            userVO = setUserRedisSessionToken(user);
        }


        return JSONResult.ok(userVO);
    }

    private UsersVO setUserRedisSessionToken(Users user){
        /*添加用户信息缓存,30分钟过期*/
        String uniqueToken = UUID.randomUUID().toString();
        redis.set(USER_REDIS_SESSION + ":" + user.getId(),uniqueToken,1000 * 60 * 30);

        /*定义UsersVO返回做需要的数据对象*/
        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(user,userVO);
        userVO.setUserToken(uniqueToken);

        return userVO;
    }

    @PostMapping("/login")
    @ApiOperation(value = "用户登录",notes = "用户登录的接口")
    public JSONResult login(@RequestBody Users user) throws Exception {

        String username = user.getUsername();
        String password = user.getPassword();

        /*用户名和密码不能为空*/
        if(StringUtils.isBlank(user.getUsername())||StringUtils.isBlank(user.getPassword())){
            return JSONResult.ok("用户名和密码不能为空");
        }

        /*判断用户是否存在*/
        Users result = userService.queryUserForLogin(username,MD5Utils.getMD5Str(password));

        if(result==null){
            return JSONResult.errorMsg("用户名或者密码错误");
        }else{
            user.setPassword("");
            UsersVO userVO = setUserRedisSessionToken(result);
            return JSONResult.ok(userVO);
        }
    }

    @PostMapping("/logout")
    @ApiImplicitParam(name = "userId",value = "用户id",required = true,dataType = "String",paramType = "query")
    @ApiOperation(value = "用户注销",notes = "用户注销的接口")
    public JSONResult logout(String userId) throws Exception {
        redis.del(USER_REDIS_SESSION + ":" + userId);
        return JSONResult.ok();
    }
}
