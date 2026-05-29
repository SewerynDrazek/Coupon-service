package com.example.coupon.api.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientIpArgumentResolverTest {

    @Mock
    private MethodParameter parameter;
    @Mock
    private NativeWebRequest webRequest;
    @Mock
    private HttpServletRequest httpRequest;
    @Mock
    private ModelAndViewContainer mavContainer;
    @Mock
    private WebDataBinderFactory binderFactory;

    @InjectMocks
    private ClientIpArgumentResolver resolver;

    @Test
    void shouldSupportParameterAnnotatedWithClientIpAndStringType() {
        //given:
        when(parameter.hasParameterAnnotation(ClientIp.class)).thenReturn(true);
        when(parameter.getParameterType()).thenAnswer(inv -> String.class);

        //when:
        //then:
        assertThat(resolver.supportsParameter(parameter)).isTrue();
    }

    @Test
    void shouldNotSupportParameterWithoutClientIpAnnotation() {
        //given:
        when(parameter.hasParameterAnnotation(ClientIp.class)).thenReturn(false);

        //when:
        //then:
        assertThat(resolver.supportsParameter(parameter)).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotSupportParameterWithClientIpAnnotationButNonStringType() {
        //given:
        when(parameter.hasParameterAnnotation(ClientIp.class)).thenReturn(true);
        when(parameter.getParameterType()).thenAnswer(inv -> Integer.class);

        //when:
        //then:
        assertThat(resolver.supportsParameter(parameter)).isFalse();
    }

    @Test
    void shouldReturnNullWhenHttpServletRequestIsUnavailable() {
        //given:
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(null);

        //when:
        //then:
        assertThat(resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).isNull();
    }

    @Test
    void shouldReturnXRealIpHeader() {
        //given:
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpRequest);
        when(httpRequest.getHeader("X-Real-IP")).thenReturn("1.2.3.4");

        //when:
        //then:
        assertThat(resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).isEqualTo("1.2.3.4");
    }

    @Test
    void shouldTrimXRealIpHeader() {
        //given:
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpRequest);
        when(httpRequest.getHeader("X-Real-IP")).thenReturn("  1.2.3.4  ");

        //when:
        //then:
        assertThat(resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).isEqualTo("1.2.3.4");
    }

    @Test
    void shouldFallBackToXForwardedForWhenXRealIpIsBlank() {
        //given:
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpRequest);
        when(httpRequest.getHeader("X-Real-IP")).thenReturn("   ");
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("5.6.7.8");

        //when:
        //then:
        assertThat(resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).isEqualTo("5.6.7.8");
    }

    @Test
    void shouldReturnLastIpFromXForwardedForChain() {
        //given:
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpRequest);
        when(httpRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 10.0.0.2, 5.6.7.8");

        //when:
        //then:
        assertThat(resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).isEqualTo("10.0.0.1");
    }

    @Test
    void shouldTrimLastIpFromXForwardedForChain() {
        //given:
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpRequest);
        when(httpRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1,  5.6.7.8  ");

        //when:
        //then:
        assertThat(resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).isEqualTo("10.0.0.1");
    }

    @Test
    void shouldFallBackToRemoteAddrWhenBothHeadersAbsent() {
        //given:
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpRequest);
        when(httpRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpRequest.getRemoteAddr()).thenReturn("9.9.9.9");

        //when:
        //then:
        assertThat(resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).isEqualTo("9.9.9.9");
    }

    @Test
    void shouldFallBackToRemoteAddrWhenXForwardedForIsBlank() {
        //given:
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpRequest);
        when(httpRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("  ");
        when(httpRequest.getRemoteAddr()).thenReturn("9.9.9.9");

        //when:
        //then:
        assertThat(resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)).isEqualTo("9.9.9.9");
    }
}
