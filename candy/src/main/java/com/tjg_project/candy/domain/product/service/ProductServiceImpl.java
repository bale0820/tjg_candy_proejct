package com.tjg_project.candy.domain.product.service;

import com.tjg_project.candy.domain.order.entity.KakaoPay;
import com.tjg_project.candy.domain.order.repository.OrderDetailRepository;
import com.tjg_project.candy.domain.product.entity.Product;
import com.tjg_project.candy.domain.product.entity.ProductDetailView;
import com.tjg_project.candy.domain.product.entity.ProductQnA;
import com.tjg_project.candy.domain.product.entity.ProductReview;
import com.tjg_project.candy.domain.product.repository.ProductDetailViewRepository;
import com.tjg_project.candy.domain.product.repository.ProductQnARepository;
import com.tjg_project.candy.domain.product.repository.ProductRepository;
import com.tjg_project.candy.domain.product.repository.ProductReviewRepository;
import com.tjg_project.candy.global.s3.S3Service;
import com.tjg_project.candy.global.s3.S3Uploader;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    // 상품, 속성, 이미지 구분
    private final static int PRODUCT_IMAGES = 0;
    private final static int PRODUCT_INFORMATION = 1;
    private final static int PRODUCT_DESCRIPTION = 2;

    // 파일 업로드(application.yml의 file)
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductReviewRepository productReviewRepository;
    @Autowired
    private ProductQnARepository productQnARepository;
    @Autowired
    private ProductDetailViewRepository productDetailViewRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
//    @Autowired
//    private S3Uploader s3Uploader;
    @Autowired
    private S3Service s3Service;
    @Override
    public List<Product> getProductList() {
        return productRepository.findAll();
    }

    @Override
    public List<ProductReview> getProductReviewList() {
        return productReviewRepository.findAllReviewWithUserName();
    }

    @Override
    public List<Map<String, Object>> getProductProductAllQnAList() {
        return productQnARepository.findAllProductQnAWithUserName();
    }

    @Override
    public List<ProductQnA> getProductProductQnAList(Long ppk) {
        return productQnARepository.findByPpk(ppk);
    }

    @Override
    public List<Product> getProductProductBestList() {
        return orderDetailRepository.findBestSellingProducts();
    }

    @Override
    public Optional<ProductDetailView> getProductDetail(Long id) {
        return productDetailViewRepository.findById(id);
    }

    @Override
    // 상품 정보 등록
    public Product saveProduct(Product product, List<MultipartFile> files) {
        // 이미지 정보 설정
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            setImages(product, file, i);
        }

        // 핫딜 정보 설정(DC값이 설정되있으면 true 아니면 false)
        product.setHotDeal(product.getDc() != 0);
        // 등록 날짜
        product.setProductDate(LocalDateTime.now());

        // product테이블에 등록
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Product product, List<MultipartFile> files) {
        // product 테이블의 상품 취득(id)
        Product findProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new RuntimeException("상품 없음"));

        // 이미지 정보 설정
        for (int idx = 0; idx < files.size(); idx++) {
            MultipartFile file = files.get(idx);
            setNewImagesAndDeleteOldImages(findProduct, file, idx);
        }
        // 입력 받은 데이터로 변경
        findProduct.setAllergyInfo(product.getAllergyInfo());
        findProduct.setBrandName(product.getBrandName());
        findProduct.setCount(product.getCount());
        findProduct.setDc(product.getDc());
        findProduct.setDelType(product.getDelType());
        findProduct.setDescription(product.getDescription());
        findProduct.setNotes(product.getNotes());
        findProduct.setOrigin(product.getOrigin());
        findProduct.setPrice(product.getPrice());
        findProduct.setProductName(product.getProductName());
        findProduct.setSeller(product.getSeller());
        findProduct.setUnit(product.getUnit());
        findProduct.setWeight(product.getWeight());
        findProduct.setCategorySub(product.getCategorySub());



        // 핫딜 정보 설정(DC값이 설정되있으면 true 아니면 false)
        findProduct.setHotDeal(product.getDc() != 0);
        // 등록 날짜
        findProduct.setProductDate(LocalDateTime.now());

        // product테이블에 등록
        return productRepository.save(findProduct);
    }


    @Override
    @Transactional
    public boolean deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("상품 없음"));

        // S3 파일 삭제
        s3Service.delete("productImages", product.getImageUrl());
        s3Service.delete("productDescription", product.getProductDescriptionImage());
        s3Service.delete("productInformation", product.getProductInformationImage());

        productRepository.delete(product);
        return true;
    }

    @Override
    public ProductQnA addProductQnA(ProductQnA qna) {
        qna.setStatus("답변대기");
        return productQnARepository.save(qna);
    }


    public void setNewImagesAndDeleteOldImages(Product product, MultipartFile file, int idx) {

        if (file == null || file.isEmpty()) {
            return;
        }

        String originalFilename = file.getOriginalFilename();
        String savedFileName = UUID.randomUUID() + "_" + originalFilename;

        String s3Dir;
        String oldFile = null;

        switch (idx) {
            case PRODUCT_IMAGES:
                s3Dir = "productImages";
                oldFile = product.getImageUrl();
                product.setImageUrl(savedFileName);
                product.setImageUrlName(originalFilename);
                break;

            case PRODUCT_INFORMATION:
                s3Dir = "productInformation";
                oldFile = product.getProductInformationImage();
                product.setProductInformationImage(savedFileName);
                break;

            case PRODUCT_DESCRIPTION:
                s3Dir = "productDescription";
                oldFile = product.getProductDescriptionImage();
                product.setProductDescriptionImage(savedFileName);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + idx);
        }

        // 1️⃣ 기존 S3 파일 삭제
        if (oldFile != null && !oldFile.isBlank()) {
            s3Service.delete(s3Dir, oldFile);
        }

        // ✅ 여기만 바뀜 (로컬 → S3)
        s3Service.upload(file, s3Dir, savedFileName);
    }

    public void setImages(Product product, MultipartFile file, int idx) {

        if (file == null || file.isEmpty()) {
            return;
        }

        String originalFilename = file.getOriginalFilename();
        String savedFileName = UUID.randomUUID() + "_" + originalFilename;

        String s3Dir;

        switch (idx) {
            case PRODUCT_IMAGES:
                s3Dir = "productImages";
                product.setImageUrl(savedFileName);
                product.setImageUrlName(originalFilename);
                break;

            case PRODUCT_INFORMATION:
                s3Dir = "productInformation";
                product.setProductInformationImage(savedFileName);
                break;

            case PRODUCT_DESCRIPTION:
                s3Dir = "productDescription";
                product.setProductDescriptionImage(savedFileName);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + idx);
        }

        // ✅ 여기만 바뀜 (로컬 → S3)
        s3Service.upload(file, s3Dir, savedFileName);
    }


    @Override
    public boolean updateCount(List<KakaoPay.ProductInfo> productInfo) {
        boolean result = false;
        List<Integer> row = new ArrayList<>();
        productInfo.forEach(info
                -> row.add(productRepository.decreaseCount(info.getPid(), info.getQty())));

        if(!row.isEmpty()){
            result = true;
        }
        return result;
    }
}