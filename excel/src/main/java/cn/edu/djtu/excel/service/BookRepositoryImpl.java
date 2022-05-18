package cn.edu.djtu.excel.service;

import cn.edu.djtu.excel.entity.Book;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @author zzx
 * @date 2021/9/17
 */
@Component
public class BookRepositoryImpl implements BookRepository {
    @Override
    @Cacheable("books")
    public Book getByIsbn(String isbn) {
        try {
            long time = 3000L;
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return new Book(isbn, "Some book");
    }
}
