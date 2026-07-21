package com.readhub.bookmanagement.repository;

import com.readhub.bookmanagement.model.Book;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class BookRepositoryImpl implements CustomBookRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Book> searchBooksCustom(Long categoryId, String keyword, Sort sort) {
        StringBuilder jpql = new StringBuilder("SELECT b FROM Book b LEFT JOIN FETCH b.category WHERE 1=1 ");
        
        if (categoryId != null) {
            jpql.append("AND b.category.categoryId = :categoryId ");
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            jpql.append("AND (LOWER(b.title) LIKE LOWER(:keyword) OR LOWER(b.author) LIKE LOWER(:keyword) OR LOWER(b.isbn) LIKE LOWER(:keyword)) ");
        }

        if (sort != null && sort.isSorted()) {
            jpql.append("ORDER BY ");
            String orderStr = sort.stream()
                    .map(order -> "b." + order.getProperty() + " " + order.getDirection().name())
                    .collect(java.util.stream.Collectors.joining(", "));
            jpql.append(orderStr);
        }

        TypedQuery<Book> query = entityManager.createQuery(jpql.toString(), Book.class);
        
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }

        return query.getResultList();
    }

    @Override
    public Page<Book> searchBooksCustom(Long categoryId, String keyword, Pageable pageable) {
        // 1. Build count query
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(b) FROM Book b WHERE 1=1 ");
        
        if (categoryId != null) {
            countJpql.append("AND b.category.categoryId = :categoryId ");
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            countJpql.append("AND (LOWER(b.title) LIKE LOWER(:keyword) OR LOWER(b.author) LIKE LOWER(:keyword) OR LOWER(b.isbn) LIKE LOWER(:keyword)) ");
        }

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql.toString(), Long.class);
        
        if (categoryId != null) {
            countQuery.setParameter("categoryId", categoryId);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            countQuery.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }
        
        Long total = countQuery.getSingleResult();

        // 2. Build result query
        StringBuilder jpql = new StringBuilder("SELECT b FROM Book b LEFT JOIN FETCH b.category WHERE 1=1 ");
        
        if (categoryId != null) {
            jpql.append("AND b.category.categoryId = :categoryId ");
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            jpql.append("AND (LOWER(b.title) LIKE LOWER(:keyword) OR LOWER(b.author) LIKE LOWER(:keyword) OR LOWER(b.isbn) LIKE LOWER(:keyword)) ");
        }

        if (pageable.getSort().isSorted()) {
            jpql.append("ORDER BY ");
            String orderStr = pageable.getSort().stream()
                    .map(order -> "b." + order.getProperty() + " " + order.getDirection().name())
                    .collect(java.util.stream.Collectors.joining(", "));
            jpql.append(orderStr);
        }

        TypedQuery<Book> query = entityManager.createQuery(jpql.toString(), Book.class);
        
        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Book> content = query.getResultList();

        return new PageImpl<>(content, pageable, total);
    }
}
