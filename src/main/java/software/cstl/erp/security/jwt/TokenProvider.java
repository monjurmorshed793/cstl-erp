package software.cstl.erp.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import io.jsonwebtoken.io.Decoders;

@Component
@Slf4j
public class TokenProvider {
    private static final String AUTHORITIES_KEY = "auth";

    private Key key;

    @Value("cstl.security.authentication.jwt.token-validity-in-seconds")
    private long tokenValidityInMilliseconds;

    @Value("cstl.security.authentication.jwt.token-validity-in-seconds-for-remember-me")
    private long tokenValidityInMillisecondsForRememberMe;

    @Value("cstl.jwt.secret'")
    private String secret;

    @Value("cstl.security.authentication.jwt.base64-secret")
    private String base64Secret;

    public TokenProvider() {
    }

    @PostConstruct
    public void init(){
        byte[] keyBytes;
/*        if(!StringUtils.isEmpty(secret)){
            log.warn("Warning: the JWT key used is not BASE64-encoded");
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }else{
            log.debug("Using a Base64-encoded JWT secret key");
            keyBytes = Decoders.BASE64.decode(base64Secret);
        }*/
        log.debug("Using a Base64-encoded JWT secret key");
        keyBytes = Decoders.BASE64.decode(base64Secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        tokenValidityInMilliseconds = 1000 * tokenValidityInMilliseconds;
        tokenValidityInMillisecondsForRememberMe = 1000 * tokenValidityInMillisecondsForRememberMe;
    }

    public String createToken(Authentication authentication, boolean rememberMe){
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        long now = (new Date()).getTime();
        Date validity;
        if(rememberMe){
            validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
        }else{
            validity = new Date(now + this.tokenValidityInMilliseconds);
        }
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(SignatureAlgorithm.HS512, key)
                .setExpiration(validity)
                .compact();
    }

    public Authentication getAuthentication(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String authToken){
        try{
            Jwts.parser().setSigningKey(key).parseClaimsJws(authToken);
            return true;
        }catch (JwtException | IllegalArgumentException e){
            log.info("Invalid JWT token");
            log.trace("Invalid JWT Token trace.", e);
        }
        return false;
    }
}
