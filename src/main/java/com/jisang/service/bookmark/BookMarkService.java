package com.jisang.service.bookmark;

import java.util.List;

import com.jisang.dto.bookmark.BookMarkInfoDTO;

public interface BookMarkService {
    public boolean isBookMarked(BookMarkInfoDTO bookMarkInfo);

    public void modifyBookMark(BookMarkInfoDTO bookMarkInfo);

    public void deleteBookMark(BookMarkInfoDTO bookMarkInfo);

    public List<BookMarkInfoDTO> findBookmarks(int userId);
}
