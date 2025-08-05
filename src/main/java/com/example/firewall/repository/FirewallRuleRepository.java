package com.example.firewall.repository;

import com.example.firewall.model.FirewallRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FirewallRuleRepository extends JpaRepository<FirewallRule, Long> {
}
