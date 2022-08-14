package com.groove.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.groove.dao.CustomerRepository;
import com.groove.dao.ProductsRepository;
import com.groove.dao.ShopRepository;
import com.groove.dao.UserRepository;
import com.groove.entities.Coupon;
import com.groove.entities.Customer;
import com.groove.entities.Product;
import com.groove.entities.Shop;
import com.groove.utilities.Cart;
import com.groove.utilities.Message;
import com.groove.utilities.Pair;

import org.springframework.ui.Model;

@Controller
public class CartController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private ProductsRepository productsRepository;
    @Autowired
    private CustomerRepository customerRepository;
    // @Autowired
    // private CartRepository cartRepository;
    public String get_context_path(String url){
        String[] url_splitted = url.split("/");
        String context = "/";
        for(int i=3;i<url_splitted.length;i++)
            context+=url_splitted[i]+"/";
        return context;
    }
    @GetMapping("/add-to-cart/{id}")
    public RedirectView add_to_cart(@PathVariable int id, Model model, Principal principal, HttpSession session, HttpServletRequest request){
        Cart cart = (Cart) session.getAttribute("cart");
        Product product = productsRepository.getReferenceById(id);
        String context = get_context_path(request.getHeader("referer"));
        if(cart==null){
            cart = new Cart(); 
            List<Pair> pairs = new ArrayList<>();
            Pair pair = new Pair();
            pair.setProduct(product);
            pair.setQuantity(1);
            pairs.add(pair);
            cart.setProducts(pairs);
            // cart.setProducts(prd);
            cart.calculateTotal();
            cart.getQuantity();
            session.setAttribute("cart", cart);
            // cart.setProducts(new ArrayList<Product>());  
        }
        else{
            // cart.getProducts().add(product);
            boolean found = false;
            for (Pair pair : cart.getProducts()) {
                if(pair.getProduct().getId()==product.getId()){
                    pair.setQuantity(pair.getQuantity()+1);
                    found = true;
                    break;
                }
            }
            if(found==false){
                Pair pair = new Pair();
                pair.setProduct(product);
                pair.setQuantity(1);
                cart.getProducts().add(pair);
            }
            cart.calculateTotal();
            cart.getQuantity();
            
        }
        session.setAttribute("cart", cart);
        // cartRepository.save(cart);
        // System.out.println(cart.getProducts().size());
        return new RedirectView(context);
    }
    @GetMapping("/cart")
    public String cart(Model model, Principal principal, HttpSession session){
        Cart cart = (Cart) session.getAttribute("cart");
        if(principal!=null){
            String email = principal.getName();
            Customer customer = customerRepository.getUserByEmail(email);
            model.addAttribute("user", customer);
        }
        model.addAttribute("cart", cart);
        if(cart!=null){
            cart.calculateTotal();
            cart.setTotal_after_discount(cart.getTotal());
            // cart.getTotal_after_discount(null,0);
            // cart.getTotal_after_charges();
            List<Pair> pairs = cart.getProducts();
            // cart.setCoupon(null);
            model.addAttribute("pairs", pairs);
        }
        model.addAttribute("title", "carts");
        return "cart";
    }
    @GetMapping("/delete-from-cart/{id}")
    public RedirectView delete_from_cart(@PathVariable int id,Model model, Principal principal, HttpSession session){
        Cart cart = (Cart) session.getAttribute("cart");
        List<Pair> pairs = cart.getProducts();
        for(int i=0;i<pairs.size();i++){
            if(pairs.get(i).getProduct().getId()==id){
                pairs.get(i).setProduct(null);
                pairs.remove(i);
                // break;
            }
        }
        pairs = cart.getProducts();
        // List<Product> products = new ArrayList<>();
        // for(Pair pair: cart.getProducts()){
        //     products.add(pair.getProduct());
        // }
        cart.calculateTotal();
        cart.setTotal_after_discount(cart.getTotal());
        cart.setCoupon(null);
        model.addAttribute("cart", cart);
        // model.addAttribute("pairs", pairs);
        // model.addAttribute("title", "carts");
        return new RedirectView("/cart");
    }
    @GetMapping(value=("/increment-quantity/{id}"))
	public RedirectView increment_quantity(@PathVariable int id,Model model, Principal principal, HttpSession session){
        Cart cart = (Cart) session.getAttribute("cart");
        for(Pair pair: cart.getProducts()){
            if(pair.getProduct().getId()==id){
                pair.setQuantity(pair.getQuantity()+1);
            }
        }
        cart.calculateTotal();
        cart.setTotal_after_discount(cart.getTotal());
        cart.setCoupon(null);
        session.setAttribute("cart", cart);
        return new RedirectView("/cart");
    }
    @GetMapping(value=("/decrement-quantity/{id}"))
	public RedirectView decrement_quantity(@PathVariable int id,Model model, Principal principal, HttpSession session){
        Cart cart = (Cart) session.getAttribute("cart");
        for(Pair pair: cart.getProducts()){
            if(pair.getProduct().getId()==id){
                if(pair.getQuantity()==1){
                    cart.getProducts().remove(pair);
                    session.setAttribute("cart", cart);
                    return new RedirectView("/cart");
                }
                else
                    pair.setQuantity(pair.getQuantity()-1);
            }
        }
        cart.calculateTotal();
        cart.setTotal_after_discount(cart.getTotal());
        cart.setCoupon(null);
        session.setAttribute("cart", cart);
        return new RedirectView("/cart");
    }
    @PostMapping("/add-coupon")
    public String add_coupon(@RequestParam(required = false) String coupon,Model model, Principal principal, HttpSession session){
        Cart cart = (Cart)session.getAttribute("cart");
        List<Pair> products = cart.getProducts();
        Coupon coupon_obj = new Coupon();
        coupon_obj.setName(coupon);
        cart.setCoupon(coupon_obj);
        // cart.setCoupon(coupon_obj);
        HashMap<Shop,List<Coupon>> available_coupons = new HashMap<Shop,List<Coupon>>();
        for(Pair pair: products){
            Product product = pair.getProduct();
            available_coupons.put(product.getShop(),product.getShop().getCoupons());
        }
        // System.out.println(coupon);
        for(Shop shop:available_coupons.keySet()){
            for(Coupon coup:available_coupons.get(shop)){
                // System.out.println(coup.getName());
                if(coup.getName().equals(coupon)){
                    double price_after_discount = cart.getTotal_after_discount(shop, coup.getPercentage());
                    cart.setTotal_after_discount(price_after_discount);
                    break;
                }
            }
        }
        model.addAttribute("cart", cart);
        model.addAttribute("pairs", products);
        model.addAttribute("title", "carts");
        session.setAttribute("message",new Message("Coupon added successfully","notification is-success"));
        return "cart";
    }
}
