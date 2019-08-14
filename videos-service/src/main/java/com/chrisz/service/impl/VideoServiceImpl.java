package com.chrisz.service.impl;

import com.chrisz.mapper.*;
import com.chrisz.pojo.Comments;
import com.chrisz.pojo.SearchRecords;
import com.chrisz.pojo.UsersLikeVideos;
import com.chrisz.pojo.Videos;
import com.chrisz.pojo.vo.CommentsVO;
import com.chrisz.pojo.vo.VideosVO;
import com.chrisz.service.VideoService;
import com.chrisz.utils.PagedResult;
import com.chrisz.utils.TimeAgoUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideosMapper videosMapper;

    @Autowired
    private VideosMapperCustom videosMapperCustom;

    @Autowired
    private SearchRecordsMapper searchRecordsMapper;

    @Autowired
    private UsersLikeVideosMapper usersLikeVideosMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private CommentsMapperCustom commentsMapperCustom;


    @Autowired
    private Sid sid;


    /*保存视频到数据库*/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public String saveVideo(Videos video) {

        //生成主键
        String id = sid.nextShort();
        video.setId(id);
        videosMapper.insertSelective(video);

        return id;
    }



    /*更新视频封面*/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateVideo(String videoId, String coverPath) {

        Videos video = new Videos();
        video.setId(videoId);
        video.setCoverPath(coverPath);
        videosMapper.updateByPrimaryKeySelective(video);
    }


    /*查询所有视频列表*/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public PagedResult getAllVideos(Videos video,Integer isSaveRecord,Integer page,Integer pageSize){

        String desc = video.getVideoDesc();
        String userId = video.getUserId();

        if(isSaveRecord!=null && isSaveRecord==1){
            String recordId = sid.nextShort();
            SearchRecords record = new SearchRecords();
            record.setId(recordId);
            record.setContent(desc);
            searchRecordsMapper.insert(record);
        }

        //分页 页数、页面大小
        PageHelper.startPage(page,pageSize);

        //todo
        List<VideosVO> list = videosMapperCustom.queryAllVideos(desc,userId);

        PageInfo<VideosVO> pageList = new PageInfo<>(list);
        PagedResult pagedResult = new PagedResult();
        pagedResult.setPage(page);
        pagedResult.setTotal(pageList.getPages());
        pagedResult.setRows(list);
        pagedResult.setRecords(pageList.getTotal());
        return pagedResult;
    }

    /*获得热词*/
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<String> getHotWords() {

        return searchRecordsMapper.getHotwords();
    }

    /*用户喜欢视频*/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void userLikeVideo(String userId, String videoId, String videoCreaterId) {

        //1.保存用户喜欢视频记录
        String likeId = sid.nextShort();

        UsersLikeVideos ulv = new UsersLikeVideos();
        ulv.setId(likeId);
        ulv.setUserId(userId);
        ulv.setVideoId(videoId);

        usersLikeVideosMapper.insert(ulv);

        //2.视频收到like累加
        videosMapperCustom.addVideoLikeCount(videoId);

        //3.视频创建者收到like累加
        usersMapper.addReceiveLikeCount(videoCreaterId);

    }

    /*用户取消喜欢视频*/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void userUnLikeVideo(String userId, String videoId, String videoCreaterId) {

        //1.删除用户喜欢视频记录
        Example example = new Example(UsersLikeVideos.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("videoId",videoId);
        usersLikeVideosMapper.deleteByExample(example);

        //2.视频收到like累减
        videosMapperCustom.reduceVideoLikeCount(videoId);

        //3.视频创建者收到like累减
        usersMapper.reduceReceiveLikeCount(videoCreaterId);
    }

    /*查询用户喜欢视频*/
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedResult queryMyLikeVideos(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page,pageSize);
        List<VideosVO> list = videosMapperCustom.queryMyLikeVideos(userId);

        PageInfo<VideosVO> pageList = new PageInfo<>(list);

        PagedResult pagedResult = new PagedResult();

        pagedResult.setPage(page);
        pagedResult.setTotal(pageList.getPages());
        pagedResult.setRows(list);
        pagedResult.setRecords(pageList.getTotal());

        return pagedResult;
    }

    /*查询用户关注的人发布的视频列表*/
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedResult queryMyFollowVideos(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page,pageSize);
        List<VideosVO> list = videosMapperCustom.queryMyFollowVideos(userId);

        PageInfo<VideosVO> pageList = new PageInfo<>(list);

        PagedResult pagedResult = new PagedResult();

        pagedResult.setPage(page);
        pagedResult.setTotal(pageList.getPages());
        pagedResult.setRows(list);
        pagedResult.setRecords(pageList.getTotal());

        return pagedResult;
    }

    /*保存评论*/
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveComment(Comments comment) {
        String cId = sid.nextShort();
        comment.setId(cId);
        comment.setCreateTime(new Date());
        commentsMapper.insert(comment);
    }

    /*获取评论列表*/
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedResult getAllComments(String videoId, Integer page, Integer pageSize) {
        PageHelper.startPage(page,pageSize);

        List<CommentsVO> list = commentsMapperCustom.queryComments(videoId);

        for (CommentsVO cv:list){
            String timeAgo = TimeAgoUtils.format(cv.getCreateTime());
            cv.setTimeAgoStr(timeAgo);
        }

        PageInfo<CommentsVO> pageList = new PageInfo<>(list);

        PagedResult pagedResult = new PagedResult();
        pagedResult.setTotal(pageList.getPages());
        pagedResult.setRows(list);
        pagedResult.setPage(page);
        pagedResult.setRecords(pageList.getTotal());

        return  pagedResult;
    }


}
