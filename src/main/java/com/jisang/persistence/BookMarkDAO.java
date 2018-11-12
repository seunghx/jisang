package com.jisang.persistence;

import java.util.List;

import com.jisang.dto.bookmark.BookMarkInfoDTO;

public interface BookMarkDAO extends MybatisMapper {
	public void create(BookMarkInfoDTO dto);
	public void delete(BookMarkInfoDTO dto);
	public int readCount(BookMarkInfoDTO dto);
	
	public List<BookMarkInfoDTO> readList(int userId);
}
