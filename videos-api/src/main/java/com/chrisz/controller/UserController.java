package com.chrisz.controller;

import com.chrisz.pojo.Users;
import com.chrisz.pojo.UsersReport;
import com.chrisz.pojo.vo.PublisherVideo;
import com.chrisz.pojo.vo.UsersVO;
import com.chrisz.service.UserService;
import com.chrisz.utils.JSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@RestController
@Api(value = "用户业务相关的接口",tags = {"用户相关业务的controller"})
@RequestMapping("/user")
public class UserController extends BasicController {

    @Autowired
    private UserService userService;


    @ApiOperation(value = "上传用户头像",notes = "用户上传头像的接口")
    @ApiImplicitParam(name = "userId",value = "用户id",required = true,dataType = "String",paramType = "query")
    @PostMapping("/uploadFace")
    public JSONResult uploadFace(String userId, @RequestParam("file")MultipartFile[] files) throws IOException {

        if(StringUtils.isBlank(userId))
            return JSONResult.errorMsg("用户id为空");

        /*用户文件保存位置命名空间*/
        String fileSpace = "F:/videosSpace";
        /*用户头像保存位置*/
        String facePathDB = "/" + userId + "/face";

        FileOutputStream fos = null;
        InputStream is = null;

        try{
            if(files!=null && files.length>0) {
                String fileName = files[0].getOriginalFilename();
                if (!StringUtils.isBlank(fileName)) {
                    /*用户头像保存绝对路径*/
                    String finalPath = fileSpace + facePathDB + "/" + fileName;

                    /*数据库保存的地址*/
                    facePathDB += "/" + fileName;

                    File outFile = new File(finalPath);

                    if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
                        outFile.getParentFile().mkdirs();
                    }

                    fos = new FileOutputStream(outFile);
                    is = files[0].getInputStream();
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

        Users user = new Users();
        user.setId(userId);
        user.setFaceImage(facePathDB);
        userService.updateUserInfo(user);

        return JSONResult.ok(facePathDB);
    }

    @ApiOperation(value = "查询用户信息",notes = "用户信息查询的接口")
    @ApiImplicitParam(name = "userId",value = "用户id",required = true,dataType = "String",paramType = "query")
    @PostMapping("/query")
    public JSONResult query(String userId, String fanId) throws IOException {

        if(StringUtils.isBlank(userId))
            return JSONResult.errorMsg("用户id为空");


        Users userInfo = userService.queryUserInfo(userId);
        UsersVO userInfoVO = new UsersVO();
        BeanUtils.copyProperties(userInfo,userInfoVO);

        userInfoVO.setFollow(userService.queryIsFollow(userId,fanId));
        return JSONResult.ok(userInfoVO);
    }

    @PostMapping("/queryPublisher")
    public JSONResult queryPublisher(String loginUserId, String videoId, String publisherUserId) throws IOException {

        if(StringUtils.isBlank(publisherUserId)){
            return JSONResult.errorMsg("参数空...");
        }

        //1.查询视频发布者信息
        Users userInfo = userService.queryUserInfo(publisherUserId);
        UsersVO publisher = new UsersVO();
        BeanUtils.copyProperties(userInfo,publisher);

        //2.查询登录用户和视频的点赞关系
        boolean isLikeVideo = userService.isUserLikeVideo(loginUserId,videoId);

        PublisherVideo publisherVideo = new PublisherVideo();
        publisherVideo.setPublisher(publisher);
        publisherVideo.setUserLikeVideo(isLikeVideo);

        return JSONResult.ok(publisherVideo);
    }


    @PostMapping("/follow")
    public JSONResult follow(String userId, String fanId) throws IOException {

        if(StringUtils.isBlank(userId) || StringUtils.isBlank(fanId))
            return JSONResult.errorMsg("id为空...");
        userService.saveUserFanRelation(userId,fanId);
        return JSONResult.ok("关注成功...");
    }

    @PostMapping("/unFollow")
    public JSONResult unFollow(String userId, String fanId) throws IOException {

        if(StringUtils.isBlank(userId) || StringUtils.isBlank(fanId))
            return JSONResult.errorMsg("id为空...");
        userService.deleteUserFanRelation(userId,fanId);
        return JSONResult.ok("取消关注成功...");
    }

    @PostMapping("/report")
    public JSONResult report(@RequestBody UsersReport usersReport) throws IOException {

        //保存举报信息
        userService.report(usersReport);
        return JSONResult.ok("举报成功,感谢你的监督...");
    }
}
