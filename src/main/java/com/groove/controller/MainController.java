package com.groove.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.groove.dao.AdminRepository;
import com.groove.dao.CategoryRepository;
import com.groove.dao.CustomerRepository;
import com.groove.dao.ProductsRepository;
import com.groove.dao.ShopOwnerRepository;
import com.groove.dao.ShopRepository;
import com.groove.dao.UserRepository;
import com.groove.entities.Admin;
import com.groove.entities.Category;
import com.groove.entities.Customer;
import com.groove.entities.Product;
import com.groove.entities.Shop;
import com.groove.entities.ShopOwner;
import com.groove.entities.User;
import com.groove.utilities.Cart;
import com.groove.utilities.Message;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Controller
public class MainController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private ProductsRepository productsRepository;
	@Autowired
	private ShopOwnerRepository shopownerRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired 
	private ShopRepository shopRepository;
	@Autowired
	private AdminRepository adminRepository;
	@GetMapping("/")
	public String home(Model model, Principal principal, HttpSession session) {
		model.addAttribute("title", "groove");
		try{
			String email = principal.getName();
			User user = userRepository.getUserByEmail(email);
			model.addAttribute("user", user);
		}
		catch(Exception e){

		}
		Object user = isLogged(principal);
		List<Category> categories = categoryRepository.findAll();
		model.addAttribute("categories", categories);
		model.addAttribute("user", user);
		Cart cart =(Cart) session.getAttribute("cart");
		List<Product> products;
		products = productsRepository.findAll();
		model.addAttribute("cart", cart);
		model.addAttribute("products", products);
		List<Product> featured = new ArrayList<>();
		for(Product product: products){
			if(product.getId()==6 ){
				featured.add(product);
			}
			if(product.getId()==18 ){
				featured.add(product);
			}
			if(product.getId()==22 ){
				featured.add(product);
			}
			if(product.getId()==23 ){
				featured.add(product);
			}
		}
		model.addAttribute("featured", featured);




		model.addAttribute("title", "groove");
		// Customer us = new Customer();
		// System.out.print(us.getId());
		// customerRepository.save(us);
		return "home";
	}
	public Object isLogged(Principal principal){
		try{
			String email = principal.getName();
			User user = userRepository.getUserByEmail(email);
			return user;
		}
		catch(Exception e){
			// System.out.println(e);
			return null;
		}
	}
	@GetMapping("/login")
	public String login(Model model, Principal principal){
		if (principal!=null) {
			return "redirect:/";
		}
		model.addAttribute("title","login");
		return "login";
		// if(isLogged(principal)==null){
		// 	model.addAttribute("title", "login");
		// 	return new RedirectView("login");
		// }
		// else{
		// 	String email = principal.getName();
        // 	Customer customer = customerRepository.getUserByEmail(email);
        // 	System.out.println(customer.getName());
        // 	model.addAttribute("title", "dashboard");
        // 	model.addAttribute("customer", customer);
		// 	return new RedirectView("dashboard");
		// }
	}
	
	@GetMapping("/login-error")
	public String login_fail(Model model,HttpSession session){
		model.addAttribute("title","login");
		session.setAttribute("message",new Message("email/password incorrect","notification is-danger"));
		return "login";
	}
	@GetMapping(value = ("/signup/{type}"))
	public String signup(@PathVariable String type, Model model){
		model.addAttribute("title", "signup");
		model.addAttribute("type", type);
		return "signup";
	}
	@RequestMapping(value = ("/signup/{type}"), method=RequestMethod.POST)
	private RedirectView ProcessSignup(@RequestParam("fullname") String fullname, @RequestParam("email") String email,
			@RequestParam("password") String password,@PathVariable String type, Model model,
			HttpSession session) {
		User user;
		if(type.equals("ROLE_SHOP")){
			user = new ShopOwner();
		}
		else if(type.equals("ROLE_CUSTOMER")){
			user = new Customer();
		}
		else{
			user = new Admin();
		}
		user.setName(fullname);
		user.setEmail(email);
		try {
			if(customerRepository.getUserByEmail(email)!=null) {
				throw new Exception("user email already exists");
			}
			if(password.length()<6) {
				throw new Exception("password length must be at least 6");
			}
			user.setPassword(passwordEncoder.encode(password));
			user.setRole(type);
			if(type.equals("ROLE_SHOP")){
				Shop shop = new Shop();
				ShopOwner shopowner = (ShopOwner)user;
				shopowner.setShop(shop);
				this.shopownerRepository.save(shopowner);
			}
			else if(type.equals("ROLE_CUSTOMER")){
				this.customerRepository.save((Customer)user);
			} 
			else{
				this.adminRepository.save((Admin)user);
			}
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("Successfully registered! ","notification is-success"));
			return new RedirectView("/login");
		}
		catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new Message(e.getMessage(),"notification is-danger"));
			return new RedirectView("/signup/"+type);
		}
	}
	// @GetMapping("/admin-signup")
    // public String admin_signup(Model model){
    //     model.addAttribute("title", "signup");
	// 	model.addAttribute("type","ROLE_ADMIN");
	// 	return "signup";
    // }
	// @PostMapping("/admin-signup")
    // public RedirectView admin_signup(@RequestParam("fullname") String fullname, @RequestParam("email") String email,
	// 		@RequestParam("password") String password,@PathVariable String type, Model model,
	// 		HttpSession session) {
	// 	Admin user = new Admin();
	// 	user.setName(fullname);
	// 	user.setEmail(email);
	// 	user.setRole("ROLE_ADMIN");
	// 	try {
	// 		if(customerRepository.getUserByEmail(email)!=null) {
	// 			throw new Exception("user email already exists");
	// 		}
	// 		if(password.length()<6) {
	// 			throw new Exception("password length must be at least 6");
	// 		}
	// 		user.setPassword(passwordEncoder.encode(password));
	// 		adminRepository.save(user);
	// 		model.addAttribute("user",user);
	// 		session.setAttribute("message",new Message("Successfully registered! ","notification is-success"));
	// 		return new RedirectView("/login");
	// 	}
	// 	catch(Exception e) {
	// 		e.printStackTrace();
	// 		model.addAttribute("user",user);
	// 		session.setAttribute("message",new Message(e.getMessage(),"notification is-danger"));
	// 		return new RedirectView("/admin-signup/"+type);
	// 	}
    // }
	@GetMapping("/selectsignup")
	public String selectSignup(Model model){
		model.addAttribute("title", "Select Option");
		return "selectsignup";
	}
	@GetMapping("/products")
	public String products(Model model, Principal principal, HttpSession session){
		// Product p = createProd();
		// productsRepository.save(p);
		List<Product> allProduct = productsRepository.findAll();
		// Product x = productsRepository.getReferenceById(36);
		// x.setName(id);
		Object user = isLogged(principal);
		List<Category> categories = categoryRepository.findAll();
		model.addAttribute("categories", categories);
		model.addAttribute("user", user);
		model.addAttribute("title", "products");
		model.addAttribute("products", allProduct);
		// System.out.println(allProduct);
		Cart cart =(Cart) session.getAttribute("cart");
		model.addAttribute("cart",cart);
		return "products";
	}
	@PostMapping(value=("/products"))
	public String search_products(Model model, Principal principal, @RequestParam(required = false) String name,
	@RequestParam(required = false) String sort, @RequestParam(required = false) String search_type, @RequestParam(required = false) String category){
		List<Product> allProduct = productsRepository.findAll();
		Object user = isLogged(principal);
		if(search_type.equals("Search Product")){
			List<Product> query_product = new ArrayList<Product>();
			if(name.equals("") && category.equals("")){
				for (Product product : allProduct) {
					if(product.getName().contains(name)){
						if(product.getCategory().getName().equals(category)) {
							query_product.add(product);
						}
					}
				}
			}
			else if(!name.equals("")){
				for (Product product : allProduct) {
					if(product.getName().contains(name)){
						query_product.add(product);
					}
				}
			}
			else if(!category.equals("")){
				for (Product product : allProduct) {
					if(product.getCategory().getName().equals(category)) {
						query_product.add(product);
					}
				}
			}
			model.addAttribute("products", query_product);
		}
		else{
			List<Shop> allshops =shopRepository.findAll();
			List<Shop> query_shop = new ArrayList<Shop>();
			for(Shop shop:allshops){
				if(shop.getName().contains(name)){
					query_shop.add(shop);
				}
			}
			model.addAttribute("shops", query_shop);
		}
		List<Category> categories = categoryRepository.findAll();
		model.addAttribute("categories", categories);
		model.addAttribute("user", user);
		model.addAttribute("title", "search");
		// System.out.println(allProduct);
		return "products";
	}
	public Product createProd(){
		Product p = new Product();
		// p.setName("shoe#"+4);
		// p.setPrice("300");
		// p.setQuantity("10");
		return p;
	}
	@GetMapping(value = ("/product/{id}"))
	public String singleProduct(@PathVariable int id, Model model, Principal principal, HttpSession session){
		Product product = productsRepository.getReferenceById(id);
		Object customer = isLogged(principal);
		Cart cart = (Cart) session.getAttribute("cart");
		model.addAttribute("cart", cart);
		model.addAttribute("customer", customer);
		model.addAttribute("product",product);
		return "singleproduct";
	}
	@GetMapping("/shops")
	public String shops(Model model, Principal principal, HttpSession session){
		List<Shop> allShop = shopRepository.findAll();
		Object user = isLogged(principal);
		model.addAttribute("user", user);
		model.addAttribute("title", "Shops");
		model.addAttribute("shops", allShop);
		Cart cart =(Cart) session.getAttribute("cart");
		model.addAttribute("cart",cart);
		return "shops";
	}
	@GetMapping(value = ("/shops/{id}"))
	public String singleShop(@PathVariable int id, Model model, Principal principal, HttpSession session){
		Shop shop = shopRepository.getReferenceById(id);
		List<Product> allProduct = productsRepository.findAll();
		Object customer = isLogged(principal);
		Cart cart = (Cart) session.getAttribute("cart");
		model.addAttribute("cart", cart);
		model.addAttribute("customer", customer);
		model.addAttribute("shop",shop);
		model.addAttribute("products",allProduct);
		return "singleshop";
	}
	@GetMapping(value = ("/phones"))
	public String phones(Model model, Principal principal, HttpSession session){
		List<Product> allProduct = productsRepository.findAll();
		Object customer = isLogged(principal);
		Cart cart = (Cart) session.getAttribute("cart");
		model.addAttribute("cart", cart);
		model.addAttribute("title", "Phones");
		model.addAttribute("customer", customer);
		model.addAttribute("products",allProduct);
		return "phones";
	}
	@GetMapping(value = ("/laptops"))
	public String laptops(Model model, Principal principal, HttpSession session){
		List<Product> allProduct = productsRepository.findAll();
		Object customer = isLogged(principal);
		Cart cart = (Cart) session.getAttribute("cart");
		model.addAttribute("cart", cart);
		model.addAttribute("title", "Laptops");
		model.addAttribute("customer", customer);
		model.addAttribute("products",allProduct);
		return "laptops";
	}
	@GetMapping(value = ("/tablets"))
	public String tablets(Model model, Principal principal, HttpSession session){
		List<Product> allProduct = productsRepository.findAll();
		Object customer = isLogged(principal);
		Cart cart = (Cart) session.getAttribute("cart");
		model.addAttribute("cart", cart);
		model.addAttribute("title", "Tablets");
		model.addAttribute("customer", customer);
		model.addAttribute("products",allProduct);
		return "tablets";
	}
	@GetMapping(value = ("/accessories"))
	public String accessories(Model model, Principal principal, HttpSession session){
		List<Product> allProduct = productsRepository.findAll();
		Object customer = isLogged(principal);
		Cart cart = (Cart) session.getAttribute("cart");
		model.addAttribute("cart", cart);
		model.addAttribute("title", "Accessories");
		model.addAttribute("customer", customer);
		model.addAttribute("products",allProduct);
		return "accessories";
	}
}
