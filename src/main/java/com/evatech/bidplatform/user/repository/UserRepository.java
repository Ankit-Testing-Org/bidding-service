package com.evatech.bidplatform.user.repository;

import com.evatech.bidplatform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User>  findByUserName(String userName);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    Optional<User> findByBarcodeValue(String barcodeValue);

    Optional<User> findByKeycloakUserId(String keycloakUserId);
}
