package com.chrisz.controller;

import com.chrisz.enums.VideoStatusEnum;
import com.chrisz.pojo.Bgm;
import com.chrisz.pojo.Comments;
import com.chrisz.pojo.Videos;
import com.chrisz.service.BgmService;
import com.chrisz.service.VideoService;
import com.chrisz.utils.FetchVideoCover;
import com.chrisz.utils.JSONResult;
import com.chrisz.utils.MergeVideoMp3;
import com.chrisz.utils.PagedResult;
import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/video")
@Api(value = "视频业务相关的接口",tags = {"视频相关业务的controller"})
public class VideoController extends BasicController{

    @Autowired
    private BgmService bgmService;


    @Autowired
    private VideoService videoService;



    @ApiOperation(value = "上传视频",notes = "用户上传视频的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId",value = "用户id",required = true,dataType = "String",paramType = "form"),
            @ApiImplicitParam(name = "bgmId",value = "背景音乐id",required = false,dataType = "String",paramType = "form"),
            @ApiImplicitParam(name = "videoSeconds",value = "视频时长",required = true,dataType = "double",paramType = "form"),
            @ApiImplicitParam(name = "videoWidth",value = "视频宽度",required = true,dataType = "int",paramType = "form"),
            @ApiImplicitParam(name = "videoHeight",value = "视频高度",required = true,dataType = "int",paramType = "form"),
            @ApiImplicitParam(name = "description",value = "视频描述",required = false,dataType = "String",paramType = "form"),
    })
    @PostMapping(value="/upload",headers = {"content-type=multipart/form-data"})
    public JSONResult upload(String userId,
                             String bgmId,
                             double videoSeconds,
                             int videoWidth,
                             int videoHeight,
                             String description,
                             @ApiParam(value = "短视频",required = true) MultipartFile file) throws Exception {

        if(StringUtils.isBlank(userId))
            return JSONResult.errorMsg("用户id为空");

        /*用户文件保存位置命名空间*/
        //String fileSpace = "F:/videosSpace";
        /*用户上传视频保存位置*/
        String videoPathDB = "/" + userId + "/video";
        String coverPathDB = "/" + userId + "/video";



        FileOutputStream fos = null;
        InputStream is = null;

        String finalPath = null;

        try{
            if(file!=null) {
                String fileName = file.getOriginalFilename();
                //abc.mp4

                String fileNamePrefix = fileName.split("\\.")[0];
                //abc

                if (!StringUtils.isBlank(fileName)) {
                    /*用户视频保存绝对路径*/
                    finalPath = FILE_SPACE + videoPathDB + "/" + fileName;

                    /*数据库保存的地址*/
                    videoPathDB += "/" + fileName;
                    //coverPathDB = coverPathDB + "/" + fileNamePrefix + ".jpg";
                    coverPathDB = coverPathDB + "/" + UUID.randomUUID().toString().substring(0,20) + ".jpg";

                    File outFile = new File(finalPath);

                    if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
                        outFile.getParentFile().mkdirs();
                    }

                    fos = new FileOutputStream(outFile);
                    is = file.getInputStream();
                    IOUtils.copy(is, fos);
                }else {
                    return JSONResult.errorMsg("上传出错1...");
                }
            }
        }catch (Exception e){

            e.printStackTrace();
            return JSONResult.errorMsg("上传出错2..");
        }finally {

            if(fos!=null){
                fos.flush();
                fos.close();
            }
        }


        if(StringUtils.isNotBlank(bgmId)){
            Bgm bgm = bgmService.queryBgmById(bgmId);
            String mp3InputPath = FILE_SPACE + bgm.getPath();
            System.out.println("bgm.getPath():" + bgm.getPath());
            MergeVideoMp3 tool = new MergeVideoMp3(FFMPEG_EXE);
            String videoInputPath = finalPath;

            String videoOutputName = UUID.randomUUID().toString() + ".mp4";
            videoPathDB = "/" + userId + "/video/" + videoOutputName;
            finalPath = FILE_SPACE + videoPathDB;

            //合成视频
            tool.convertor(videoInputPath,mp3InputPath,videoSeconds,finalPath);

        }

        System.out.println("uploadPathDB:" + videoPathDB);
        System.out.println("finalPath:" + finalPath);

        //截取封面
        FetchVideoCover videoCoverTool = new FetchVideoCover(FFMPEG_EXE);
        videoCoverTool.getCover(finalPath,FILE_SPACE + coverPathDB);

        //保存视频信息到数据库
        Videos video = new Videos();
        video.setAudioId(bgmId);
        video.setUserId(userId);
        video.setVideoSeconds((float)videoSeconds);
        video.setVideoHeight(videoHeight);
        video.setVideoWidth(videoWidth);
        video.setVideoDesc(description);
        video.setVideoPath(videoPathDB);
        video.setCoverPath(coverPathDB);
        video.setStatus(VideoStatusEnum.SUCCESS.getValue());
        video.setCreateTime(new Date());

        String videoId = videoService.saveVideo(video);
        return JSONResult.ok(videoId);
    }


    @ApiOperation(value = "上传视频封面",notes = "用户上传视频封面的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId",value = "用户id",required = true,dataType = "String",paramType = "form"),
            @ApiImplicitParam(name = "videoId",value = "视频id",required = true,dataType = "String",paramType = "form"),
    })
    @PostMapping(value="/uploadCover",headers = {"content-type=multipart/form-data"})
    public JSONResult uploadCover(String userId,
                                  String videoId,
                                  @ApiParam(value = "短视频封面",required = true) MultipartFile file) throws Exception {

        if(StringUtils.isBlank(userId)||StringUtils.isBlank(videoId))
            return JSONResult.errorMsg("用户id和视频id不能为空");

        /*用户文件保存位置命名空间*/
        //String fileSpace = "F:/videosSpace";
        /*用户上传视频封面保存位置*/
        String CoverPathDB = "/" + userId + "/video";


        FileOutputStream fos = null;
        InputStream is = null;

        String finalPath = null;

        try{
            if(file!=null) {
                String fileName = file.getOriginalFilename();
                if (!StringUtils.isBlank(fileName)) {
                    /*用户保存绝对路径*/
                    finalPath = FILE_SPACE + CoverPathDB + "/" + fileName;

                    /*数据库保存的地址*/
                    CoverPathDB += "/" + fileName;

                    File outFile = new File(finalPath);

                    if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
                        outFile.getParentFile().mkdirs();
                    }

                    fos = new FileOutputStream(outFile);
                    is = file.getInputStream();
                    IOUtils.copy(is, fos);
                }else {
                    return JSONResult.errorMsg("上传出错1...");
                }
            }
        }catch (Exception e){

            e.printStackTrace();
            return JSONResult.errorMsg("上传出错2..");
        }finally {

            if(fos!=null){
                fos.flush();
                fos.close();
            }
        }

        videoService.updateVideo(videoId,CoverPathDB);
        return JSONResult.ok();
    }



    /*isSaveRecord  0或者null 不需要保存 ,1 需要保存*/
    @ApiOperation(value = "获取视频列表",notes = "获取视频列表的接口")
    @PostMapping(value="/showAll")
    public JSONResult showAll(@RequestBody Videos video,
                              Integer isSaveRecord,
                              Integer page, Integer pageSize){



        if(page == null){
            page = 1;
        }

        if(pageSize==null){
            pageSize = PAGE_SIZE;
        }
        PagedResult result = videoService.getAllVideos(video,isSaveRecord,page,pageSize);

        return JSONResult.ok(result);
    }




    @PostMapping(value="/hot")
    public JSONResult hot() throws Exception {
        return JSONResult.ok(videoService.getHotWords());
    }


    @PostMapping(value="/userLike")
    public JSONResult userLikeVideo(String userId, String videoId, String videoCreaterId) throws Exception {
        videoService.userLikeVideo(userId,videoId,videoCreaterId);
        return  JSONResult.ok();
    }

    @PostMapping(value="/userUnLike")
    public JSONResult userUnLikeVideo(String userId, String videoId, String videoCreaterId) throws Exception {
        videoService.userUnLikeVideo(userId,videoId,videoCreaterId);
        return  JSONResult.ok();
    }

    @ApiOperation(value = "获取用户喜欢视频列表",notes = "获取用户喜欢视频列表")
    @PostMapping(value="/showMyLike")
    public JSONResult showMyLike(String userId,
                                 Integer page, Integer pageSize){

        if(StringUtils.isBlank(userId)){
            return JSONResult.ok();
        }


        if(page == null){
            page = 1;
        }

        if(pageSize==null){
            pageSize = PAGE_SIZE;
        }
        PagedResult result = videoService.queryMyLikeVideos(userId,page,pageSize);

        return JSONResult.ok(result);
    }

    @ApiOperation(value = "获取用户关注的人视频列表",notes = "获取用户关注的人视频列表")
    @PostMapping(value="/showMyFollow")
    public JSONResult showMyFollow(String userId,
                                   Integer page, Integer pageSize){

        if(StringUtils.isBlank(userId)){
            return JSONResult.ok();
        }


        if(page == null){
            page = 1;
        }

        if(pageSize==null){
            pageSize = PAGE_SIZE;
        }
        PagedResult result = videoService.queryMyFollowVideos(userId,page,pageSize);

        return JSONResult.ok(result);
    }


    @PostMapping(value="/saveComment")
    public JSONResult saveComment(@RequestBody Comments comment,
                                  String fatherCommentId,
                                  String toUserId) throws Exception {
        if(StringUtils.isNotBlank(fatherCommentId)&&StringUtils.isNotBlank(toUserId)){
            comment.setFatherCommentId(fatherCommentId);
            comment.setToUserId(toUserId);
        }
        videoService.saveComment(comment);
        return  JSONResult.ok();
    }

    @PostMapping(value="/getVideoComments")
    public JSONResult getVideoComments(String videoId, Integer page, Integer pageSize) throws Exception {
        if(StringUtils.isBlank(videoId)){
            return  JSONResult.ok();
        }


        if(page==null){
            page=1;
        }

        if(pageSize==null){
            pageSize = 10;
        }

        PagedResult pageList = videoService.getAllComments(videoId,page,pageSize);
        return  JSONResult.ok(pageList);
    }


}
