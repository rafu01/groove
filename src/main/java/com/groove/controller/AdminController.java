package com.groove.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.groove.dao.AdminRepository;
import com.groove.dao.CategoryRepository;
import com.groove.dao.OrderRepository;
import com.groove.dao.ShopRepository;
import com.groove.entities.Admin;
import com.groove.entities.Category;
import com.groove.entities.Order;
import com.groove.entities.Shop;
import com.groove.utilities.Message;


@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private OrderRepository orderRepository;
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal){
        String email = principal.getName();
        Admin user = adminRepository.getUserByEmail(email);
        model.addAttribute("title", "admin panel");
        model.addAttribute("user",user);
        return "admindashboard";
    }
    @GetMapping("/add-category")
    public String addCategory(Model model,Principal principal){
        String email = principal.getName();
        Admin user = adminRepository.getUserByEmail(email);
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("title", "add category");
        model.addAttribute("categories", categories);
        model.addAttribute("user",user);
        return "addcategory";
    }
    @PostMapping("/add-category")
    public String saveCategory(@RequestParam("name") String name, Model model,Principal principal, HttpSession session){
        String email = principal.getName();
        Admin user = adminRepository.getUserByEmail(email);
        Category category = new Category();
        category.setName(name);
        this.categoryRepository.save(category);
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("title", "add category");
        model.addAttribute("user",user);
        model.addAttribute("categories", categories);
        session.setAttribute("message",new Message("category added: "+name,"notification is-success"));
        return "addcategory";
    }
    @GetMapping("/all-shops")
    public String allShops(Model model, Principal principal){
        String email = principal.getName();
        Admin user = adminRepository.getUserByEmail(email); 
        List<Shop> shops = shopRepository.findAll();
        model.addAttribute("title", "add category");
        model.addAttribute("user",user);
        model.addAttribute("shops", shops);
        return "allshops";
    }
    @GetMapping("/pending-shops")
    public String pendingshops(Model model, Principal principal){
        String email = principal.getName();
        Admin user = adminRepository.getUserByEmail(email); 
        List<Shop> shops = shopRepository.findAll();
        model.addAttribute("title", "shop status");
        model.addAttribute("user",user);
        model.addAttribute("shops", shops);
        return "pendingshops";
    }
    @PostMapping(value=("/update_status/{id}"))
    public RedirectView update_status(Model model, Principal principal, @PathVariable int id,@RequestParam("approved_status") String approved_status){
        String email = principal.getName();
        Admin user = adminRepository.getUserByEmail(email); 
        Shop shop = shopRepository.getReferenceById(id);
        shop.setApproved_status(approved_status.equals("True")?true:false);
        shopRepository.save(shop);
        List<Shop> shops = shopRepository.findAll();
        model.addAttribute("title", "shop status");
        model.addAttribute("user",user);
        model.addAttribute("shops", shops);
        return new RedirectView("/admin/pending-shops");
    }
    @GetMapping("/view-order")
    public String view_orders(Model model, Principal principal){
        String email = principal.getName();
        Admin user = adminRepository.getUserByEmail(email); 
        List<Shop> shops = shopRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        model.addAttribute("title", "orders");
        model.addAttribute("user",user);
        model.addAttribute("orders", orders);
        model.addAttribute("shops", shops);
        return "view_orders";
    }
}
