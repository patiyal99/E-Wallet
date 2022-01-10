package com.example.jbdl.major_project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface WalletRepository extends JpaRepository<Wallet,Integer> {

    @Transactional
    @Modifying
    @Query("update Wallet w set w.balance = w.balance + :amount where w.email = :userEmail")
    void updateWallet(String userEmail,Double amount);

    Wallet findByEmail(String userEmail);
}
