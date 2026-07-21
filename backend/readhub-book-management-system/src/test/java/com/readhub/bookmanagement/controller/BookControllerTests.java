package com.readhub.bookmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.readhub.bookmanagement.dto.BookRequest;
import com.readhub.bookmanagement.model.Book;
import com.readhub.bookmanagement.model.Category;
import com.readhub.bookmanagement.repository.CategoryRepository;
import com.readhub.bookmanagement.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BookControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    private Category science;
    private BookRequest validRequest;

    @BeforeEach
    public void setUp() {
        science = categoryRepository.findByName("Science")
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName("Science");
                    return categoryRepository.save(c);
                });

        validRequest = new BookRequest();
        validRequest.setIsbn("978-3-16-148410-0");
        validRequest.setTitle("Test Book unique title");
        validRequest.setAuthor("Test Author");
        validRequest.setPublicationYear(2022);
        validRequest.setCategoryId(science.getCategoryId());
        validRequest.setTotalCopies(10);
    }

    @Test
    public void testGetAllBooks_PublicAccess() throws Exception {
        Book book = Book.builder()
                .isbn("978-3-16-148410-1")
                .title("Test Book unique title")
                .author("Test Author")
                .publicationYear(2022)
                .category(science)
                .totalCopies(10)
                .availableCopies(10)
                .build();
        bookRepository.save(book);

        mockMvc.perform(get("/api/books")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "admin@readhub.com", roles = "ADMIN")
    public void testAddBook_AdminAccess() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Book unique title"));
    }

    @Test
    @WithMockUser(username = "student@readhub.com", roles = "STUDENT")
    public void testAddBook_StudentAccessForbidden() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testAddBook_UnauthenticatedAccessUnauthorized() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden()); // Spring security rejects anonymous write with 403/Forbidden when no credentials are provided
    }

    @Test
    @WithMockUser(username = "admin@readhub.com", roles = "ADMIN")
    public void testAddBook_ValidationFailure() throws Exception {
        BookRequest invalidRequest = new BookRequest(); // Empty fields trigger validation checks
        
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
