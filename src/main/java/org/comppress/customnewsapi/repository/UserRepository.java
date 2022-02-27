package org.comppress.customnewsapi.repository;

import org.comppress.customnewsapi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    UserEntity findByUsernameAndDeletedFalse(String username);
    Boolean existsByEmail(String email);
    List<UserEntity> findByDeletedTrue();
    Optional<UserEntity> findByEmailAndDeletedFalse(String email);
    Optional<UserEntity> findByOtpAndEmailAndIsOtpUsedFalse(String otp, String email);

}
