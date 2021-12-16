package com.example.keycloak.authenticator;

import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * Twilio SMS AuthenticatorFactory
 */
public class SMSAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

	/* シングルトンモデルのためのクラス */
	private static final SMSAuthenticator SINGLETON = new SMSAuthenticator();

	/** プロバイダーID */
	public static final String PROVIDER_ID = "sms-authenticator-with-twilio";

	/* 管理コンソールでこのオーセンティケーターに設定可能なRequirementの選択肢 */
	private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
			AuthenticationExecutionModel.Requirement.REQUIRED,
			AuthenticationExecutionModel.Requirement.DISABLED
	};

	/* 管理コンソールでこのオーセンティケーターに設定可能な項目 */
	private static final List<ProviderConfigProperty> configProperties;
	static {
		configProperties = ProviderConfigurationBuilder
				// SERVICE SID
				.create()
				.property()
				.name(SMSAuthenticator.CONFIG_SMS_SERVICE_SID)
				.label("SERVICE SID")
				.type(ProviderConfigProperty.STRING_TYPE)
				.defaultValue("")
				.helpText("Set the SERVICE SID to connect to Twilio. It usually starts with 'VA'.")
				.add()

				// ACCOUNT SID
				.property()
				.name(SMSAuthenticator.CONFIG_SMS_ACCOUNT_SID)
				.label("ACCOUNT SID")
				.type(ProviderConfigProperty.STRING_TYPE)
				.defaultValue("")
				.helpText("Set the ACCOUNT SID to connect to Twilio. It usually starts with 'SK'.")
				.add()

				// AUTH TOKEN
				.property()
				.name(SMSAuthenticator.CONFIG_SMS_AUTH_TOKEN)
				.label("AUTH TOKEN")
				.type(ProviderConfigProperty.STRING_TYPE)
				.defaultValue("")
				.helpText("Set the AUTH TOKEN to connect to Twilio.")
				.add()

				.build();
	}

	/*
	 * オーセンティケーターのインスタンスを返すメソッド
	 */
	@Override
	public Authenticator create(KeycloakSession session) {
		return SINGLETON;
	}

	/*
	 * プロバイダーIDを返すメソッド
	 */
	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	/*
	 * 設定可能な項目を返すメソッド
	 */
	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return configProperties;
	}

	/*
	 * ツールチップ・テキストに表示する情報を返すメソッド
	 */
	@Override
	public String getHelpText() {
		return "SMS Authenticate using Twilio.";
	}

	/*
	 * 管理コンソールで表示するオーセンティケーター名を返すメソッド
	 */
	@Override
	public String getDisplayType() {
		return "Twilio SMS Authentication";
	}

	/*
	 * オーセンティケーターが属するカテゴリーを返すメソッド
	 */
	@Override
	public String getReferenceCategory() {
		return "sms-auth-code";
	}

	/*
	 * オーセンティケーターをフロー内で設定きるかどうかを返すメソッド
	 */
	@Override
	public boolean isConfigurable() {
		return true;
	}

	/*
	 * Requirementの選択肢を返すメソッド
	 */
	@Override
	public Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES;
	}

	/*
	 * SMSAuthenticator.setRequiredActions()を呼び出すかどうかを返すメソッド
	 */
	@Override
	public boolean isUserSetupAllowed() {
		return false;
	}

	/*
	 * このクラスが最初に生成されたときに一度だけ呼び出されるメソッド
	 */
	@Override
	public void init(Scope scope) {
	}

	/*
	 * すべてのプロバイダーファクトリが初期化された後に呼び出されるメソッド
	 */
	@Override
	public void postInit(KeycloakSessionFactory factory) {
	}

	/*
	 * サーバーがシャットダウンしたときに呼び出されるメソッド
	 */
	@Override
	public void close() {
	}
}
