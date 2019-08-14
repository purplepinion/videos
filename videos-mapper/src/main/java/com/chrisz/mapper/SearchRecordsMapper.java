package com.chrisz.mapper;

import java.util.List;

import com.chrisz.pojo.SearchRecords;
import com.chrisz.utils.MyMapper;

public interface SearchRecordsMapper extends MyMapper<SearchRecords> {
	/*获取热词列表*/
	public List<String> getHotwords();
}