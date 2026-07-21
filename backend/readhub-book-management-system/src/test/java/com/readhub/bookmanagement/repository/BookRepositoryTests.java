package com.readhub.bookmanagement.repository;

import com.readhub.bookmanagement.model.Book;
import com.readhub.bookmanagement.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookRepositoryTests {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category fiction;
    private Category science;
    private Book book1;
    private Book book2;
    private Book book3;

    @BeforeEach
    public void setUp() {
        fiction = new Category();
        fiction.setName("Fiction");
        entityManager.persist(fiction);

        science = new Category();
        science.setName("Science");
        entityManager.persist(science);

        book1 = Book.builder()
                .isbn("111-111")
                .title("Clean Code")
                .author("Robert Martin")
                .publicationYear(2008)
                .totalCopies(10)
                .availableCopies(10)
                .category(science)
                .build();
        entityManager.persist(book1);

        book2 = Book.builder()
                .isbn("222-222")
                .title("The Hobbit")
                .author("J.R.R. Tolkien")
                .publicationYear(1937)
                .totalCopies(5)
                .availableCopies(5)
                .category(fiction)
                .build();
        entityManager.persist(book2);

        book3 = Book.builder()
                .isbn("333-333")
                .title("Introduction to Algorithms")
                .author("Thomas Cormen")
                .publicationYear(2009)
                .totalCopies(8)
                .availableCopies(8)
                .category(science)
                .build();
        entityManager.persist(book3);

        entityManager.flush();
    }

    @Test
    public void testSearchByKeywordInTitle() {
        List<Book> books = bookRepository.searchBooksCustom(null, "hobbit", Sort.by("id"));
        assertEquals(1, books.size());
        assertEquals("The Hobbit", books.get(0).getTitle());
    }

    @Test
    public void testSearchByKeywordInAuthor() {
        List<Book> books = bookRepository.searchBooksCustom(null, "martin", Sort.by("id"));
        assertEquals(1, books.size());
        assertEquals("Clean Code", books.get(0).getTitle());
    }

    @Test
    public void testFilterByCategory() {
        List<Book> books = bookRepository.searchBooksCustom(science.getCategoryId(), null, Sort.by("id"));
        assertEquals(2, books.size());
        assertTrue(books.stream().anyMatch(b -> b.getTitle().equals("Clean Code")));
        assertTrue(books.stream().anyMatch(b -> b.getTitle().equals("Introduction to Algorithms")));
    }

    @Test
    public void testCombineKeywordAndCategory() {
        List<Book> books = bookRepository.searchBooksCustom(science.getCategoryId(), "Algorithms", Sort.by("id"));
        assertEquals(1, books.size());
        assertEquals("Introduction to Algorithms", books.get(0).getTitle());
    }

    @Test
    public void testCombineKeywordAndCategory_NoMatch() {
        List<Book> books = bookRepository.searchBooksCustom(science.getCategoryId(), "Hobbit", Sort.by("id"));
        assertTrue(books.isEmpty());
    }

    @Test
    public void testPagination() {
        Page<Book> page = bookRepository.searchBooksCustom(science.getCategoryId(), null, PageRequest.of(0, 1, Sort.by("title").ascending()));
        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertEquals(1, page.getContent().size());
        assertEquals("Clean Code", page.getContent().get(0).getTitle()); // 'C' comes before 'I'
    }

    @Test
    public void testSorting() {
        List<Book> booksAsc = bookRepository.searchBooksCustom(null, null, Sort.by(Sort.Direction.ASC, "publicationYear"));
        assertEquals(3, booksAsc.size());
        assertEquals("The Hobbit", booksAsc.get(0).getTitle()); // 1937
        assertEquals("Clean Code", booksAsc.get(1).getTitle()); // 2008
        assertEquals("Introduction to Algorithms", booksAsc.get(2).getTitle()); // 2009
    }
}
