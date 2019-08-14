package com.chrisz.service.impl;

import com.chrisz.mapper.UsersFansMapper;
import com.chrisz.mapper.UsersLikeVideosMapper;
import com.chrisz.mapper.UsersMapper;
import com.chrisz.mapper.UsersReportMapper;
import com.chrisz.pojo.Users;
import com.chrisz.pojo.UsersFans;
import com.chrisz.pojo.UsersLikeVideos;
import com.chrisz.pojo.UsersReport;
import com.chrisz.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private UsersLikeVideosMapper usersLikeVideosMapper;

    @Autowired
    private UsersFansMapper usersFansMapper;

    @Autowired
    private UsersReportMapper usersReportMapper;

    @Autowired
    private Sid sid;

    @Transactional(propagation = Propagation.SUPPORTS)
    /*若用户名不存在返回false*/
    @Override
    public boolean queryUsernameIsExist(String username) {
        Users user = new Users();
        user.setUsername(username);

        Users result = usersMapper.selectOne(user);

        return result==null?false:true;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    /*保存新用户信息*/
    @Override
    public void saveUser(Users user) {

        //生成唯一id
        String userId = sid.nextShort();

        user.setId(userId);
        usersMapper.insert(user);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    /*校验用户名和密码*/
    @Override
    public Users queryUserForLogin(String username, String md5Str) {
        Users user = new Users();
        user.setUsername(username);

        Users result = usersMapper.selectOne(user);
        if(result == null){
            return null;
        }else{
            if(result.getPassword().equals(md5Str)){
                return result;
            }else{
                return null;
            }
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    /*更新用户信息*/
    @Override
    public void updateUserInfo(Users user) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("id", user.getId());
        usersMapper.updateByExampleSelective(user, userExample);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    /*若用户名不存在返回false*/
    @Override
    public Users queryUserInfo(String userId) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("id", userId);
        Users user = usersMapper.selectOneByExample(userExample);
        return user;
    }

    /*查询用户和视频like关系*/
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean isUserLikeVideo(String userId, String videoId) {


        if(StringUtils.isBlank(userId) || StringUtils.isBlank(videoId)){
            return false;
        }
        Example example = new Example(UsersLikeVideos.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("videoId",videoId);

        List<UsersLikeVideos> list = usersLikeVideosMapper.selectByExample(example);

        if(list!=null && list.size()>0){
            return  true;
        }
        return false;
    }

    /*添加用户关注关系*/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveUserFanRelation(String userId, String fanId) {

        //1.添加关注记录
        String relId = sid.nextShort();

        UsersFans usersFans = new UsersFans();
        usersFans.setId(relId);
        usersFans.setUserId(userId);
        usersFans.setFanId(fanId);

        usersFansMapper.insert(usersFans);

        //2.被关注者粉丝数加一

        usersMapper.addFansCount(userId);
        //3.关注者关注数加一

        usersMapper.addFollowersCount(fanId);
    }

    /*删除用户关注关系*/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteUserFanRelation(String userId, String fanId) {

        //1.删除关注记录
        Example example = new Example(UsersFans.class);

        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("fanId",fanId);

        usersFansMapper.deleteByExample(example);

        //2.被关注者粉丝数减一

        usersMapper.reduceFansCount(userId);
        //3.关注者关注数减一

        usersMapper.reduceFollowersCount(fanId);
    }

    /*查询是否关注*/
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryIsFollow(String userId, String fanId) {

        Example example = new Example(UsersFans.class);

        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("fanId",fanId);

        List<UsersFans> list = usersFansMapper.selectByExample(example);

        if(list!=null && list.size()>0){
            return  true;
        }
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void report(UsersReport usersReport) {
        String urId = sid.nextShort();

        usersReport.setId(urId);
        usersReport.setCreateDate(new Date());

        usersReportMapper.insert(usersReport);
    }


}
