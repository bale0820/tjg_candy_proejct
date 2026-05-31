package com.tjg_project.candy.domain.order.service;

import com.tjg_project.candy.domain.order.dto.CartDto;
import com.tjg_project.candy.domain.order.entity.Cart;

import java.util.List;

public interface CartService {
    Cart addToCart(Cart cart);
    Cart addToCart(Cart cart, Long userId);
    List<CartDto> cartList(Long upk);
    int updateQty(Cart cart);
    int deleteItem(Long cid);
}
