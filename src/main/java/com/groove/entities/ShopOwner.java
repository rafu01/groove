package com.groove.entities;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "shopowner")
public class ShopOwner extends User{
	// @Id
	// @GeneratedValue(strategy = GenerationType.AUTO)
	// @Column(name = "id")
	// private int id;
	// public int getId() {
	// 	return id;
	// }
	// public void setId(int id) {
	// 	this.id = id;
	// }
	@OneToOne(cascade = CascadeType.ALL, fetch =FetchType.LAZY)
	private Shop shop;
	public Shop getShop() {
		return shop;
	}
	public void setShop(Shop shop) {
		this.shop = shop;
	}

}
