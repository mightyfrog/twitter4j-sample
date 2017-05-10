package org.mightyfrog.android.twitter4jsample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationContext;

/**
 * @author Shigehiro Soejima
 */
public class OAuthActivity extends AppCompatActivity implements Const {
    private OAuthAuthorization mOAuth;
    private RequestToken mRequestToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        oauth();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        getAccessToken(intent.getData());
    }

    /* PRIVATE METHODS */

    /**
     *
     */
    private void oauth() {
        Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<String> emitter) throws Exception {
                mOAuth = new OAuthAuthorization(ConfigurationContext.getInstance());
                mOAuth.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
                mRequestToken = mOAuth.getOAuthRequestToken(CALLBACK_URL);
                emitter.onSuccess(mRequestToken.getAuthorizationURL());
            }
        }).subscribeOn(Schedulers.io())
                .subscribe(new DisposableSingleObserver<String>() {
                    @Override
                    public void onSuccess(@NonNull String url) {
                        openChromeCustomTab(url);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
    }

    /**
     * @param uri The twitter callback URI.
     */
    private void getAccessToken(final Uri uri) {
        if (uri == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        Single.create(new SingleOnSubscribe<AccessToken>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<AccessToken> emitter) throws Exception {
                if (uri.toString().startsWith(CALLBACK_URL)) {
                    String verifier = uri.getQueryParameter("oauth_verifier");
                    emitter.onSuccess(mOAuth.getOAuthAccessToken(mRequestToken, verifier));
                }
            }
        }).subscribeOn(Schedulers.io())
                .subscribe(new DisposableSingleObserver<AccessToken>() {
                    @Override
                    public void onSuccess(@NonNull AccessToken accessToken) {
                        Intent intent = new Intent();
                        intent.putExtra(ACCESS_TOKEN, accessToken);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
    }

    /**
     * @param url The URL.
     */
    private void openChromeCustomTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setShowTitle(true);
        builder.build().launchUrl(this, Uri.parse(url));
    }
}
