package com.example.demo.util;

import static com.example.constants.ValidationConstants.ISS;
import static com.example.constants.ValidationConstants.JWK_URl_SUFFIX;
import static com.example.constants.ValidationConstants.NOT_VALID_JSON_WEB_TOKEN;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Base64;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.jwtvalidate.CustomException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

@Component
public class TokenValidationUtil {

	private final int RETRY_COUNT =10;

	public JWTClaimsSet validateAWSJwtToken(String token) throws CustomException {



        String jsonWebKeyFileURL = getJsonWebKeyURL(token);
    	System.out.println("jsonWebKeyFileURL : "+jsonWebKeyFileURL);


        ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
        JWKSource jwkSource = null;
        try {
//			jwkSource = new RemoteJWKSet(new URL(jsonWebKeyFileURL));
        	DefaultResourceRetriever resourceRetriever = new DefaultResourceRetriever(3000, 3000); // 3 sec connect/read timeouts
        	jwkSource = new RemoteJWKSet<>(new URL(jsonWebKeyFileURL), resourceRetriever);

		} catch (MalformedURLException e) {
      	  throw new CustomException(HttpStatus.UNAUTHORIZED, "MalformedURLException:", e.getMessage());
		}
        JWSAlgorithm jwsAlgorithm = JWSAlgorithm.RS256;
        JWSKeySelector keySelector = new JWSVerificationKeySelector(jwsAlgorithm, jwkSource);
        jwtProcessor.setJWSKeySelector(keySelector);

    	System.out.println("jwtProcessor : "+jwtProcessor.toString());

    	int attemptCount =0;

    	while(attemptCount<RETRY_COUNT) {

        	attemptCount++;

        	System.out.println("Ateempt process count  : "+attemptCount);

        	JWTClaimsSet claimsSet = processToken(jwtProcessor,token, attemptCount);
        	if(claimsSet!=null) {
				return claimsSet;
			}

    	}


    	throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "ParseException:", "Internal server error,might happen due to poor net connection");


    }




	public JWTClaimsSet processToken(ConfigurableJWTProcessor jwtProcessor,String token,int attemptCount) throws CustomException {


		 try {

	        	JWTClaimsSet claimsSet = jwtProcessor.process(token, new SimpleSecurityContext());
	            return claimsSet;

	        }catch (BadJWTException e) {
	            System.out.println("BadJWTException: " + e.getMessage());
	      	    throw new CustomException(HttpStatus.UNAUTHORIZED, "BadJWTException:", e.getMessage());
	        }catch(ParseException e) {
	            System.out.println("ParseException: " + e.getMessage());
	              if(attemptCount>RETRY_COUNT) {
					throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "ParseException:", e.getMessage());
				}
	        }catch(JOSEException e) {
	            System.out.println("ParseException: " + e.getMessage());
	              if(attemptCount>RETRY_COUNT) {
					throw new CustomException(HttpStatus.UNAUTHORIZED, "JOSEException:", e.getMessage());
				}
	        } catch (BadJOSEException e) {
				// TODO Auto-generated catch block
	            System.out.println("BadJOSEException: " + e.getMessage());
	            throw new CustomException(HttpStatus.UNAUTHORIZED, "BadJOSEException:", e.getMessage());
			}



		 return null;

	  }




    //  Parse the Jwt token and get the token issuer URL including user pool id.

	 public static String getJsonWebKeyURL(String token) throws CustomException {
	        JsonObject payload = getPayload(token);
	        JsonElement issJsonElement = payload.get(ISS);
	        if (Objects.isNull(issJsonElement)) {
	            throw  new CustomException(HttpStatus.UNAUTHORIZED,NOT_VALID_JSON_WEB_TOKEN, payload.toString());
	        }

	        String issString = issJsonElement.getAsString();
	        String jwkURl = issString + JWK_URl_SUFFIX;
	        return jwkURl;
	    }



	    //Returns payload of a JWT as a JSON object.

	 public static JsonObject getPayload(String jwt) throws CustomException {
	        try {
	            System.out.println("Util- Extracting payload...");
	            validateJWT(jwt);
	            final String payload = jwt.split("\\.")[1];
	            final byte[] payloadBytes =  Base64.getUrlDecoder().decode(payload);
	            final String payloadString = new String(payloadBytes, "UTF-8");
	            JsonParser jsonParser = new JsonParser();
	            JsonObject jsonObject = (JsonObject) jsonParser.parse(payloadString);
	            return jsonObject;
	        } catch ( UnsupportedEncodingException e) {
	            throw new CustomException(HttpStatus.UNAUTHORIZED,NOT_VALID_JSON_WEB_TOKEN, jwt);
	        }
	    }


	  public static JsonObject getHeader(String jwt) throws CustomException {
	        try {
	            System.out.println("Util- Extracting header...");
	            validateJWT(jwt);
	            String header = jwt.split("\\.")[0];
	            final byte [] headerBytes = Base64.getUrlDecoder().decode(header);
	            final String headerString = new String(headerBytes, "UTF-8");
	            JsonParser jsonParser = new JsonParser();
	            JsonObject jsonObject = (JsonObject) jsonParser.parse(headerString);
	            return jsonObject;
	        }catch (UnsupportedEncodingException e){
	            throw new CustomException(HttpStatus.UNAUTHORIZED, NOT_VALID_JSON_WEB_TOKEN, jwt);
	        }


	    }


	    public static String getSignature(String jwt) throws CustomException {
	        try {
	            System.out.println("Util- getting signature...");
	            validateJWT(jwt);
	            final String signature = jwt.split("\\.")[2];
	            final byte[] signatureBytes = Base64.getUrlDecoder().decode(signature);
	            return new String(signatureBytes, "UTF-8");
	        } catch (final Exception e) {
	            throw new CustomException(HttpStatus.UNAUTHORIZED, NOT_VALID_JSON_WEB_TOKEN, jwt);
	        }
	    }





	    //Checks if JWT Token is a valid JSON Web Token.

	    public static void validateJWT(String jwt) throws CustomException {
	        // Check if the the JWT has the three parts
            System.out.println("Util- validateJWT parts");
	        final String[] jwtParts = jwt.split("\\.");
	        if (jwtParts.length != 3) {
	            System.out.println("Util- validateJWT parts");
	            throw new CustomException(HttpStatus.UNAUTHORIZED,NOT_VALID_JSON_WEB_TOKEN, jwt);
	        }
	    }




}
