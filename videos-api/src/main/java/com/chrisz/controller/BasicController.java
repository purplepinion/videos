package com.chrisz.controller;

import com.chrisz.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BasicController {

    @Autowired
    protected RedisOperator redis;

    public static final String USER_REDIS_SESSION = "user_redis_session";

    // 文件保存的命名空间
    public static final String FILE_SPACE = "F:/videosSpace";

    // ffmpeg所在目录
    public static final String FFMPEG_EXE = "F:\\workspace\\ffmpeg\\bin\\ffmpeg.exe";

    //首页视频每页个数
    public static final Integer PAGE_SIZE = 5;

}
