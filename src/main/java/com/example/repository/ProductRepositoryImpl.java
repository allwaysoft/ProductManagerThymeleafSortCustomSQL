package com.example.repository;

import com.example.model.Product;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Product mapUserResult(final ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setBrand(rs.getString("brand"));
        product.setMadein(rs.getString("madein"));
        product.setPrice(rs.getLong("price"));
        return product;
    }

    @Override
    public Page<Product> findAllByContaining(String keyword, Pageable page) {

        int count = jdbcTemplate.queryForObject("SELECT count(* ) FROM product WHERE CONCAT(id, ' ', name, ' ' , brand, ' ' , madein, ' ' , price) LIKE CONCAT('%',?,'%') ", Integer.class, keyword);

        Sort.Order order = !page.getSort().isEmpty() ? page.getSort().toList().get(0) : Sort.Order.by("ID");

        List<Product> products = jdbcTemplate.query("SELECT * FROM product WHERE CONCAT(id, ' ', name, ' ' , brand, ' ' , madein, ' ' , price) LIKE CONCAT('%',?,'%') ORDER BY " + order.getProperty() + " "
                + order.getDirection().name() + " LIMIT " + page.getPageSize() + " OFFSET " + page.getOffset(),
                (rs, rowNum) -> mapUserResult(rs), keyword
        );

        return new PageImpl<Product>(products, page, count);
    }

}
