package com.tjg_project.candy.domain.order.service;


import com.tjg_project.candy.domain.order.dto.CartDto;
import com.tjg_project.candy.domain.order.dto.UserDto;
import com.tjg_project.candy.domain.order.entity.Cart;
import com.tjg_project.candy.domain.order.repository.CartRepository;
import com.tjg_project.candy.domain.user.entity.Users;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl  implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Override
    @Transactional // 중복 방지 Transaction
    // 장바구니 추가
    public Cart addToCart(Cart cart) {
        return addToCart(cart, cart.getUser().getId());
    }

    @Override
    @Transactional
    public Cart addToCart(Cart cart, Long userId) {
        if (cart.getProduct() == null || cart.getProduct().getId() == null) {
            throw new IllegalArgumentException("Product id is required.");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User id is required.");
        }

        if (cart.getQty() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        Users user = new Users();
        user.setId(userId);
        cart.setUser(user);

        // 상품 id
        Long pid = cart.getProduct().getId();
        // 유저 id
        Long uid = userId;
        // 수량
        int qty = cart.getQty();
        // 장바구니 존재 유무 체크
        Optional<Cart> optionalCart = cartRepository.findByUser_IdAndProduct_Id(uid, pid);
        Cart cartData = null;

        // 장바구니에 이미 존재할 경우
        if(optionalCart.isPresent()){
            // 기존 장바구니 수량 증가
            Cart existingCart = optionalCart.get();
            // 기존 qty + 추가된 qty
            existingCart.setQty(existingCart.getQty() + qty);
            // 기존 장바구니 테이블의 qty값 갱신
            cartData = cartRepository.save(existingCart);
        } else {
            // 없을경우 레코드 추가
            cartData = cartRepository.save(cart);
        }
        return cartData;
    }

    @Override
    public List<CartDto> cartList (Long upk) {
        List<CartDto> cartDtoList = new ArrayList<>();


        List<Cart> cartList =cartRepository.findByUser_Id(upk);

        for(Cart cart : cartList) {
            CartDto cartDto = new CartDto();

            UserDto userDto = new UserDto(
                    cart.getUser().getName(),
                    cart.getUser().getEmail(),
                    cart.getUser().getPhone(),
                    cart.getUser().getAddress()
            );

            cartDto.setProduct(cart.getProduct());
            cartDto.setQty(cart.getQty());
            cartDto.setAddedAt(cart.getAddedAt());
            cartDto.setCid(cart.getCid());
            cartDto.setUser(userDto);
            cartDtoList.add(cartDto);
        }
        return cartDtoList;
    }

    @Override
    @Transactional
    public int updateQty(Cart cart) {
        // 1️⃣ cid로 기존 cart 찾기
        Cart findCart = cartRepository.findById(cart.getCid())
                .orElseThrow(() -> new RuntimeException("Cart not found: " + cart.getCid()));

        // 2️⃣ qty 수정
        findCart.setQty(cart.getQty());

        // 3️⃣ JPA가 더티체킹으로 자동 UPDATE 수행
        // 굳이 save() 호출 안 해도 됨 (하지만 해도 무방)
        // cartRepository.save(findCart);

        return 1; // 성공 시 1 반환 (혹은 void도 OK)
    }

    @Override
    public int deleteItem(Long cid) {
        if (cartRepository.existsById(cid)) {
            cartRepository.deleteById(cid);
            return 1; // 삭제 성공
        } else {
            return 0; // cid 없음
        }
    }
}
