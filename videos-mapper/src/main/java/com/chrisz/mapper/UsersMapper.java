package com.chrisz.mapper;

import com.chrisz.pojo.Users;
import com.chrisz.utils.MyMapper;

public interface UsersMapper extends MyMapper<Users> {
	
	/**
	 * @Description: 用户受喜欢数累加
	 */
	public void addReceiveLikeCount(String userId);

	/**
	 * @Description: 用户受喜欢数累减
	 */
	public void reduceReceiveLikeCount(String userId);

	/**
	 * @Description: 增加用户粉丝数
	 */
	public void addFansCount(String userId);

	/**
	 * @Description: 减少用户粉丝数
	 */
	public void reduceFansCount(String userId);

	/**
	 * @Description: 增加关注数
	 */
	public void addFollowersCount(String userId);

	/**
	 * @Description: 减少关注数
	 */
	public void reduceFollowersCount(String userId);

}