package com.tjg_project.candy.domain.product.service;

import com.tjg_project.candy.domain.order.entity.KakaoPay;
import com.tjg_project.candy.domain.product.entity.Product;
import com.tjg_project.candy.domain.product.entity.ProductDetailView;
import com.tjg_project.candy.domain.product.entity.ProductQnA;
import com.tjg_project.candy.domain.product.entity.ProductReview;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductService {
    List<Product> getProductProductBestList();
    List<Map<String, Object>> getProductProductAllQnAList();
    List<ProductQnA> getProductProductQnAList(Long ppk);
    List<ProductReview> getProductReviewList();
    List<Product> getProductList();
    Optional<ProductDetailView> getProductDetail(Long id);
    Product saveProduct(Product product, List<MultipartFile> files);
    Product updateProduct(Product product, List<MultipartFile> files);
    boolean deleteProduct(Long id);
    ProductQnA addProductQnA(ProductQnA qna);
    boolean updateCount(List<KakaoPay.ProductInfo> productInfo);
}
