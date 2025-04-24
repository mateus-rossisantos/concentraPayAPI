package com.cobranca.concentrapay;

import lombok.Data;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;

@Data
public class Credentials {
	private String clientId;
	private String clientSecret;
	private String certificate;
	private boolean sandbox;
	private boolean debug;

	private String chave;

	public Credentials() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream credentialsFile = classLoader.getResourceAsStream("credentials.json");
		JSONTokener tokener = new JSONTokener(credentialsFile);
		JSONObject credentials = new JSONObject(tokener);
		try {
			credentialsFile.close();
		} catch (IOException e) {
			System.out.println("Impossible to close file credentials.json");
		}

		this.clientId = credentials.getString("client_id");
		this.clientSecret = credentials.getString("client_secret");
		this.certificate = credentials.getString("certificate");
		this.sandbox = credentials.getBoolean("sandbox");
		this.debug = credentials.getBoolean("debug");
		this.chave = credentials.getString("chave");
	}
}
