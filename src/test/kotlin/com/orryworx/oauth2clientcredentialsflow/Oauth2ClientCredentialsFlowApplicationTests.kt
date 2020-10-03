package com.orryworx.oauth2clientcredentialsflow

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.ResourceAccessException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Oauth2ClientCredentialsFlowApplicationTests() {

    @BeforeAll
    fun setup() {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        val map = LinkedMultiValueMap<String, String>().apply {
            add("client_id", "client")
            add("client_secret", "secret")
            add("grant_type", "client_credentials")
        }
        val token = restTemplate.postForEntity("/oauth/token", HttpEntity(map, headers), OAuth2AccessToken::class.java)

        assertThat(token.statusCode).isEqualTo(HttpStatus.OK)

        bearer = HttpHeaders().apply {
            setBearerAuth(token.body.toString())
        }
    }

    @Test
    fun `echo`() {
        assertThat(restTemplate.postForEntity("/widget", HttpEntity(widget, bearer), Widget::class.java).body).isEqualTo(widget)
    }

    @Test
    fun `echo without auth`() {
        Assertions.assertThrows(ResourceAccessException::class.java) {
            restTemplate.postForEntity("/widget", HttpEntity(widget), Widget::class.java)
        }
    }

    private val widget = Widget(22, "Coronavirus")

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private lateinit var bearer: HttpHeaders
}
