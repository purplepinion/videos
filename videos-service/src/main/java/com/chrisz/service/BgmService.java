package com.chrisz.service;

import com.chrisz.mapper.BgmMapper;
import com.chrisz.pojo.Bgm;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public interface BgmService {

    /*查询背景音乐列表*/
    public List<Bgm> queryBgmList();

    /*根据bgmId 查询bgm*/
    public Bgm queryBgmById(String bgmId);
}
