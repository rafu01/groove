package com.groove.utilities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.groove.entities.Product;

// @Entity
// @Table(name = "pair")
public class Pair  {
    // @Id
	// @GeneratedValue(strategy = GenerationType.AUTO)
	// @Column(name = "id")
	// private int id;
    private Product product;
    private int qty;
    // public int getId() {
	// 	return id;
	// }
    // public void setId(int id) {
	// 	this.id = id;
	// }
    public Product getProduct(){
        return product;
    }
    public void setProduct(Product product){
        this.product = product;
    }
    public int getQuantity(){
        return qty;
    }
    public void setQuantity(int qty){
        this.qty = qty;
    }
}
