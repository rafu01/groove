package com.groove.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import com.groove.dao.CategoryRepository;
import com.groove.dao.ProductsRepository;
import com.groove.dao.ShopOwnerRepository;
import com.groove.dao.ShopRepository;
import com.groove.dao.UserRepository;
import com.groove.entities.Category;
import com.groove.entities.Coupon;
import com.groove.entities.Order;
import com.groove.entities.Product;
import com.groove.entities.Shop;
import com.groove.entities.ShopOwner;
import com.groove.entities.User;
import com.groove.utilities.Message;

@Controller
@RequestMapping("/shop")
public class ShopOwnerController {
    @Autowired
	private ShopOwnerRepository shopOwnerRepository;
    @Autowired
	private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private ProductsRepository productsRepository;
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal){
        String email = principal.getName();
        ShopOwner user = shopOwnerRepository.getUserByEmail(email);

        model.addAttribute("title", "dashboard");
        model.addAttribute("user", user);
        model.addAttribute("products", user.getShop().getProducts());
		return "shopownerdashboard";
    }
    @GetMapping("/add-product")
	public String addProduct(Model model, Principal principal){
        String email = principal.getName();
        ShopOwner user = shopOwnerRepository.getUserByEmail(email);
        List<Category> catgeories = categoryRepository.findAll();
        model.addAttribute("title", "add product");
        model.addAttribute("user", user);
        model.addAttribute("categories", catgeories);
		return "addproduct";
	}
    @GetMapping(value = ("/{id}"))
    public String shop(Model model, Principal principal, @PathVariable int id){
        Shop shop = shopRepository.getReferenceById(id);
        model.addAttribute("shop", shop);
        try{
            String email = principal.getName();
            User user = userRepository.findByEmail(email);
            model.addAttribute("user", user);
        }
        catch(Exception e){
            
        }
        model.addAttribute("title", shop.getName());
        return "singleshop";
    }
    @GetMapping("/add-items")
    public String add_items(Model model, Principal principal){
        String email = principal.getName();
        ShopOwner user = shopOwnerRepository.getUserByEmail(email);
        Shop shop = user.getShop();
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("shop", shop);
        model.addAttribute("categories", categories);
        model.addAttribute("user", user);
        model.addAttribute("title", "add items");
        
        return "add_items";
    }
    @PostMapping(value=("/add-items"))
    public RedirectView save_add_items(Model model, Principal principal, HttpSession session,
    @RequestParam("name") String name, @RequestParam("description") String description,
    @RequestParam("price") Integer price, @RequestParam("quantity") Integer quantity,
    @RequestParam("category") String category,
    @RequestParam("file") MultipartFile file) throws IOException{

        String email = principal.getName();
        ShopOwner user = shopOwnerRepository.getUserByEmail(email);
        Shop shop = user.getShop();
        // List<Product> current_products = shop.getProducts();
        // if(current_products.isEmpty()){
        //     current_products = new ArrayList<Product>();
        // }
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setShop(shop);
        List<Category> cat = categoryRepository.findAll();
        for (Category category2 : cat) {
            if(category2.getName().equals(category)){
                product.setCategory(category2);
            }
        }
        if(file!=null) {
			byte[] image = java.util.Base64.getEncoder().encode(file.getBytes());
			product.setImage(image);
		}
        
        productsRepository.save(product);
        shop.getProducts().add(product);
        // shop.setProducts(current_products);
        // productsRepository.save(product);
        shopRepository.save(shop);
        session.setAttribute("message",new Message("product added successfully","notification is-success"));
        return new RedirectView("/shop/dashboard");
    }
    @GetMapping("/product/edit/{id}")
    public String edit_items(Model model, Principal principal, @PathVariable int id){
        String email = principal.getName();
        ShopOwner user = shopOwnerRepository.getUserByEmail(email);
        Product product = productsRepository.getReferenceById(id);
        model.addAttribute("user", user);
        model.addAttribute("product", product);
        model.addAttribute("title", "edit items");
        return "edititem";
    }
    @PostMapping(value=("/product/save-items/{id}"))
    public RedirectView save_edit_items(@PathVariable int id,Model model, Principal principal,
    @RequestParam("name") String name, @RequestParam("description") String description,
    @RequestParam("price") Integer price, @RequestParam("quantity") Integer quantity,
    @RequestParam("file") MultipartFile file, HttpSession session) throws IOException{

        // String email = principal.getName();
        // ShopOwner user = shopOwnerRepository.getUserByEmail(email);
        Product product = productsRepository.getReferenceById(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        if(file!=null) {
			byte[] image = java.util.Base64.getEncoder().encode(file.getBytes());
			product.setImage(image);
		}
        productsRepository.save(product);
        session.setAttribute("message",new Message("product edited successfully","notification is-success"));
        return new RedirectView("/shop/dashboard");
    }
    @GetMapping("/product/delete/{id}")
    public RedirectView delete_items(Model model, Principal principal, @PathVariable int id, HttpSession session){
        String email = principal.getName();
        ShopOwner user = shopOwnerRepository.getUserByEmail(email);
        Product product = productsRepository.getReferenceById(id);
        Shop shop = user.getShop();
        List<Product> products = shop.getProducts();
        for(int i=0;i<products.size();i++){
            if(products.get(i).getId()==product.getId()){
                shop.getProducts().set(i, null);
                break;
            }
        }
        product.setShop(null);
        product.setCategory(null);
        this.productsRepository.delete(product);
        model.addAttribute("user", user);
        model.addAttribute("product", product);
        model.addAttribute("title", "edit items");
        session.setAttribute("message",new Message("product deleted successfully","notification is-danger"));
        return new RedirectView("/shop/dashboard");
    }
    @GetMapping("/edit")
    public String edit_shop(Model model, Principal principal){
        String email = principal.getName();
        ShopOwner user = shopOwnerRepository.getUserByEmail(email);
        Shop shop = user.getShop();
        model.addAttribute("user", user);
        model.addAttribute("shop", shop);
        model.addAttribute("title", "edit shop");
        return "editshop";
    }
    @PostMapping(value=("/save-shop"))
    public RedirectView save_shop(Model model, Principal principal, @RequestParam String name, @RequestParam String description,
    HttpSession session){
        String email = principal.getName();
        ShopOwner user = shopOwnerRepository.getUserByEmail(email);
        Shop shop = user.getShop();
        shop.setName(name);
        shop.setDescription(description);
        shopRepository.save(shop);
        session.setAttribute("message",new Message("shop info updated successfully","notification is-success"));
        return new RedirectView("/shop/dashboard");
    }
    @GetMapping("/orders")
    public String view_order(Model model, Principal principal){
        String email = principal.getName();
        ShopOwner user = shopOwnerRepository.getUserByEmail(email);
        Shop shop = user.getShop();

        List<Order> orders = shop.getOrders();
        // Order o = new Order();
        // o.setName("customer#1");
        // o.setQuantity(5);
        // o.setTotal(500);
        // o.setProducts(prod);
        // List<Order> orders = shop.getOrders();
        // orders.add(o);
        // orders.add(o);
        model.addAttribute("orders", orders);
        System.out.println(orders.get(0).getTotal());
        // System.out.println(orders);
        model.addAttribute("user", user);
        // model.addAttribute("orders", orders);
        model.addAttribute("title", "view orders");
        return "view_orders";
    }
    @GetMapping("/add-coupon")
    public String add_coupon(Model model, Principal principal){
        String email = principal.getName();
        ShopOwner user = shopOwnerRepository.getUserByEmail(email);
        Shop shop = user.getShop();
        List<Coupon> coupons = shop.getCoupons();
        model.addAttribute("user", user);
        model.addAttribute("coupons", coupons);
        model.addAttribute("title", "add coupon");
        return "add_coupon";
    }
    @PostMapping("/add-coupon")
    public RedirectView save_coupon(@ModelAttribute Coupon coupon, Principal principal, HttpSession session){
        String email = principal.getName();
        ShopOwner user = shopOwnerRepository.getUserByEmail(email);
        Shop shop = user.getShop();
        shop.getCoupons().add(coupon);
        shopRepository.save(shop);
        session.setAttribute("message",new Message("Coupon added: "+coupon.getName(),"notification is-success"));
        return new RedirectView("/shop/add-coupon");
    }
}
