package com.groove.controller;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.groove.dao.CustomerRepository;
import com.groove.dao.OrderRepository;
import com.groove.dao.ProductsRepository;
import com.groove.dao.ShopRepository;
import com.groove.dao.UserRepository;
import com.groove.entities.Coupon;
import com.groove.entities.Customer;
import com.groove.entities.Order;
import com.groove.entities.Product;
import com.groove.entities.Shop;
import com.groove.utilities.Cart;
import com.groove.utilities.Message;
import com.groove.utilities.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
	private CustomerRepository customerRepository;
    // @Autowired
	// private BCryptPasswordEncoder passwordEncoder;
	@Autowired 
	private ProductsRepository productsRepository;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private ShopRepository shopRepository;
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal, HttpSession session){
        String email = principal.getName();
        Customer customer = customerRepository.getUserByEmail(email);
		Cart cart = (Cart)session.getAttribute("cart");
		customerRepository.save(customer);
		List<Order> orders = customer.getOrders();
		model.addAttribute("cart", cart);
		System.out.println(orders.size());
		model.addAttribute("orders", orders);
        model.addAttribute("title", "dashboard");
        model.addAttribute("user", customer);
		return "dashboard";
    }
	@GetMapping(value=("/add-favorite/{id}"))
	public RedirectView add_to_favorite(@PathVariable int id, Model model, Principal principal, HttpSession session){
		String email = principal.getName();
		Customer customer =  customerRepository.getUserByEmail(email);
		Product product = productsRepository.getReferenceById(id);
		customer.addFavorite(product);
		Cart cart = (Cart)session.getAttribute("cart");
		customerRepository.save(customer);
		model.addAttribute("cart", cart);
		model.addAttribute("user", customer);
		return new RedirectView("/products");
	}
	@GetMapping("/delete-favorite/{id}")
	public RedirectView view_favorite(@PathVariable int id, Model model, Principal principal,HttpServletRequest request){
		String email = principal.getName();
		Customer customer = customerRepository.getUserByEmail(email);
		List<Product> favorite = customer.getFavorite();
		System.out.println(request.getRequestURI());
		for(Product product: favorite){
			if(product.getId()==id){
				favorite.remove(product);
				break;
			}
		}
		customerRepository.save(customer);
		model.addAttribute("user", customer);
		model.addAttribute("title", "favorite");
		return new RedirectView("/customer/dashboard");
	}
	@GetMapping("/checkout")
	public String checkout(Model model, Principal principal, HttpSession session){
		Customer customer = customerRepository.findByEmail(principal.getName());
		Cart cart = (Cart) session.getAttribute("cart");
		List<Pair> pairs = cart.getProducts();
		cart.getTotal_after_charges();
		model.addAttribute("cart", cart);
		model.addAttribute("pairs", pairs);
		model.addAttribute("title", "checkout");
		model.addAttribute("user",customer);
		return "checkout";
	}
	// @GetMapping("/view-favorite")
	// public String view_favorite(Model model, Principal principal){
	// 	String email = principal.getName();
	// 	Customer customer = customerRepository.getUserByEmail(email);
	// 	List<Product> favorite = customer.getFavorite();
	// 	System.out.println(favorite.size());
	// 	model.addAttribute("user", customer);
	// 	model.addAttribute("favorite", favorite);
	// 	model.addAttribute("title", "favorite");
	// 	return "favorite";
	// }
    // @RequestMapping(path="/signup", method=RequestMethod.POST)
	// private String ProcessSignup(@RequestParam("fullname") String fullname, @RequestParam("email") String email,
	// 		@RequestParam("password") String password,Model model,
	// 		HttpSession session) {
	// 	Customer customer = new Customer();
	// 	customer.setName(fullname);
	// 	customer.setEmail(email);
	// 	customer.setRole("ROLE_ADMIN");
	// 	try {
	// 		if(customerRepository.getUserByEmail(email)!=null) {
	// 			throw new Exception("user email already exists");
	// 		}
	// 		if(password.length()<6) {
	// 			throw new Exception("password length must be at least 6");
	// 		}
	// 		customer.setPassword(passwordEncoder.encode(password));
	// 		this.customerRepository.save(customer);
	// 		model.addAttribute("customer",customer);
	// 		session.setAttribute("message",new Message("Successfully registered! ","notification is-success"));
	// 		return "login";
	// 	}
	// 	catch(Exception e) {
	// 		e.printStackTrace();
	// 		model.addAttribute("customer",customer);
	// 		session.setAttribute("message",new Message(e.getMessage(),"notification is-danger"));
	// 		return "signup";
	// 	}
	// }

	@PostMapping("/confirm-checkout")
	public String confirm_checkout(@RequestParam String name, @RequestParam String address, @RequestParam String number,  Model model, Principal principal, HttpSession session){
		Customer customer = customerRepository.findByEmail(principal.getName());
		Cart cart = (Cart) session.getAttribute("cart");
		Coupon coupon = cart.getCoupon();
		List<Pair> pairs = cart.getProducts();
		Order order = new Order();
		order.setAddress(address);
		order.setName(name);
		order.setNumber(number);
		order.setQuantity(cart.getQuantity());
		order.setTotal(cart.getTotal_after_charges());
		List<Product> products = new ArrayList<Product>();
		List<Shop> shops = shopRepository.findAll();

		HashMap<String, List<Product>> shop_names = new HashMap<String, List<Product>>();
		HashMap<String, Integer> shop_qty = new HashMap<String, Integer>();
		// HashMap<String, > shop_total = new HashMap<String, List<Product>>();
		for(Pair pair: pairs){
			products.add(pair.getProduct());
			for(Shop shop: shops){
				System.out.println(pair.getProduct().getShop());
				if(shop.getName().equals(pair.getProduct().getShop().getName())){
					if(shop_names.get(shop.getName())==null){
						List<Product> prod = new ArrayList<>();
						prod.add(pair.getProduct());
						shop_names.put(shop.getName(),prod);
						shop_qty.put(shop.getName(),pair.getQuantity());
					}
					else{
						shop_names.get(shop.getName()).add(pair.getProduct());
						shop_qty.put(shop.getName(),shop_qty.get(shop.getName())+pair.getQuantity());
					}
				}
			}

			// System.out.println(pair.getProduct().getName());
		}

		for(String shop_name:shop_names.keySet()){
			for(Shop shop:shops){
				if(shop.getName().equals(shop_name)){
					List<Order> orders = shop.getOrders();
					if(orders==null){
						orders = new ArrayList<Order>();
					}
					Order ord = new Order();
					ord.setProducts(shop_names.get(shop_name));
					ord.setQuantity(shop_qty.get(shop_name));
					orders.add(ord);
					int total = 0;
					for(Product produc:shop_names.get(shop_name)){
						total += produc.getPrice();
					}
					if(shop.getCoupons().contains(coupon)){
						total-=total*coupon.getPercentage();
					}
					ord.setName(customer.getName());
					ord.setTotal(total);
					shop.setOrders(orders);
					shopRepository.save(shop);
					break;
				}
			}
		}

		order.setProducts(products);
		List<Order> orders = customer.getOrders();
		if(orders==null)
			orders = new ArrayList<>();
		orders.add(order);
		customer.setOrders(orders);
		customerRepository.save(customer);
		orderRepository.save(order);
		// session.removeAttribute("cart");
		model.addAttribute("cart", cart);
		model.addAttribute("title", "confirmed");
		model.addAttribute("user",customer);
		return "order_confirm";
	}
}
