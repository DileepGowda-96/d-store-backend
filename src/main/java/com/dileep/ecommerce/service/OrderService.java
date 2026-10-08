package com.dileep.ecommerce.service;

import com.dileep.ecommerce.dto.OrderItemResponse;
import com.dileep.ecommerce.dto.OrderResponse;
import com.dileep.ecommerce.entity.Cart;
import com.dileep.ecommerce.entity.CartItem;
import com.dileep.ecommerce.entity.Order;
import com.dileep.ecommerce.entity.OrderItem;
import com.dileep.ecommerce.entity.OrderStatus;
import com.dileep.ecommerce.entity.Product;
import com.dileep.ecommerce.entity.User;
import com.dileep.ecommerce.exception.ResourceNotFoundException;
import com.dileep.ecommerce.repository.CartRepository;
import com.dileep.ecommerce.repository.OrderRepository;
import com.dileep.ecommerce.repository.ProductRepository;
import com.dileep.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse placeOrder(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found"
                        ));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PLACED)
                .orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() >
                    product.getStockQuantity()) {

                throw new RuntimeException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();

            order.getItems().add(orderItem);

            BigDecimal subtotal =
                    product.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            cartItem.getQuantity()
                                    )
                            );

            totalAmount = totalAmount.add(subtotal);

            product.setStockQuantity(
                    product.getStockQuantity()
                            - cartItem.getQuantity()
            );

            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return convertToResponse(savedOrder);
    }

    public List<OrderResponse> getUserOrders(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found"
            );
        }

        return orderRepository
                .findByUserId(userId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found"
                        ));

        return convertToResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(
            Long userId,
            Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found"
                        ));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "You can only cancel your own order"
            );
        }

        if (order.getStatus() != OrderStatus.PLACED &&
                order.getStatus() != OrderStatus.CONFIRMED) {

            throw new RuntimeException(
                    "Order cannot be cancelled at this stage"
            );
        }

        for (OrderItem orderItem : order.getItems()) {

            Product product = orderItem.getProduct();

            product.setStockQuantity(
                    product.getStockQuantity()
                            + orderItem.getQuantity()
            );

            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);

        return convertToResponse(
                orderRepository.save(order)
        );
    }

    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found"
                        ));

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == OrderStatus.CANCELLED ||
                currentStatus == OrderStatus.DELIVERED) {

            throw new RuntimeException(
                    "Order status cannot be changed"
            );
        }

        if (currentStatus == OrderStatus.PLACED &&
                newStatus != OrderStatus.CONFIRMED) {

            throw new RuntimeException(
                    "PLACED order can only be CONFIRMED"
            );
        }

        if (currentStatus == OrderStatus.CONFIRMED &&
                newStatus != OrderStatus.SHIPPED) {

            throw new RuntimeException(
                    "CONFIRMED order can only be SHIPPED"
            );
        }

        if (currentStatus == OrderStatus.SHIPPED &&
                newStatus != OrderStatus.DELIVERED) {

            throw new RuntimeException(
                    "SHIPPED order can only be DELIVERED"
            );
        }

        order.setStatus(newStatus);

        return convertToResponse(
                orderRepository.save(order)
        );
    }

    private OrderResponse convertToResponse(Order order) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(this::convertItemToResponse)
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderDate(),
                items
        );
    }

    private OrderItemResponse convertItemToResponse(
            OrderItem orderItem) {

        Product product = orderItem.getProduct();

        return new OrderItemResponse(
                orderItem.getId(),
                product.getId(),
                product.getName(),
                orderItem.getQuantity(),
                orderItem.getPrice(),
                orderItem.getSubtotal()
        );
    }
}