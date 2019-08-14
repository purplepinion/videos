package com.chrisz.mapper;

import java.util.List;

import com.chrisz.pojo.Comments;
import com.chrisz.pojo.vo.CommentsVO;
import com.chrisz.utils.MyMapper;

public interface CommentsMapperCustom extends MyMapper<Comments> {
	
	public List<CommentsVO> queryComments(String videoId);
}