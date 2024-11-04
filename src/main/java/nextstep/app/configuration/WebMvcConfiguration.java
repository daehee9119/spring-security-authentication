package nextstep.app.configuration;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import nextstep.app.domain.Member;
import nextstep.app.domain.MemberRepository;
import nextstep.app.ui.AuthenticationException;
import nextstep.security.configuration.interceptor.BasicAuthenticationInterceptor;
import nextstep.security.configuration.interceptor.FormLoginInterceptor;
import nextstep.security.model.UserDetails;
import nextstep.security.service.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final MemberRepository memberRepository;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(formLoginInterceptor()).addPathPatterns("/login");
        registry.addInterceptor(basicAuthenticationInterceptor()).addPathPatterns("/members");
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public Optional<UserDetails> loadUserByUsername(String username) {
                Member member = memberRepository.findByEmail(username).orElseThrow(AuthenticationException::new);
                return Optional.ofNullable(
                        UserDetails.builder().userName(member.getEmail()).password(member.getPassword()).build());
            }

            @Override
            public Optional<UserDetails> loadUserByUsernameAndEmail(String username, String password) {
                Member member =
                        memberRepository.findByEmailAndPassword(username, password)
                                .orElseThrow(AuthenticationException::new);
                return Optional.ofNullable(
                        UserDetails.builder().userName(member.getEmail()).password(member.getPassword()).build());
            }
        };
    }

    @Bean
    public FormLoginInterceptor formLoginInterceptor() {
        return new FormLoginInterceptor(userDetailsService());
    }

    @Bean
    public BasicAuthenticationInterceptor basicAuthenticationInterceptor() {
        return new BasicAuthenticationInterceptor(userDetailsService());
    }
}
