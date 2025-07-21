package com.example.HomePage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisterJobRecuiterRepository extends JpaRepository<RegisterRecuiter,Long>
{

    RegisterRecuiter findByUsername(String username);
}

