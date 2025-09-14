package com.dm.dmbackend.domain.account.member.repository;

import com.dm.dmbackend.domain.account.member.entity.SocialAccount;
import com.dm.dmbackend.domain.account.oauth2.provider.OAuth2Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderUserId(OAuth2Provider provider, String providerUserId);
    boolean existsByProviderAndProviderUserId(OAuth2Provider provider, String providerUserId);
}
