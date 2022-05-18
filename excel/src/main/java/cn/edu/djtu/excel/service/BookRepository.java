package cn.edu.djtu.excel.service;

import cn.edu.djtu.excel.entity.Book;

/**
 * @author zzx
 * @date 2021/9/17
 */
public interface BookRepository {
    Book getByIsbn(String isbn);
}
