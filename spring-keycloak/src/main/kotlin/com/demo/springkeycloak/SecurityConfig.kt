package com.demo.springkeycloak

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val keyCloakLogoutHandler: KeycloakLogoutHandler,
) {

    @Bean
    fun sessionAuthenticationStrategy(): SessionAuthenticationStrategy {
        return RegisterSessionAuthenticationStrategy(SessionRegistryImpl())
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests()
            .anyRequest().authenticated()
        http.oauth2Login()
            .and()
            .logout()
            .addLogoutHandler(keyCloakLogoutHandler)
            .logoutSuccessUrl("/")
        return http.build()
    }
}

@Component
class KeycloakLogoutHandler : LogoutHandler {
    private val logger = LoggerFactory.getLogger(KeycloakLogoutHandler::class.java)
    lateinit var restTemplate: RestTemplate

    override fun logout(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        logoutFromKeyCloak(authentication?.principal as OidcUser)
    }

    private fun logoutFromKeyCloak(user: OidcUser) {
        val endPoint = "${user.issuer}/protocol/openid-connect/logout"
        val builder = UriComponentsBuilder.fromUriString(endPoint)
            .queryParam("id_token_hint", user.idToken.tokenValue)

        val response = restTemplate.getForEntity(builder.toUriString(), String::class.java)
        if (response.statusCode.is2xxSuccessful) {
            logger.info("Success Logout")
        } else {
            logger.error("Fail to Logout")
        }
    }
}
