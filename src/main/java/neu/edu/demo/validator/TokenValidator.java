package neu.edu.demo.validator;


import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;


import java.util.Base64;


@Component
public class TokenValidator {

    private static final String ISSUER = "https://accounts.google.com";
    private static final String AUDIENCE = "218875083468-uiimd5tj92mjhmjumtq9954md7v08jun.apps.googleusercontent.com";
    private static final String SUBJECT = "115325788996787597219";

    //private static final int EXPIRATION_IN_SECONDS = 60; // 60s


    private static final String e = "AQAB";
    // 公钥指数部分
    private static final String n = "vfBbH3bcgTzYXomo5hmimATzkEF0QIuhMYmwx0IrpdKT6M15b6KBVhZsPfwbRNoui3iBe8xLON2VHarDgXRzrHec6-oLx8Sh4R4B47MdASURoiIOBiSOiJ3BjKQexNXT4wO0ZLSEMTVt_h24fgIerASU6w2XQOeGb7bbgZnJX3a0NAjsfrxCeG0PacWK2TE2R00mZoeAYWtCuAsE-Xz0hkGqEsg7HqIMYeLjQ-NFkGBErGAi5Cd_k3_D7rv0IEdoB1GkJpIdMLqnI-MR_OxsQNZGpC12OaLXCqgkFAgW69QLAG3YMaTFgPi-Us1i2idc4SPADYijiPml---jCap9yw";
    // 公钥模数部分

    public static boolean verifyToken(String token) {
        try {
            // 构建RSAPublicKeySpec对象
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(
                    new BigInteger(1, Base64.getUrlDecoder().decode(n)),
                    new BigInteger(1, Base64.getUrlDecoder().decode(e))
            );

            // 构建RSA公钥对象
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            //System.out.println("verifyToken keyFactory: " + keyFactory);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            //System.out.println("verifyToken publicKey: " + publicKey);

            // use RS256 algorithm
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);

            // verify JWT by RS256 algorithm
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withAudience(AUDIENCE)
                    .withSubject(SUBJECT)
                    .build();

            DecodedJWT jwt = verifier.verify(token);
            return true;
        } catch (JWTDecodeException | NoSuchAlgorithmException | InvalidKeySpecException | TokenExpiredException e) {
            System.out.println("verifyToken, Exception: " + e);
            return false;
        }
    }
}