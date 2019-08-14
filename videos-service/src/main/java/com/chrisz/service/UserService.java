package com.chrisz.service;

import com.chrisz.pojo.Users;
import com.chrisz.pojo.UsersReport;

public interface UserService {
    /*判断用户名是否存在*/
    public boolean queryUsernameIsExist(String username);

    /*保存用户信息*/
    public void saveUser(Users user);

    /*查询是否存在用户名为username密码为md5Str的用户，并返回*/
    public Users queryUserForLogin(String username, String md5Str);

    /*更新用户信息*/
    public void updateUserInfo(Users user);

    /*查询用户信息*/
    public Users queryUserInfo(String userId);

    /*查询用户和视频点赞关系*/
    public boolean isUserLikeVideo(String userId,String videoId);

    /*添加用户关注粉丝关系*/
    public void saveUserFanRelation(String userId,String fanId);

    /*删除用户关注粉丝关系*/
    public void deleteUserFanRelation(String userId,String fanId);

    /*查询是否关注*/
    public boolean queryIsFollow(String userId,String fanId);

    /*举报用户*/
    public void report(UsersReport usersReport);

}
