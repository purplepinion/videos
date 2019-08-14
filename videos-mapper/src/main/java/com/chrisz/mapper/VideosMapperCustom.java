package com.chrisz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.chrisz.pojo.Videos;
import com.chrisz.pojo.vo.VideosVO;
import com.chrisz.utils.MyMapper;

public interface VideosMapperCustom extends MyMapper<Videos> {
	
	/**
	 * @Description: 条件查询所有视频列表
	 */
	public List<VideosVO> queryAllVideos(@Param("videoDesc") String videoDesc,@Param("userId") String userId);


    /**
     * @Description: 视频喜欢数量累加
     */
    public void addVideoLikeCount(String videoId);

    /**
     * @Description: 视频喜欢数量累减
     */
    public void reduceVideoLikeCount(String videoId);

    /**
     * @Description: 查询用户喜欢视频
     */
    public List<VideosVO> queryMyLikeVideos(String userId);

    /**
     * @Description: 查询用户关注的人发的视频列表
     */
    public List<VideosVO> queryMyFollowVideos(String userId);

}