package com.chrisz.controller;

import com.chrisz.service.BgmService;
import com.chrisz.utils.JSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bgm")
@Api(value = "BGM相关的接口",tags = {"BGM相关业务的controller"})
public class BgmController {

    @Autowired
    private BgmService bgmService;

    @ApiOperation(value = "获取BGM列表",notes = "获取BGM列表的接口")
    @PostMapping("/list")
    public JSONResult list(){

        return JSONResult.ok(bgmService.queryBgmList());
    }
}
