package com.tjg_project.candy.domain.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjg_project.candy.domain.product.entity.Product;
import com.tjg_project.candy.domain.product.entity.ProductDetailView;
import com.tjg_project.candy.domain.product.entity.ProductQnA;
import com.tjg_project.candy.domain.product.entity.ProductReview;
import com.tjg_project.candy.domain.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/productList")
    public List<Product>  getProductList() {
        return productService.getProductList();
    }

    @GetMapping("/productReviewList")
    public List<ProductReview>  getProductReviewList() {
        return productService.getProductReviewList();
    }

    @GetMapping("/productAllQnAList")
    public List<Map<String, Object>>  getProductProductQnAList() {
        return productService.getProductProductAllQnAList();
    }

    @GetMapping("/productQnAList")
    public List<ProductQnA>  getProductProductQnAList(@RequestParam("ppk") Long ppk) {
        return productService.getProductProductQnAList(ppk);
    }



    @GetMapping("/productBestList")
    public List<Product>  getProductProductBestList() {
        return productService.getProductProductBestList();
    }

    // 상품 정보 취득
    @GetMapping("/productDetail")
    public Optional<ProductDetailView> getProductDetail(@RequestParam("id") Long id) {
        return productService.getProductDetail(id);
    }
    
    // 상품 정보 등록
    @PostMapping("/productAdd")
    public Product saveProduct(@RequestParam("product") String productJson,
                               @RequestPart("files") List<MultipartFile> files)
            throws JsonProcessingException {
        // product JSON 데이터 매핑작업
        ObjectMapper mapper = new ObjectMapper();
        Product product = mapper.readValue(productJson, Product.class);
        return productService.saveProduct(product, files);
    }

    // 상품 정보 수정
    @PostMapping("/productUpdate")
    public Product updateProduct(@RequestParam("product") String productJson,
                                 @RequestPart("files") List<MultipartFile> files)
            throws JsonProcessingException {
        // product JSON 데이터 매핑작업
        ObjectMapper mapper = new ObjectMapper();
        Product product = mapper.readValue(productJson, Product.class);

        return productService.updateProduct(product, files);
    }

    // 상품 정보 삭제
    @GetMapping("/productDelete")
    public boolean deleteProduct(@RequestParam("id") Long id) {
        return productService.deleteProduct(id);
    }

    // 상품 QnA 등록
    @PostMapping("/addQnA")
    public ProductQnA addQnA(@RequestBody ProductQnA qna) {
        return productService.addProductQnA(qna);
    }
}
