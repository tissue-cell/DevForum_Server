package com.tissue_cell.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.tissue_cell.config.PropertiesConfig;

@Service("github")
public class GithubOAuthServiceImpl implements OAuthService {
	
	@Autowired
	PropertiesConfig propertiesConfig;
	
	@Autowired
	RestTemplate restTemplate;
	
	private ObjectMapper mapper;
	
	@Autowired
	public GithubOAuthServiceImpl() {
		mapper = new ObjectMapper();
		
		//JSON 파싱을 위한 기본값 세팅
		//요청시 파라미터는 스네이크 케이스로 세팅되므로 Object mapper에 미리 설정해준다.
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		mapper.setSerializationInclusion(Include.NON_NULL);
	}
	

	@Override
	public String getAccessToken(String code) throws Exception {
		//액세스 토큰 요청을 위한 리퀘스트 파라미터
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("code", code);
		params.add("client_id", propertiesConfig.getGithubClientId());
		params.add("client_secret", propertiesConfig.getGithubSecret());
				
		HttpHeaders headers = new HttpHeaders();
		headers.add("Accept", "application/json");
				
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
				
		//System.out.println(propertiesConfig.getGithubSecretKey());
		ResponseEntity<String> responseGithubToken = restTemplate.exchange(
				"https://github.com/login/oauth/access_token", // {요청할 서버 주소}
				HttpMethod.POST, // {요청할 방식}
				entity, // {요청할 때 보낼 데이터}
				String.class // {요청시 반환되는 데이터 타입}
				);
				
		Map<String, Object> tokenresult = mapper.readValue(responseGithubToken.getBody(), new TypeReference<HashMap<String, Object>>() {});
				
		return (String)tokenresult.get("access_token");
	}

	@Override
	public String getUserEmail(String accessToken) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "token " + accessToken);

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);

		ResponseEntity<String> responseGithubEmail = restTemplate.exchange("https://api.github.com/user/public_emails", // {요청할
				HttpMethod.GET, // {요청할 방식}
				entity, // {요청할 때 보낼 데이터}
				String.class // {요청시 반환되는 데이터 타입}
		);
		System.out.println(responseGithubEmail.getBody());
		List<Map<String, Object>> userResult = mapper.readValue(responseGithubEmail.getBody(), new TypeReference<ArrayList<HashMap<String, Object>>>() {});
		
		System.out.println(userResult);
		
		return (String) userResult.get(1).get("email");
	}
}