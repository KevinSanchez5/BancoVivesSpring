package vives.bancovives.rest.products.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import vives.bancovives.rest.products.dto.input.InputProduct;
import vives.bancovives.rest.products.dto.output.OutputProduct;
import vives.bancovives.rest.products.model.Product;
import vives.bancovives.rest.products.service.ProductService;
import vives.bancovives.utils.PageResponse;
import vives.bancovives.utils.PaginationLinksUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private PaginationLinksUtils paginationLinksUtils;

    @Autowired
    public ProductControllerTest(ProductService categoriasService) {
        this.productService = categoriasService;
        mapper.registerModule(new JavaTimeModule());
    }

    private final ObjectMapper mapper = new ObjectMapper();

    private InputProduct inputProduct;
    private OutputProduct outputProduct;
    private Product product;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        // Producto
        product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setProductType("Electronics");
        product.setDescription("A test product description");
        product.setInterest(5.0);

        // Initialize OutputProduct
        outputProduct = new OutputProduct();
        outputProduct.setId(product.getId());
        outputProduct.setName(product.getName());
        outputProduct.setProductType(product.getProductType());
        outputProduct.setDescription(product.getDescription());
        outputProduct.setInterest(product.getInterest());

        // Initialize InputProduct
        inputProduct = new InputProduct();
        inputProduct.setName("Test Product");
        inputProduct.setProductType("Electronics");
        inputProduct.setDescription("A test product description");
        inputProduct.setInterest(5.0);
    }

    @Test
    void getProducts() throws Exception {
        var list = List.of(product);
        Page<Product> page = new PageImpl<>(list);

        // Arrange
        when(productService.findAll(
                any(Optional.class),
                any(Optional.class),
                any(Optional.class),
                any(Pageable.class))
        ).thenReturn(page);

        // Consulto el endpoint
        MockHttpServletResponse response = mockMvc.perform(
                        get("/v1/products")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        PageResponse<Product> res = mapper.readValue(response.getContentAsString(), new TypeReference<>() {
        });

        // Assert
        assertAll(
                () -> assertEquals(200, response.getStatus()),
                () -> assertEquals(1, res.content().size())
        );

        // Verify
        verify(productService, times(1)).findAll(
                any(Optional.class),
                any(Optional.class),
                any(Optional.class),
                any(Pageable.class)
        );
    }

    @Test
    void getProductById() throws Exception {
        // Arrange
        when(productService.findById(productId)).thenReturn(product);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                get("/v1/products/" + productId)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputProduct responseBody = mapper.readValue(response.getContentAsString(), OutputProduct.class);
        assertAll(
                () -> assertEquals(outputProduct.getId(), responseBody.getId()),
                () -> assertEquals(outputProduct.getName(), responseBody.getName()),
                () -> assertEquals(outputProduct.getProductType(), responseBody.getProductType()),
                () -> assertEquals(outputProduct.getDescription(), responseBody.getDescription()),
                () -> assertEquals(outputProduct.getInterest(), responseBody.getInterest())
        );
    }

    @Test
    void getProductByName() throws Exception {
        // Arrange
        when(productService.findByName(inputProduct.getName())).thenReturn(product);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                get("/v1/products/name/" + inputProduct.getName())
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputProduct responseBody = mapper.readValue(response.getContentAsString(), OutputProduct.class);
        assertAll(
                () -> assertEquals(outputProduct.getId(), responseBody.getId()),
                () -> assertEquals(outputProduct.getName(), responseBody.getName()),
                () -> assertEquals(outputProduct.getProductType(), responseBody.getProductType()),
                () -> assertEquals(outputProduct.getDescription(), responseBody.getDescription()),
                () -> assertEquals(outputProduct.getInterest(), responseBody.getInterest())
        );
    }

    @Test
    void createProduct() throws Exception {
        // Arrange
        when(productService.save(any(InputProduct.class))).thenReturn(product);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                post("/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(inputProduct))
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputProduct responseBody = mapper.readValue(response.getContentAsString(), OutputProduct.class);
        assertAll(
                () -> assertEquals(outputProduct.getId(), responseBody.getId()),
                () -> assertEquals(outputProduct.getName(), responseBody.getName()),
                () -> assertEquals(outputProduct.getProductType(), responseBody.getProductType()),
                () -> assertEquals(outputProduct.getDescription(), responseBody.getDescription()),
                () -> assertEquals(outputProduct.getInterest(), responseBody.getInterest())
        );
    }

    @Test
    void updateProduct() throws Exception {
        // Arrange
        when(productService.updateById(eq(productId), any(InputProduct.class))).thenReturn(product);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                put("/v1/products/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(inputProduct))
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputProduct responseBody = mapper.readValue(response.getContentAsString(), OutputProduct.class);
        assertAll(
                () -> assertEquals(outputProduct.getId(), responseBody.getId()),
                () -> assertEquals(outputProduct.getName(), responseBody.getName()),
                () -> assertEquals(outputProduct.getProductType(), responseBody.getProductType()),
                () -> assertEquals(outputProduct.getDescription(), responseBody.getDescription()),
                () -> assertEquals(outputProduct.getInterest(), responseBody.getInterest())
        );
    }

    @Test
    void deleteProduct() throws Exception {
        // Arrange
        when(productService.deleteById(productId)).thenReturn(product);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                delete("/v1/products/" + productId)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        OutputProduct responseBody = mapper.readValue(response.getContentAsString(), OutputProduct.class);
        assertAll(
                () -> assertEquals(outputProduct.getId(), responseBody.getId()),
                () -> assertEquals(outputProduct.getName(), responseBody.getName()),
                () -> assertEquals(outputProduct.getProductType(), responseBody.getProductType()),
                () -> assertEquals(outputProduct.getDescription(), responseBody.getDescription()),
                () -> assertEquals(outputProduct.getInterest(), responseBody.getInterest())
        );
    }
}
