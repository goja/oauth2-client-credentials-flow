package com.orryworx.oauth2clientcredentialsflow

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
class OAuth2ServerConfiguration {

    @Configuration
    @EnableAuthorizationServer
    class OAuth2AuthorizationServerConfiguration : AuthorizationServerConfigurerAdapter() {

        override fun configure(oauthServer: AuthorizationServerSecurityConfigurer) {
            oauthServer.allowFormAuthenticationForClients()
        }

        override fun configure(clients: ClientDetailsServiceConfigurer) {
            clients
                    .inMemory()
                    .withClient("client")
                    .secret("{noop}secret")
                    .scopes("default")
                    .authorizedGrantTypes("client_credentials")
                    .accessTokenValiditySeconds(86400) // 24 hours
        }

        @Bean
        fun accessTokenConverter(): JwtAccessTokenConverter = JwtAccessTokenConverter().apply {
            setSigningKey("t0P$3cret")
        }

        @Bean
        fun tokenStore(): TokenStore = JwtTokenStore(accessTokenConverter())

        @Bean
        @Primary
        fun tokenServices(): DefaultTokenServices = DefaultTokenServices().apply {
            setTokenStore(tokenStore())
        }

        override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
            endpoints.tokenStore(tokenStore())
                    .accessTokenConverter(accessTokenConverter())
        }
    }

    @Configuration
    @EnableResourceServer
    class OAuth2ResourceServerConfiguration(private val tokenServices: DefaultTokenServices) : ResourceServerConfigurerAdapter() {

        override fun configure(config: ResourceServerSecurityConfigurer) {
            config.tokenServices(tokenServices)
        }

        override fun configure(http: HttpSecurity) {
            http.cors().disable().csrf().disable().httpBasic().disable()
                    .authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                    .exceptionHandling()
                    .authenticationEntryPoint { request: HttpServletRequest?, response: HttpServletResponse, authException: AuthenticationException? -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED) }
                    .accessDeniedHandler { request: HttpServletRequest?, response: HttpServletResponse, authException: AccessDeniedException? -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED) }
        }
    }
}