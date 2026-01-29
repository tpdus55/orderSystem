package com.example.orderSystem.product.service;

import com.example.orderSystem.common.auth.JwtTokenFilter;
import com.example.orderSystem.member.domain.Member;
import com.example.orderSystem.member.repository.MemberRepository;
import com.example.orderSystem.product.domain.Product;
import com.example.orderSystem.product.dtos.ProductCreateDto;
import com.example.orderSystem.product.dtos.ProductDetailDto;
import com.example.orderSystem.product.dtos.ProductListDto;
import com.example.orderSystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;
    @Value("${aws.s3.bucket1}")
    private String bucket;

    @Autowired
    public ProductService(ProductRepository productRepository, MemberRepository memberRepository, S3Client s3Client) {
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.s3Client = s3Client;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Product save(ProductCreateDto dto, MultipartFile productImage){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        Product product = dto.toEntity(member,null);


        if(productImage != null && !productImage.isEmpty()){

            String fileName = "product-"+product.getId()+"-" + System.currentTimeMillis() + "-" + productImage.getOriginalFilename();;
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(productImage.getContentType()) //image/jpeg, video/mp4,....
                    .build();

            try {
                s3Client.putObject(request, RequestBody.fromBytes(productImage.getBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String imageUrl = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm();

            product.updateImagePath(imageUrl);
        }else{
            product.updateImagePath(null);
        }
        productRepository.save(product);
        return product;

    }
    @Transactional(readOnly = true)
    public ProductDetailDto productDetail(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("없는 아이디 입니다."));
        ProductDetailDto dto = ProductDetailDto.fromEntity(product);
        return dto;
    }
    @Transactional(readOnly = true)
    public Page<ProductListDto> productList(Pageable pageable){
        Page<Product> productList = productRepository.findAll(pageable);
        return productList.map(p-> ProductListDto.fromEntity(p));
    }
}
