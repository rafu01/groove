package com.groove.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.groove.entities.ShopOwner;


public interface ShopOwnerRepository extends JpaRepository<ShopOwner,Integer>{
	@Query("select c from ShopOwner c where c.email = :email")
	public ShopOwner getUserByEmail(@Param("email") String email);
	public ShopOwner findByEmail(String email);
}
