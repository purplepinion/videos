package com.chrisz.service;

import com.chrisz.pojo.Comments;
import com.chrisz.pojo.Videos;
import com.chrisz.utils.PagedResult;

import java.util.List;

public interface VideoService {


    /*
     *  @Description:保存视频
     *  @Params: 视频对象 video
     *  @Return:返回视频主键id String
     */
    public String saveVideo(Videos video);


    /*
     *  @Description:更新视频封面
     *  @Params: 视频id String、 视频封面路径coverPath String
     *  @Return: void
     */
    public void updateVideo(String videoId,String coverPath);

    /*
     *  @Description:分页查询视频列表
     *  @Params:page分页起始 pageSize分页大小 video isSaveRecord
     *  @Return:
     */
    public PagedResult getAllVideos(Videos video,Integer isSaveRecord,Integer page,Integer pageSize);


    /*
     *  @Description:获取热词列表
     *  @Params:
     *  @Return:
     */
    public List<String> getHotWords();


    /*
     *  @Description:用户喜欢视频
     *  @Params:
     *  @Return:
     */
    public void userLikeVideo(String userId,String videoId,String videoCreaterId);

    /*
     *  @Description:用户取消喜欢视频
     *  @Params:
     *  @Return:
     */
    public void userUnLikeVideo(String userId,String videoId,String videoCreaterId);


    /*
     *  @Description:查询用户喜欢视频列表
     *  @Params:
     *  @Return:
     */
    public PagedResult queryMyLikeVideos(String userId,Integer page,Integer pageSize);


    /*
     *  @Description:查询用户关注的人发的视频列表
     *  @Params:
     *  @Return:
     */
    public PagedResult queryMyFollowVideos(String userId,Integer page,Integer pageSize);

    /*
     *  @Description:保存评论
     *  @Params:
     *  @Return:
     */
    public void saveComment(Comments comment);


    /*
     *  @Description:获取评论列表
     *  @Params:
     *  @Return:
     */
    public PagedResult getAllComments(String videoId,Integer page,Integer pageSize);

}
