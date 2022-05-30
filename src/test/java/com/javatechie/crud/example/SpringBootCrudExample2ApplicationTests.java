package com.javatechie.crud.example;

import com.javatechie.crud.example.entity.Product;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootCrudExample2ApplicationTests {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";

    private static RestTemplate restTemplate = null;

    @Autowired
    private TestProductRepository testProductRepository;


    @BeforeAll
    public static void init() {
        restTemplate = new RestTemplate();
    }

    @BeforeEach
    public void setUp() {
        baseUrl = baseUrl.concat(":").concat(port + "").concat("/products");
    }

    @Test
    public void testAddProduct() {
        Product product = new Product("headset", 1, 7999);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, product, String.class);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(3, testProductRepository.findAll().size());
    }

    @Test
    @Sql(scripts = { "classpath:test/schema.sql", "classpath:test/data.sql" })
    public void testGetProducts(){
        List<Product> products = restTemplate.getForObject(baseUrl, List.class);
        assertAll(
                () -> assertNotNull(products),
                () -> assertEquals(2, products.size())
        );
    }

    @Test
    @Sql(statements = "INSERT INTO PRODUCT_TBL (id,name, quantity, price) VALUES (4,'AC', 1, 34000)",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM PRODUCT_TBL where name='AC'",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testGetProductById(){
        Product product = restTemplate.getForObject(baseUrl.concat("/{id}"), Product.class, 4);
        assertAll(
                () -> assertNotNull(product),
                () -> assertEquals("AC", product.getName())
        );
    }

    @Test
    @Sql(statements = "INSERT INTO PRODUCT_TBL (id,name, quantity, price) VALUES (5,'AC', 1, 34000)",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testUpdateProduct(){
        List<Product> all = testProductRepository.findAll();
        Product product = new Product("AC", 1, 50000);
        restTemplate.put(baseUrl+"/update/{id}", product, 5);
        Product productFromDB = testProductRepository.findById(5).get();
        assertAll(
                () -> assertNotNull(productFromDB),
                () -> assertEquals(50000, productFromDB.getPrice())
        );
    }

    @Test
    public void testDeleteProduct(){
        List<Product> all = testProductRepository.findAll();
        assertEquals(2, all.size());
        restTemplate.delete(baseUrl+"/delete/{id}", 1);
        assertEquals(1, testProductRepository.findAll().size());
    }


}
